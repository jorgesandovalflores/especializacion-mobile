import { HttpStatus, Injectable } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import { DriverDao } from "src/features/drivers/dao/driver.dao";
import { DriverPresenceService } from "src/features/drivers/services/driver-presence.service";
import { TariffService } from "src/features/tariff/services/tariff.service";
import {
    PlannedRoute,
    RoutePlanner,
    RouteUnavailableError,
} from "src/features/routing/route-planner";
import { TripOfferDao } from "../dao/trip-offer.dao";
import { TripDao } from "../dao/trip.dao";
import { PlaceDto, TripRouteRequestDto } from "../dto/place.dto";
import { RatingRequestDto } from "../dto/trip-request.dto";
import { PassengerCurrentTripDto, QuoteDto, TripDto } from "../dto/trip.dto";
import { TripEntity } from "../entities/trip.entity";
import { OfferStatus } from "../enum/offer-status.enum";
import {
    CANCELLABLE_TRIP_STATUSES,
    TripCancelledBy,
    TripStatus,
} from "../enum/trip-status.enum";
import { toOfferDto, toTripDto } from "../mapper/trip.mapper";
import { tripError } from "./trip-errors";
import { TripNotifier } from "./trip-notifier.service";

const RATING_WINDOW_MS = 60 * 60 * 1000;

@Injectable()
export class PassengerTripService {
    constructor(
        private readonly tripDao: TripDao,
        private readonly offerDao: TripOfferDao,
        private readonly driverDao: DriverDao,
        private readonly presence: DriverPresenceService,
        private readonly tariff: TariffService,
        private readonly routes: RoutePlanner,
        private readonly notifier: TripNotifier,
        private readonly i18n: I18nService,
    ) {}

    async quote(
        request: TripRouteRequestDto,
        lang?: string,
    ): Promise<QuoteDto> {
        const route = await this.plan(request, lang);
        const fare = await this.tariff.fareFor(route.distanceMeters);
        return {
            origin: this.place(request.origin),
            destination: this.place(request.destination),
            distanceMeters: route.distanceMeters,
            durationSeconds: route.durationSeconds,
            polyline: route.polyline,
            estimatedFare: fare.amount,
            currency: fare.currency,
        };
    }

    async request(
        passengerId: string,
        request: TripRouteRequestDto,
        lang?: string,
    ): Promise<TripDto> {
        if (await this.tripDao.findActiveByPassenger(passengerId)) {
            throw await tripError(this.i18n, "activeExists", lang);
        }

        const quote = await this.quote(request, lang);
        const trip = await this.tripDao.create({
            passengerId,
            status: TripStatus.REQUESTED,
            originLat: quote.origin.lat,
            originLng: quote.origin.lng,
            originAddress: quote.origin.address,
            destinationLat: quote.destination.lat,
            destinationLng: quote.destination.lng,
            destinationAddress: quote.destination.address,
            distanceMeters: quote.distanceMeters,
            durationSeconds: quote.durationSeconds,
            routePolyline: quote.polyline,
            estimatedFare: quote.estimatedFare,
            currency: quote.currency,
        });

        const dto = toTripDto(trip);
        const nearby = await this.presence.nearbyAvailable(quote.origin);
        await this.notifier.requested(
            dto,
            nearby.map((driver) => driver.driverId),
        );
        this.notifier.updated(dto);
        return dto;
    }

    async current(passengerId: string): Promise<PassengerCurrentTripDto> {
        const trip =
            (await this.tripDao.findActiveByPassenger(passengerId)) ??
            (await this.tripDao.findUnratedByPassenger(
                passengerId,
                new Date(Date.now() - RATING_WINDOW_MS),
            ));
        if (!trip) return { trip: null, offers: [] };

        const offers =
            trip.status === TripStatus.REQUESTED
                ? await this.offerDao.findByTrip(trip.id, OfferStatus.PENDING)
                : [];
        return {
            trip: toTripDto(trip),
            offers: offers.map((offer) => toOfferDto(offer, trip.currency)),
        };
    }

