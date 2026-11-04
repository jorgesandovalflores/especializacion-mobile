import { Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { CacheService } from "src/core/cache/cache.service";
import { distanceMeters, GeoPoint } from "src/core/geo/geo";

export interface DriverPosition extends GeoPoint {
    bearing: number | null;
    speed: number | null;
    accuracy: number | null;
    at: string;
}

export interface ActiveTripRef {
    tripId: string;
    passengerId: string;
}

export interface NearbyDriver {
    driverId: string;
    distanceMeters: number;
    position: DriverPosition;
}

const ONLINE_KEY = "drivers:online";
const LOCATIONS_KEY = "drivers:locations";
const activeTripKey = (driverId: string) => `driver:${driverId}:trip`;

@Injectable()
export class DriverPresenceService {
    constructor(
        private readonly cache: CacheService,
        private readonly config: ConfigService,
    ) {}

    async setOnline(driverId: string, online: boolean): Promise<void> {
        if (online) await this.cache.sAdd(ONLINE_KEY, driverId);
        else await this.cache.sRem(ONLINE_KEY, driverId);
    }

    async isOnline(driverId: string): Promise<boolean> {
        return this.cache.sIsMember(ONLINE_KEY, driverId);
    }

    async onlineIds(): Promise<string[]> {
        return this.cache.sMembers(ONLINE_KEY);
    }

    async saveLocation(
        driverId: string,
        position: DriverPosition,
    ): Promise<void> {
        await this.cache.hSet(LOCATIONS_KEY, driverId, position);
    }

    async location(driverId: string): Promise<DriverPosition | null> {
        return this.cache.hGet<DriverPosition>(LOCATIONS_KEY, driverId);
    }

    async locations(): Promise<Record<string, DriverPosition>> {
        return this.cache.hGetAll<DriverPosition>(LOCATIONS_KEY);
    }

    async setActiveTrip(driverId: string, ref: ActiveTripRef): Promise<void> {
        await this.cache.set(activeTripKey(driverId), ref);
    }

    async activeTrip(driverId: string): Promise<ActiveTripRef | null> {
        return this.cache.get<ActiveTripRef>(activeTripKey(driverId));
    }

    async clearActiveTrip(driverId: string): Promise<void> {
        await this.cache.del(activeTripKey(driverId));
    }

    isFresh(position: DriverPosition | null): boolean {
        if (!position) return false;
        const staleMs =
            this.numberConfig("DRIVER_LOCATION_STALE_SEC", 120) * 1000;
        return Date.now() - new Date(position.at).getTime() <= staleMs;
    }

    async nearbyAvailable(point: GeoPoint): Promise<NearbyDriver[]> {
        const radiusMeters =
            this.numberConfig("DRIVER_SEARCH_RADIUS_KM", 10) * 1000;
        const [onlineIds, positions] = await Promise.all([
            this.onlineIds(),
            this.locations(),
        ]);

        const nearby: NearbyDriver[] = [];
        for (const driverId of onlineIds) {
            const position = positions[driverId];
            if (!this.isFresh(position ?? null)) continue;
            if (await this.activeTrip(driverId)) continue;
            const distance = distanceMeters(point, position);
            if (distance <= radiusMeters) {
                nearby.push({ driverId, distanceMeters: distance, position });
            }
        }
        return nearby.sort((a, b) => a.distanceMeters - b.distanceMeters);
    }

    private numberConfig(key: string, fallback: number): number {
        const value = Number(this.config.get(key));
        return Number.isFinite(value) && value > 0 ? value : fallback;
    }
}
