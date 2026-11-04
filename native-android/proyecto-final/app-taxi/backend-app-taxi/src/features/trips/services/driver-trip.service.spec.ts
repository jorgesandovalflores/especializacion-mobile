import { ConfigService } from "@nestjs/config";
import { I18nService } from "nestjs-i18n";
import { DriverPresenceService } from "src/features/drivers/services/driver-presence.service";
import { RoutePlanner } from "src/features/routing/route-planner";
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
    PASSENGER_ID,
    TRIP_ID,
} from "../testing/trip-fixtures";
import { DriverTripService } from "./driver-trip.service";
import { TripNotifier } from "./trip-notifier.service";

const POSITION = {
    lat: -12.05,
    lng: -77.04,
    bearing: null,
    speed: null,
    accuracy: null,
    at: new Date().toISOString(),
};

describe("DriverTripService", () => {
    let tripDao: jest.Mocked<TripDao>;
    let offerDao: jest.Mocked<TripOfferDao>;
    let presence: jest.Mocked<DriverPresenceService>;
    let routes: jest.Mocked<RoutePlanner>;
    let notifier: jest.Mocked<TripNotifier>;
    let service: DriverTripService;

    beforeEach(() => {
        tripDao = {
            findById: jest.fn().mockResolvedValue(buildTrip()),
            transition: jest.fn().mockResolvedValue(true),
            findOpenRequests: jest.fn().mockResolvedValue([buildTrip()]),
        } as unknown as jest.Mocked<TripDao>;
        offerDao = {
            exists: jest.fn().mockResolvedValue(false),
            create: jest.fn().mockResolvedValue(buildOffer()),
            offeredTripIds: jest.fn().mockResolvedValue(new Set()),
        } as unknown as jest.Mocked<TripOfferDao>;
        presence = {
            isOnline: jest.fn().mockResolvedValue(true),
            activeTrip: jest.fn().mockResolvedValue(null),
            location: jest.fn().mockResolvedValue(POSITION),
            clearActiveTrip: jest.fn(),
        } as unknown as jest.Mocked<DriverPresenceService>;
        routes = {
            route: jest.fn().mockResolvedValue({
                distanceMeters: 1300,
                durationSeconds: 240,
                polyline: "xyz",
            }),
        };
        notifier = {
            offer: jest.fn(),
            status: jest.fn(),
            updated: jest.fn(),
            markNotified: jest.fn(),
        } as unknown as jest.Mocked<TripNotifier>;

        service = new DriverTripService(
            tripDao,
            offerDao,
            presence,
            routes,
            notifier,
            { get: jest.fn() } as unknown as ConfigService,
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

    it("sends the offer to the passenger with the OSRM ETA to the pickup", async () => {
        const offer = await service.offer(DRIVER_ID, TRIP_ID, { price: 18.5 });

        expect(routes.route).toHaveBeenCalledWith(POSITION, {
            lat: -12.0464,
            lng: -77.0428,
        });
        expect(offerDao.create).toHaveBeenCalledWith({
            tripId: TRIP_ID,
            driverId: DRIVER_ID,
            price: 18.5,
            etaSeconds: 240,
            distanceToPickupMeters: 1300,
        });
        expect(notifier.offer).toHaveBeenCalledWith(PASSENGER_ID, offer);
    });

    it("does not let an offline driver send offers", async () => {
        presence.isOnline.mockResolvedValue(false);

        const error = await failure(
            service.offer(DRIVER_ID, TRIP_ID, { price: 18.5 }),
        );

        expect(error.message).toBe("trip.driverOffline");
        expect(offerDao.create).not.toHaveBeenCalled();
    });

    it("allows a single offer per trip", async () => {
        offerDao.exists.mockResolvedValue(true);

        const error = await failure(
            service.offer(DRIVER_ID, TRIP_ID, { price: 18.5 }),
        );

        expect(error.message).toBe("trip.offerExists");
    });

    it("moves ACCEPTED to ARRIVED and notifies both sides", async () => {
        const accepted = buildTrip({
            status: TripStatus.ACCEPTED,
            driverId: DRIVER_ID,
            driver: buildDriver(),
        });
        tripDao.findById
            .mockResolvedValueOnce(accepted)
            .mockResolvedValueOnce({ ...accepted, status: TripStatus.ARRIVED });

        const trip = await service.changeStatus(DRIVER_ID, TRIP_ID, {
            status: TripStatus.ARRIVED,
        });

        expect(tripDao.transition).toHaveBeenCalledWith(
            TRIP_ID,
            [TripStatus.ACCEPTED],
            expect.objectContaining({ status: TripStatus.ARRIVED }),
        );
        expect(notifier.status).toHaveBeenCalledWith(trip);
        expect(presence.clearActiveTrip).not.toHaveBeenCalled();
    });

    it("frees the driver when the trip is completed", async () => {
        tripDao.findById.mockResolvedValue(
            buildTrip({
                status: TripStatus.IN_PROGRESS,
                driverId: DRIVER_ID,
                driver: buildDriver(),
            }),
        );

        await service.changeStatus(DRIVER_ID, TRIP_ID, {
            status: TripStatus.COMPLETED,
        });

        expect(presence.clearActiveTrip).toHaveBeenCalledWith(DRIVER_ID);
    });

    it("rejects skipping a state", async () => {
        tripDao.findById.mockResolvedValue(
            buildTrip({ status: TripStatus.ACCEPTED, driverId: DRIVER_ID }),
        );

        const error = await failure(
            service.changeStatus(DRIVER_ID, TRIP_ID, {
                status: TripStatus.COMPLETED,
            }),
        );

        expect(error.message).toBe("trip.invalidTransition");
        expect(tripDao.transition).not.toHaveBeenCalled();
    });

    it("lists open requests without the ones already offered", async () => {
        offerDao.offeredTripIds.mockResolvedValue(new Set([TRIP_ID]));

        expect(await service.openRequests(DRIVER_ID)).toEqual([]);
    });
});