    async acceptOffer(
        passengerId: string,
        tripId: string,
        offerId: string,
        lang?: string,
    ): Promise<TripDto> {
        const trip = await this.ownTrip(passengerId, tripId, lang);
        if (trip.status !== TripStatus.REQUESTED) {
            throw await tripError(this.i18n, "notRequested", lang);
        }

        const offer = await this.offerDao.findById(offerId);
        if (
            !offer ||
            offer.tripId !== tripId ||
            offer.status !== OfferStatus.PENDING
        ) {
            throw await tripError(this.i18n, "offerUnavailable", lang);
        }

        if (await this.presence.activeTrip(offer.driverId)) {
            await this.offerDao.reject(offer.id);
            throw await tripError(this.i18n, "driverBusy", lang);
        }

        const assigned = await this.tripDao.transition(
            tripId,
            [TripStatus.REQUESTED],
            {
                status: TripStatus.ACCEPTED,
                driverId: offer.driverId,
                finalFare: offer.price,
                acceptedAt: new Date(),
            },
        );
        if (!assigned) throw await tripError(this.i18n, "notRequested", lang);

        await this.offerDao.accept(tripId, offer.id);
        await this.presence.setActiveTrip(offer.driverId, {
            tripId,
            passengerId,
        });

        const dto = toTripDto((await this.tripDao.findById(tripId))!);
        this.notifier.offerAccepted(offer.driverId, dto);
        await this.notifier.unavailable(tripId, "TAKEN", offer.driverId);
        this.notifier.status(dto);
        return dto;
    }

    async cancel(
        passengerId: string,
        tripId: string,
        lang?: string,
    ): Promise<TripDto> {
        const trip = await this.ownTrip(passengerId, tripId, lang);
        if (!CANCELLABLE_TRIP_STATUSES.includes(trip.status)) {
            throw await tripError(this.i18n, "notCancellable", lang);
        }

        const cancelled = await this.tripDao.transition(
            tripId,
            CANCELLABLE_TRIP_STATUSES,
            {
                status: TripStatus.CANCELLED,
                cancelledAt: new Date(),
                cancelledBy: TripCancelledBy.PASSENGER,
            },
        );
        if (!cancelled)
            throw await tripError(this.i18n, "notCancellable", lang);

        if (trip.status === TripStatus.REQUESTED) {
            await this.offerDao.rejectAll(tripId);
            await this.notifier.unavailable(tripId, "CANCELLED");
        }
        if (trip.driverId) await this.presence.clearActiveTrip(trip.driverId);

        const dto = toTripDto((await this.tripDao.findById(tripId))!);
        this.notifier.status(dto);
        return dto;
    }

    async rate(
        passengerId: string,
        tripId: string,
        request: RatingRequestDto,
        lang?: string,
    ): Promise<TripDto> {
        const trip = await this.ownTrip(passengerId, tripId, lang);
        if (trip.status !== TripStatus.COMPLETED || !trip.driverId) {
            throw await tripError(this.i18n, "notCompleted", lang);
        }
        const rated = await this.tripDao.rateByPassenger(
            tripId,
            request.rating,
            request.comment ?? null,
        );
        if (!rated) throw await tripError(this.i18n, "alreadyRated", lang);

        await this.driverDao.addRating(trip.driverId, request.rating);
        const dto = toTripDto((await this.tripDao.findById(tripId))!);
        this.notifier.updated(dto);
        return dto;
    }

    private async ownTrip(
        passengerId: string,
        tripId: string,
        lang?: string,
    ): Promise<TripEntity> {
        const trip = await this.tripDao.findById(tripId);
        if (!trip || trip.passengerId !== passengerId) {
            throw await tripError(
                this.i18n,
                "notFound",
                lang,
                HttpStatus.NOT_FOUND,
            );
        }
        return trip;
    }

    private async plan(
        request: TripRouteRequestDto,
        lang?: string,
    ): Promise<PlannedRoute> {
        try {
            return await this.routes.route(request.origin, request.destination);
        } catch (error) {
            if (error instanceof RouteUnavailableError) {
                throw await tripError(
                    this.i18n,
                    "routeUnavailable",
                    lang,
                    HttpStatus.SERVICE_UNAVAILABLE,
                );
            }
            throw error;
        }
    }

    private place(place: PlaceDto) {
        return {
            lat: place.lat,
            lng: place.lng,
            address: place.address ?? null,
        };
    }
}
