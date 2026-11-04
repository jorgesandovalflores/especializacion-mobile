import { Injectable } from "@nestjs/common";
import { RealtimeEmitter } from "src/core/realtime/realtime-emitter.service";
import { RealtimeEvents, Rooms } from "src/core/realtime/realtime.events";
import { CacheService } from "src/core/cache/cache.service";
import { OfferDto, TripDto } from "../dto/trip.dto";

export type TripUnavailableReason = "TAKEN" | "CANCELLED";

export interface DriverLocationEvent {
    driverId: string;
    tripId: string | null;
    lat: number;
    lng: number;
    bearing: number | null;
    speed: number | null;
    at: string;
}

const NOTIFIED_TTL_SEC = 3600;
const notifiedKey = (tripId: string) => `trip:${tripId}:notified`;

@Injectable()
export class TripNotifier {
    constructor(
        private readonly realtime: RealtimeEmitter,
        private readonly cache: CacheService,
    ) {}

    async requested(trip: TripDto, driverIds: string[]): Promise<void> {
        if (driverIds.length === 0) return;
        await this.cache.sAdd(notifiedKey(trip.id), ...driverIds);
        await this.cache.expire(notifiedKey(trip.id), NOTIFIED_TTL_SEC);
        this.realtime.emit(
            driverIds.map(Rooms.driver),
            RealtimeEvents.TripRequested,
            trip,
        );
    }

    async markNotified(tripIds: string[], driverId: string): Promise<void> {
        for (const tripId of tripIds) {
            await this.cache.sAdd(notifiedKey(tripId), driverId);
            await this.cache.expire(notifiedKey(tripId), NOTIFIED_TTL_SEC);
        }
    }

    offer(passengerId: string, offer: OfferDto): void {
        this.realtime.emit(
            Rooms.passenger(passengerId),
            RealtimeEvents.TripOffer,
            offer,
        );
    }

    offerAccepted(driverId: string, trip: TripDto): void {
        this.realtime.emit(
            Rooms.driver(driverId),
            RealtimeEvents.TripOfferAccepted,
            trip,
        );
    }

    async unavailable(
        tripId: string,
        reason: TripUnavailableReason,
        exceptDriverId?: string,
    ): Promise<void> {
        const driverIds = (
            await this.cache.sMembers(notifiedKey(tripId))
        ).filter((id) => id !== exceptDriverId);
        this.realtime.emit(
            driverIds.map(Rooms.driver),
            RealtimeEvents.TripUnavailable,
            { tripId, reason },
        );
        await this.cache.del(notifiedKey(tripId));
    }

    status(trip: TripDto): void {
        const rooms = [Rooms.passenger(trip.passenger!.id)];
        if (trip.driver) rooms.push(Rooms.driver(trip.driver.id));
        this.realtime.emit(rooms, RealtimeEvents.TripStatus, trip);
        this.updated(trip);
    }

    updated(trip: TripDto): void {
        this.realtime.emit(Rooms.admins, RealtimeEvents.TripUpdated, trip);
    }

    driverLocation(
        passengerId: string | null,
        event: DriverLocationEvent,
    ): void {
        const rooms: string[] = [Rooms.admins];
        if (passengerId) rooms.push(Rooms.passenger(passengerId));
        this.realtime.emit(rooms, RealtimeEvents.DriverLocation, event);
    }
}
