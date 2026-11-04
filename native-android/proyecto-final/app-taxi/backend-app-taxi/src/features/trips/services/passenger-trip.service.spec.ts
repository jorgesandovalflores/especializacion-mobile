import { I18nService } from "nestjs-i18n";
import { DriverDao } from "src/features/drivers/dao/driver.dao";
import { DriverPresenceService } from "src/features/drivers/services/driver-presence.service";
import { TariffService } from "src/features/tariff/services/tariff.service";
import {
    RoutePlanner,
    RouteUnavailableError,
} from "src/features/routing/route-planner";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { TripOfferDao } from "../dao/trip-offer.dao";
import { TripDao } from "../dao/trip.dao";
import { TripStatus } from "../enum/trip-status.enum";
import {
    buildDriver,
    buildOffer,
    buildTrip,
    DRIVER_ID,
    i18nStub,
    OFFER_ID,
    OTHER_DRIVER_ID,
    PASSENGER_ID,
    TRIP_ID,
} from "../testing/trip-fixtures";
import { PassengerTripService } from "./passenger-trip.service";
import { TripNotifier } from "./trip-notifier.service";

const ROUTE_REQUEST = {
    origin: { lat: -12.0464, lng: -77.0428, address: "Plaza San Martín" },
    destination: { lat: -12.12, lng: -77.03, address: "Parque Kennedy" },
};

