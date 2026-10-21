import { Logger } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { FcmPushService } from "./fcm-push.service";
import { PushDelivery, PushMessage, PushSender } from "./push-sender";

const configOf = (values: Record<string, string>): ConfigService =>
    ({ get: (key: string) => values[key] }) as unknown as ConfigService;

const message: PushMessage = {
    title: "Tu taxi está llegando",
    body: "Carlos llega en 3 min",
    deeplink: "app-taxi://passenger/home",
};

class FakePushSender extends PushSender {
    deliver = jest.fn<Promise<PushDelivery>, [string[], PushMessage]>();

    constructor(
        config: ConfigService,
        private readonly configured: boolean,
    ) {
        super(config);
    }

    protected isConfigured(): boolean {
        return this.configured;
    }
}

describe("PushSender", () => {
    let warn: jest.SpyInstance;

    beforeEach(() => {
        warn = jest
            .spyOn(Logger.prototype, "warn")
            .mockImplementation(() => undefined);
        jest.spyOn(Logger.prototype, "error").mockImplementation(
            () => undefined,
        );
    });

    afterEach(() => jest.restoreAllMocks());

    it("does nothing when the passenger has no devices", async () => {
        const sender = new FakePushSender(configOf({}), true);

        expect(await sender.send([], message)).toEqual({
            sent: 0,
            invalidTokens: [],
        });
        expect(sender.deliver).not.toHaveBeenCalled();
    });

    it("logs the push in development when there are no credentials", async () => {
        const sender = new FakePushSender(configOf({}), false);

        const delivery = await sender.send(["token-a"], message);

        expect(delivery).toEqual({ sent: 1, invalidTokens: [] });
        expect(warn).toHaveBeenCalledWith(
            expect.stringContaining("[DEV] Push"),
        );
        expect(sender.deliver).not.toHaveBeenCalled();
    });

    it("does not pretend to send in production without credentials", async () => {
        const sender = new FakePushSender(
            configOf({ NODE_ENV: "production" }),
            false,
        );

        expect(await sender.send(["token-a"], message)).toEqual({
            sent: 0,
            invalidTokens: [],
        });
    });

    it("delegates to the provider when it is configured", async () => {
        const sender = new FakePushSender(configOf({}), true);
        sender.deliver.mockResolvedValue({
            sent: 1,
            invalidTokens: ["token-b"],
        });

        const delivery = await sender.send(["token-a", "token-b"], message);

        expect(sender.deliver).toHaveBeenCalledWith(
            ["token-a", "token-b"],
            message,
        );
        expect(delivery).toEqual({ sent: 1, invalidTokens: ["token-b"] });
    });

    it("turns a provider error into zero sent", async () => {
        const sender = new FakePushSender(configOf({}), true);
        sender.deliver.mockRejectedValue(new Error("network down"));

        expect(await sender.send(["token-a"], message)).toEqual({
            sent: 0,
            invalidTokens: [],
        });
    });

    it("FcmPushService needs project, client email and private key", async () => {
        const partial = new FcmPushService(
            configOf({ FIREBASE_PROJECT_ID: "app-taxi" }),
        );

        expect(await partial.send(["token-a"], message)).toEqual({
            sent: 1,
            invalidTokens: [],
        });
        expect(warn).toHaveBeenCalledWith(
            expect.stringContaining("[DEV] Push"),
        );
    });
});
