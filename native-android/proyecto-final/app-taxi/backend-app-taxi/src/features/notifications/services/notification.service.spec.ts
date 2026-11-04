import { PassengerDao } from "src/features/passengers/dao/passenger.dao";
import { PassengerEntity } from "src/features/passengers/entities/passenger.entity";
import { PassengerDeviceDao } from "../dao/passenger-device.dao";
import { DevicePlatform } from "../enum/device-platform.enum";
import { PushMessage, PushSender } from "../remote/push-sender";
import { NotificationService } from "./notification.service";

const message: PushMessage = {
    title: "Tu taxi está llegando",
    body: "Carlos llega en 3 min",
    deeplink: "app-taxi://passenger/menu",
};

describe("NotificationService", () => {
    let devices: jest.Mocked<
        Pick<
            PassengerDeviceDao,
            "upsert" | "findTokensByPassengerId" | "deleteByTokens"
        >
    >;
    let passengers: jest.Mocked<Pick<PassengerDao, "findByPhoneNumber">>;
    let push: { send: jest.Mock };
    let service: NotificationService;

    beforeEach(() => {
        devices = {
            upsert: jest.fn().mockResolvedValue(undefined),
            findTokensByPassengerId: jest
                .fn()
                .mockResolvedValue(["token-a", "token-b"]),
            deleteByTokens: jest.fn().mockResolvedValue(undefined),
        };
        passengers = { findByPhoneNumber: jest.fn() };
        push = {
            send: jest.fn().mockResolvedValue({ sent: 2, invalidTokens: [] }),
        };
        service = new NotificationService(
            devices as unknown as PassengerDeviceDao,
            passengers as unknown as PassengerDao,
            push as unknown as PushSender,
        );
    });

    it("registers the device as ANDROID when the platform is missing", async () => {
        await service.registerDevice("passenger-1", { token: "token-a" });

        expect(devices.upsert).toHaveBeenCalledWith(
            "passenger-1",
            "token-a",
            DevicePlatform.ANDROID,
        );
    });

    it("sends to every device of the passenger", async () => {
        const delivery = await service.sendToPassenger("passenger-1", message);

        expect(push.send).toHaveBeenCalledWith(["token-a", "token-b"], message);
        expect(delivery).toEqual({ sent: 2, invalidTokens: [] });
    });

    it("deletes the tokens that FCM rejected", async () => {
        push.send.mockResolvedValue({ sent: 1, invalidTokens: ["token-b"] });

        await service.sendToPassenger("passenger-1", message);

        expect(devices.deleteByTokens).toHaveBeenCalledWith(["token-b"]);
    });

    it("returns null when no passenger has that phone", async () => {
        passengers.findByPhoneNumber.mockResolvedValue(null);

        expect(await service.sendToPhone("51999999999", message)).toBeNull();
        expect(push.send).not.toHaveBeenCalled();
    });

    it("sends by phone to the passenger devices", async () => {
        passengers.findByPhoneNumber.mockResolvedValue(
            Object.assign(new PassengerEntity(), { id: "passenger-1" }),
        );

        await service.sendToPhone("51987654321", message);

        expect(devices.findTokensByPassengerId).toHaveBeenCalledWith(
            "passenger-1",
        );
    });
});
