import { HttpStatus, Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { I18nService } from "nestjs-i18n";
import { distanceMeters } from "src/core/geo/geo";
import {
    DriverPosition,
    DriverPresenceService,
} from "src/features/drivers/services/driver-presence.service";
import { RoutePlanner } from "src/features/routing/route-planner";
import { TripOfferDao } from "../dao/trip-offer.dao";
import { TripDao } from "../dao/trip.dao";
import {
    OfferRequestDto,
    RatingRequestDto,
    TripStatusRequestDto,
} from "../dto/trip-request.dto";
import { DriverCurrentTripDto, OfferDto, TripDto } from "../dto/trip.dto";
import { TripEntity } from "../entities/trip.entity";
import { NEXT_DRIVER_STATUS, TripStatus } from "../enum/trip-status.enum";
import { toOfferDto, toTripDto } from "../mapper/trip.mapper";
import { tripError } from "./trip-errors";
import { TripNotifier } from "./trip-notifier.service";

const RATING_WINDOW_MS = 60 * 60 * 1000;
const FALLBACK_SPEED_MPS = 25_000 / 3600;

const TIMESTAMP_BY_STATUS: Partial<Record<TripStatus, keyof TripEntity>> = {
    [TripStatus.ARRIVED]: "arrivedAt",
    [TripStatus.IN_PROGRESS]: "startedAt",
    [TripStatus.COMPLETED]: "completedAt",
};

@Injectable()
export class DriverTripService {
    constructor(
        private readonly tripDao: TripDao,
        private readonly offerDao: TripOfferDao,
        private readonly presence: DriverPresenceService,
        private readonly routes: RoutePlanner,
        private readonly notifier: TripNotifier,
        private readonly config: ConfigService,
        private readonly i18n: I18nService,
    ) {}

    async openRequests(driverId: string): Promise<TripDto[]> {
        if (await this.presence.activeTrip(driverId)) return [];

        const ttlSec = Number(this.config.get("TRIP_REQUEST_TTL_SEC")) || 600;
        const radiusMeters =
            (Number(this.config.get("DRIVER_SEARCH_RADIUS_KM")) || 10) * 1000;
        const trips = await this.tripDao.findOpenRequests(
            new Date(Date.now() - ttlSec * 1000),
        );
        const position = await this.presence.location(driverId);
        const offered = await this.offerDao.offeredTripIds(
            driverId,
            trips.map((trip) => trip.id),
        );

        const open = trips.filter(
            (trip) =>
                !offered.has(trip.id) &&
                (!position ||
                    distanceMeters(position, {
                        lat: trip.originLat,
                        lng: trip.originLng,
                    }) <= radiusMeters),
        );
        await this.notifier.markNotified(
            open.map((trip) => trip.id),
            driverId,
        );
        return open.map(toTripDto);
    }

    async current(driverId: string): Promise<DriverCurrentTripDto> {
        const trip =
            (await this.tripDao.findActiveByDriver(driverId)) ??
            (await this.tripDao.findUnratedByDriver(
                driverId,
                new Date(Date.now() - RATING_WINDOW_MS),
            ));
        return { trip: trip ? toTripDto(trip) : null };
    }

    async offer(
        driverId: string,
        tripId: string,
        request: OfferRequestDto,
        lang?: string,
    ): Promise<OfferDto> {
        if (!(await this.presence.isOnline(driverId))) {
            throw await tripError(this.i18n, "driverOffline", lang);
        }
        if (await this.presence.activeTrip(driverId)) {
            throw await tripError(this.i18n, "driverBusy", lang);
        }

        const trip = await this.tripDao.findById(tripId);
        if (!trip) {
            throw await tripError(
                this.i18n,
                "notFound",
                lang,
                HttpStatus.NOT_FOUND,
            );
        }
        if (trip.status !== TripStatus.REQUESTED) {
            throw await tripError(this.i18n, "notRequested", lang);
        }
        if (await this.offerDao.exists(tripId, driverId)) {
            throw await tripError(this.i18n, "offerExists", lang);
        }

        const approach = await this.approach(
            await this.presence.location(driverId),
            trip,
        );
        const offer = await this.offerDao.create({
            tripId,
            driverId,
            price: request.price,
            etaSeconds: approach?.etaSeconds ?? null,
            distanceToPickupMeters: approach?.distanceMeters ?? null,
        });

        const dto = toOfferDto(offer, trip.currency);
        this.notifier.offer(trip.passengerId, dto);
        return dto;
    }

    async changeStatus(
        driverId: string,
        tripId: string,
        request: TripStatusRequestDto,
        lang?: string,
    ): Promise<TripDto> {
        const trip = await this.ownTrip(driverId, tripId, lang);
        if (NEXT_DRIVER_STATUS[trip.status] !== request.status) {
            throw await tripError(this.i18n, "invalidTransition", lang);
        }

        const timestamp = TIMESTAMP_BY_STATUS[request.status]!;
        const changed = await this.tripDao.transition(tripId, [trip.status], {
            status: request.status,
            [timestamp]: new Date(),
        });
        if (!changed)
            throw await tripError(this.i18n, "invalidTransition", lang);

        if (request.status === TripStatus.COMPLETED) {
            await this.presence.clearActiveTrip(driverId);
        }

        const dto = toTripDto((await this.tripDao.findById(tripId))!);
        this.notifier.status(dto);
        return dto;
    }

    async rate(
        driverId: string,
        tripId: string,
        request: RatingRequestDto,
        lang?: string,
    ): Promise<TripDto> {
        const trip = await this.ownTrip(driverId, tripId, lang);
        if (trip.status !== TripStatus.COMPLETED) {
            throw await tripError(this.i18n, "notCompleted", lang);
        }
        const rated = await this.tripDao.rateByDriver(
            tripId,
            request.rating,
            request.comment ?? null,
        );
        if (!rated) throw await tripError(this.i18n, "alreadyRated", lang);

        const dto = toTripDto((await this.tripDao.findById(tripId))!);
        this.notifier.updated(dto);
        return dto;
    }

    private async ownTrip(
        driverId: string,
        tripId: string,
        lang?: string,
    ): Promise<TripEntity> {
        const trip = await this.tripDao.findById(tripId);
        if (!trip || trip.driverId !== driverId) {
            throw await tripError(
                this.i18n,
                "notFound",
                lang,
                HttpStatus.NOT_FOUND,
            );
        }
        return trip;
    }

    private async approach(
        position: DriverPosition | null,
        trip: TripEntity,
    ): Promise<{ etaSeconds: number; distanceMeters: number } | null> {
        if (!position) return null;
        const pickup = { lat: trip.originLat, lng: trip.originLng };
        try {
            const route = await this.routes.route(position, pickup);
            return {
                etaSeconds: route.durationSeconds,
                distanceMeters: route.distanceMeters,
            };
        } catch {
            const straight = distanceMeters(position, pickup);
            return {
                etaSeconds: Math.round(straight / FALLBACK_SPEED_MPS),
                distanceMeters: straight,
            };
        }
    }
}
