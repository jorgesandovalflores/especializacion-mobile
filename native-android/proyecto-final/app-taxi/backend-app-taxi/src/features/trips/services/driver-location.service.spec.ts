import { DriverPresenceService } from "src/features/drivers/services/driver-presence.service";
import { DriverLocationService } from "./driver-location.service";
import { TripNotifier } from "./trip-notifier.service";
import { TripTrackService } from "./trip-track.service";
import { DRIVER_ID, PASSENGER_ID, TRIP_ID } from "../testing/trip-fixtures";

describe("DriverLocationService", () => {
    let presence: jest.Mocked<DriverPresenceService>;
    let track: jest.Mocked<TripTrackService>;
    let notifier: jest.Mocked<TripNotifier>;
    let service: DriverLocationService;

    beforeEach(() => {
        presence = {
            saveLocation: jest.fn(),
            activeTrip: jest.fn().mockResolvedValue(null),
        } as unknown as jest.Mocked<DriverPresenceService>;
        track = {
            append: jest.fn(),
        } as unknown as jest.Mocked<TripTrackService>;
        notifier = {
            driverLocation: jest.fn(),
        } as unknown as jest.Mocked<TripNotifier>;
        service = new DriverLocationService(presence, track, notifier);
    });

    it("only notifies the admins when the driver is free", async () => {
        await service.update(DRIVER_ID, {
            lat: -12.05,
            lng: -77.04,
            bearing: 90,
        });

        expect(presence.saveLocation).toHaveBeenCalled();
        expect(track.append).not.toHaveBeenCalled();
        expect(notifier.driverLocation).toHaveBeenCalledWith(
            null,
            expect.objectContaining({
                driverId: DRIVER_ID,
                tripId: null,
                bearing: 90,
            }),
        );
    });

    it("records the track and notifies the passenger during a trip", async () => {
        presence.activeTrip.mockResolvedValue({
            tripId: TRIP_ID,
            passengerId: PASSENGER_ID,
        });

        await service.update(DRIVER_ID, { lat: -12.05, lng: -77.04 });

        expect(track.append).toHaveBeenCalledWith(
            TRIP_ID,
            expect.objectContaining({ lat: -12.05, lng: -77.04 }),
        );
        expect(notifier.driverLocation).toHaveBeenCalledWith(
            PASSENGER_ID,
            expect.objectContaining({ tripId: TRIP_ID }),
        );
    });

    it("ignores invalid coordinates", async () => {
        expect(await service.update(DRIVER_ID, { lat: 120, lng: "x" })).toBe(
            false,
        );
        expect(presence.saveLocation).not.toHaveBeenCalled();
    });
});
