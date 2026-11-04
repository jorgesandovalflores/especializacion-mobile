import { DriverEntity } from "src/features/drivers/entities/driver.entity";
import { DriverStatus } from "src/features/drivers/enum/driver-status.enum";
import { PassengerEntity } from "src/features/passengers/entities/passenger.entity";
import { PassengerStatus } from "src/features/passengers/enum/passenger-status.enum";
import { TripOfferEntity } from "../entities/trip-offer.entity";
import { TripEntity } from "../entities/trip.entity";
import { OfferStatus } from "../enum/offer-status.enum";
import { TripStatus } from "../enum/trip-status.enum";

export const PASSENGER_ID = "11111111-1111-4111-8111-111111111111";
export const DRIVER_ID = "22222222-2222-4222-8222-222222222222";
export const OTHER_DRIVER_ID = "33333333-3333-4333-8333-333333333333";
export const TRIP_ID = "44444444-4444-4444-8444-444444444444";
export const OFFER_ID = "55555555-5555-4555-8555-555555555555";

export const buildPassenger = (): PassengerEntity =>
    Object.assign(new PassengerEntity(), {
        id: PASSENGER_ID,
        phoneNumber: "51980773166",
        givenName: "Jorge",
        familyName: "Sandoval",
        status: PassengerStatus.ACTIVE,
    });

export const buildDriver = (id = DRIVER_ID): DriverEntity =>
    Object.assign(new DriverEntity(), {
        id,
        phoneNumber: "51900000001",
        givenName: "Carlos",
        familyName: "Quispe",
        vehiclePlate: "ABC-123",
        vehicleBrand: "Toyota",
        vehicleModel: "Yaris",
        vehicleColor: "Blanco",
        status: DriverStatus.ACTIVE,
        ratingAvg: 4.9,
        ratingCount: 12,
        createdAt: new Date("2026-11-01T10:00:00Z"),
    });

export const buildTrip = (overrides: Partial<TripEntity> = {}): TripEntity =>
    Object.assign(new TripEntity(), {
        id: TRIP_ID,
        passengerId: PASSENGER_ID,
        passenger: buildPassenger(),
        driverId: null,
        driver: null,
        status: TripStatus.REQUESTED,
        originLat: -12.0464,
        originLng: -77.0428,
        originAddress: "Plaza San Martín",
        destinationLat: -12.12,
        destinationLng: -77.03,
        destinationAddress: "Parque Kennedy",
        distanceMeters: 9838,
        durationSeconds: 787,
        routePolyline: "abc",
        estimatedFare: 19.76,
        finalFare: null,
        currency: "PEN",
        requestedAt: new Date("2026-11-02T15:00:00Z"),
        ...overrides,
    });

export const buildOffer = (
    overrides: Partial<TripOfferEntity> = {},
): TripOfferEntity =>
    Object.assign(new TripOfferEntity(), {
        id: OFFER_ID,
        tripId: TRIP_ID,
        driverId: DRIVER_ID,
        driver: buildDriver(),
        price: 18.5,
        etaSeconds: 240,
        distanceToPickupMeters: 1300,
        status: OfferStatus.PENDING,
        createdAt: new Date("2026-11-02T15:00:10Z"),
        ...overrides,
    });

export const i18nStub = () => ({
    t: jest.fn((key: string) => Promise.resolve(key)),
});