describe("PassengerTripService", () => {
    let tripDao: jest.Mocked<TripDao>;
    let offerDao: jest.Mocked<TripOfferDao>;
    let presence: jest.Mocked<DriverPresenceService>;
    let routes: jest.Mocked<RoutePlanner>;
    let notifier: jest.Mocked<TripNotifier>;
    let service: PassengerTripService;

    beforeEach(() => {
        tripDao = {
            findActiveByPassenger: jest.fn().mockResolvedValue(null),
            create: jest.fn().mockResolvedValue(buildTrip()),
            findById: jest.fn().mockResolvedValue(buildTrip()),
            transition: jest.fn().mockResolvedValue(true),
        } as unknown as jest.Mocked<TripDao>;
        offerDao = {
            findById: jest.fn().mockResolvedValue(buildOffer()),
            accept: jest.fn(),
            reject: jest.fn(),
            rejectAll: jest.fn(),
        } as unknown as jest.Mocked<TripOfferDao>;
        presence = {
            nearbyAvailable: jest.fn().mockResolvedValue([
                { driverId: DRIVER_ID, distanceMeters: 800 },
                { driverId: OTHER_DRIVER_ID, distanceMeters: 2500 },
            ]),
            activeTrip: jest.fn().mockResolvedValue(null),
            setActiveTrip: jest.fn(),
            clearActiveTrip: jest.fn(),
        } as unknown as jest.Mocked<DriverPresenceService>;
        routes = {
            route: jest.fn().mockResolvedValue({
                distanceMeters: 9838,
                durationSeconds: 787,
                polyline: "abc",
            }),
        };
        notifier = {
            requested: jest.fn(),
            updated: jest.fn(),
            offerAccepted: jest.fn(),
            unavailable: jest.fn(),
            status: jest.fn(),
        } as unknown as jest.Mocked<TripNotifier>;
        const tariff = {
            fareFor: jest
                .fn()
                .mockResolvedValue({ amount: 19.76, currency: "PEN" }),
        };

        service = new PassengerTripService(
            tripDao,
            offerDao,
            {} as DriverDao,
            presence,
            tariff as unknown as TariffService,
            routes,
            notifier,
            i18nStub() as unknown as I18nService,
        );
    });

    const failure = (promise: Promise<unknown>) =>
        promise.then(
            () => {
                throw new Error("Se esperaba una excepción");
            },
            (error: HttpCustomException) => error,
        );

    it("quotes with the OSRM route and the tariff", async () => {
        const quote = await service.quote(ROUTE_REQUEST);

        expect(routes.route).toHaveBeenCalledWith(
            ROUTE_REQUEST.origin,
            ROUTE_REQUEST.destination,
        );
        expect(quote).toMatchObject({
            distanceMeters: 9838,
            polyline: "abc",
            estimatedFare: 19.76,
            currency: "PEN",
        });
    });

    it("answers 503 when the route cannot be calculated", async () => {
        routes.route.mockRejectedValue(new RouteUnavailableError("NoRoute"));

        const error = await failure(service.quote(ROUTE_REQUEST));

        expect(error.getStatus()).toBe(503);
    });

    it("requests the trip and emits it to the nearby drivers", async () => {
        const trip = await service.request(PASSENGER_ID, ROUTE_REQUEST);

        expect(tripDao.create).toHaveBeenCalledWith(
            expect.objectContaining({
                passengerId: PASSENGER_ID,
                status: TripStatus.REQUESTED,
                estimatedFare: 19.76,
            }),
        );
        expect(presence.nearbyAvailable).toHaveBeenCalledWith({
            lat: -12.0464,
            lng: -77.0428,
            address: "Plaza San Martín",
        });
        expect(notifier.requested).toHaveBeenCalledWith(trip, [
            DRIVER_ID,
            OTHER_DRIVER_ID,
        ]);
    });

    it("rejects a second trip while one is active", async () => {
        tripDao.findActiveByPassenger.mockResolvedValue(buildTrip());

        const error = await failure(
            service.request(PASSENGER_ID, ROUTE_REQUEST),
        );

        expect(error.message).toBe("trip.activeExists");
        expect(tripDao.create).not.toHaveBeenCalled();
    });

    it("accepts an offer: assigns the driver and closes the request for the rest", async () => {
        tripDao.findById
            .mockResolvedValueOnce(buildTrip())
            .mockResolvedValueOnce(
                buildTrip({
                    status: TripStatus.ACCEPTED,
                    driverId: DRIVER_ID,
                    driver: buildDriver(),
                    finalFare: 18.5,
                }),
            );

        const trip = await service.acceptOffer(PASSENGER_ID, TRIP_ID, OFFER_ID);

        expect(tripDao.transition).toHaveBeenCalledWith(
            TRIP_ID,
            [TripStatus.REQUESTED],
            expect.objectContaining({
                status: TripStatus.ACCEPTED,
                driverId: DRIVER_ID,
                finalFare: 18.5,
            }),
        );
        expect(offerDao.accept).toHaveBeenCalledWith(TRIP_ID, OFFER_ID);
        expect(presence.setActiveTrip).toHaveBeenCalledWith(DRIVER_ID, {
            tripId: TRIP_ID,
            passengerId: PASSENGER_ID,
        });
        expect(notifier.offerAccepted).toHaveBeenCalledWith(DRIVER_ID, trip);
        expect(notifier.unavailable).toHaveBeenCalledWith(
            TRIP_ID,
            "TAKEN",
            DRIVER_ID,
        );
        expect(trip.status).toBe(TripStatus.ACCEPTED);
    });

    it("rejects the offer of a driver that is already busy", async () => {
        presence.activeTrip.mockResolvedValue({
            tripId: "other",
            passengerId: "other",
        });

        const error = await failure(
            service.acceptOffer(PASSENGER_ID, TRIP_ID, OFFER_ID),
        );

        expect(error.message).toBe("trip.driverBusy");
        expect(offerDao.reject).toHaveBeenCalledWith(OFFER_ID);
        expect(tripDao.transition).not.toHaveBeenCalled();
    });

    it("cancels a requested trip and tells the notified drivers", async () => {
        await service.cancel(PASSENGER_ID, TRIP_ID);

        expect(offerDao.rejectAll).toHaveBeenCalledWith(TRIP_ID);
        expect(notifier.unavailable).toHaveBeenCalledWith(TRIP_ID, "CANCELLED");
        expect(notifier.status).toHaveBeenCalled();
    });

    it("hides trips of other passengers", async () => {
        const error = await failure(service.cancel("someone-else", TRIP_ID));

        expect(error.getStatus()).toBe(404);
    });
});
