import { DriverEntity } from "src/features/drivers/entities/driver.entity";
import { PassengerEntity } from "src/features/passengers/entities/passenger.entity";
import {
    OfferDto,
    TripDriverDto,
    TripDto,
    TripPassengerDto,
} from "../dto/trip.dto";
import { TripOfferEntity } from "../entities/trip-offer.entity";
import { TripEntity } from "../entities/trip.entity";

const iso = (date?: Date | null) => (date ? date.toISOString() : null);

export const toTripDriverDto = (driver: DriverEntity): TripDriverDto => ({
    id: driver.id,
    givenName: driver.givenName,
    familyName: driver.familyName,
    phone: driver.phoneNumber,
    photoUrl: driver.photoUrl ?? null,
    rating: Number(driver.ratingAvg),
    vehicle: {
        plate: driver.vehiclePlate,
        brand: driver.vehicleBrand,
        model: driver.vehicleModel,
        color: driver.vehicleColor,
    },
});

export const toTripPassengerDto = (
    passenger: PassengerEntity,
): TripPassengerDto => ({
    id: passenger.id,
    givenName: passenger.givenName ?? null,
    familyName: passenger.familyName ?? null,
    phone: passenger.phoneNumber,
    photoUrl: passenger.photoUrl ?? null,
});

export const toTripDto = (trip: TripEntity): TripDto => ({
    id: trip.id,
    status: trip.status,
    origin: {
        lat: trip.originLat,
        lng: trip.originLng,
        address: trip.originAddress ?? null,
    },
    destination: {
        lat: trip.destinationLat,
        lng: trip.destinationLng,
        address: trip.destinationAddress ?? null,
    },
    distanceMeters: trip.distanceMeters,
    durationSeconds: trip.durationSeconds,
    polyline: trip.routePolyline,
    estimatedFare: trip.estimatedFare,
    finalFare: trip.finalFare ?? null,
    currency: trip.currency,
    passenger: trip.passenger ? toTripPassengerDto(trip.passenger) : null,
    driver: trip.driver ? toTripDriverDto(trip.driver) : null,
    ratingByPassenger: trip.ratingByPassenger ?? null,
    ratingByDriver: trip.ratingByDriver ?? null,
    requestedAt: trip.requestedAt.toISOString(),
    acceptedAt: iso(trip.acceptedAt),
    arrivedAt: iso(trip.arrivedAt),
    startedAt: iso(trip.startedAt),
    completedAt: iso(trip.completedAt),
    cancelledAt: iso(trip.cancelledAt),
    cancelledBy: trip.cancelledBy ?? null,
});

export const toOfferDto = (
    offer: TripOfferEntity,
    currency: string,
): OfferDto => ({
    id: offer.id,
    tripId: offer.tripId,
    price: offer.price,
    currency,
    etaSeconds: offer.etaSeconds ?? null,
    distanceToPickupMeters: offer.distanceToPickupMeters ?? null,
    status: offer.status,
    createdAt: offer.createdAt.toISOString(),
    driver: offer.driver ? toTripDriverDto(offer.driver) : null,
});
