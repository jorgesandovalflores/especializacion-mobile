import { Injectable } from "@nestjs/common";
import { DriverPresenceService } from "src/features/drivers/services/driver-presence.service";
import { TripNotifier } from "./trip-notifier.service";
import { TripTrackService } from "./trip-track.service";

export interface DriverLocationPayload {
    lat?: unknown;
    lng?: unknown;
    bearing?: unknown;
    speed?: unknown;
    accuracy?: unknown;
}

const finiteOrNull = (value: unknown): number | null =>
    typeof value === "number" && Number.isFinite(value) ? value : null;

@Injectable()
export class DriverLocationService {
    constructor(
        private readonly presence: DriverPresenceService,
        private readonly track: TripTrackService,
        private readonly notifier: TripNotifier,
    ) {}

    async update(
        driverId: string,
        payload: DriverLocationPayload,
    ): Promise<boolean> {
        const lat = finiteOrNull(payload?.lat);
        const lng = finiteOrNull(payload?.lng);
        if (
            lat === null ||
            lng === null ||
            Math.abs(lat) > 90 ||
            Math.abs(lng) > 180
        ) {
            return false;
        }

        const at = new Date().toISOString();
        const position = {
            lat,
            lng,
            bearing: finiteOrNull(payload.bearing),
            speed: finiteOrNull(payload.speed),
            accuracy: finiteOrNull(payload.accuracy),
            at,
        };
        await this.presence.saveLocation(driverId, position);

        const activeTrip = await this.presence.activeTrip(driverId);
        if (activeTrip) {
            await this.track.append(activeTrip.tripId, { lat, lng, at });
        }

        this.notifier.driverLocation(activeTrip?.passengerId ?? null, {
            driverId,
            tripId: activeTrip?.tripId ?? null,
            lat,
            lng,
            bearing: position.bearing,
            speed: position.speed,
            at,
        });
        return true;
    }
}
