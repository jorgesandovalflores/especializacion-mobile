import { Logger } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { BrevoSmsService } from "./brevo-sms.service";
import { LabsMobileSmsService } from "./labsmobile-sms.service";
import { SMS_DEV_MESSAGE_ID } from "./sms-sender";
import { smsSenderProvider } from "./sms-sender.provider";

const configOf = (values: Record<string, string>): ConfigService =>
    ({ get: (key: string) => values[key] }) as unknown as ConfigService;

const jsonResponse = (status: number, body: unknown): Response =>
    new Response(JSON.stringify(body), {
        status,
        headers: { "content-type": "application/json" },
    });

const LABS = {
    LABSMOBILE_USER: "user@mail.com",
    LABSMOBILE_API_KEY: "labs-key",
    LABSMOBILE_TEXT_SMS: "Tu código de App Taxi es:",
    LABSMOBILE_SENDER: "AppTaxi",
};

const BREVO = {
    BREVO_API_KEY: "brevo-key",
    BREVO_TEXT_SMS: "Tu código de App Taxi es:",
    BREVO_SENDER: "AppTaxi",
};

describe("SmsSender", () => {
    let fetchMock: jest.SpiedFunction<typeof fetch>;

    beforeEach(() => {
        fetchMock = jest.spyOn(global, "fetch");
        jest.spyOn(Logger.prototype, "error").mockImplementation(
            () => undefined,
        );
        jest.spyOn(Logger.prototype, "warn").mockImplementation(
            () => undefined,
        );
    });

    afterEach(() => jest.restoreAllMocks());

    const lastRequest = () => {
        const [url, init] = fetchMock.mock.calls[0];
        return {
            url: url as string,
            headers: init?.headers as Record<string, string>,
            body: JSON.parse(init?.body as string) as Record<string, unknown>,
        };
    };

    describe("LabsMobileSmsService", () => {
        it("envía con Basic Auth y devuelve el subid", async () => {
            fetchMock.mockResolvedValue(
                jsonResponse(200, {
                    code: "0",
                    message: "Message has been successfully sent",
                    subid: "sub-123",
                }),
            );

            const id = await new LabsMobileSmsService(configOf(LABS)).sendOtp(
                "51987654321",
                "1234",
            );

            const req = lastRequest();
            expect(id).toBe("sub-123");
            expect(req.url).toBe("https://api.labsmobile.com/json/send");
            expect(req.headers.authorization).toBe(
                `Basic ${Buffer.from("user@mail.com:labs-key").toString("base64")}`,
            );
            expect(req.body).toEqual({
                message: "Tu código de App Taxi es: 1234",
                tpoa: "AppTaxi",
                recipient: [{ msisdn: "51987654321" }],
            });
        });

        it("devuelve null si LabsMobile responde un code distinto de 0", async () => {
            fetchMock.mockResolvedValue(
                jsonResponse(200, {
                    code: "35",
                    message: "The account has no enough credit",
                }),
            );

            const id = await new LabsMobileSmsService(configOf(LABS)).sendOtp(
                "51987654321",
                "1234",
            );

            expect(id).toBeNull();
        });

        it("devuelve null ante un error HTTP", async () => {
            fetchMock.mockResolvedValue(
                jsonResponse(401, { code: "401", message: "Unauthorized" }),
            );

            const id = await new LabsMobileSmsService(configOf(LABS)).sendOtp(
                "51987654321",
                "1234",
            );

            expect(id).toBeNull();
        });

        it("devuelve null si la red falla o vence el timeout", async () => {
            fetchMock.mockRejectedValue(
                new DOMException(
                    "The operation was aborted due to timeout",
                    "TimeoutError",
                ),
            );

            const id = await new LabsMobileSmsService(configOf(LABS)).sendOtp(
                "51987654321",
                "1234",
            );

            expect(id).toBeNull();
        });

        it("sin credenciales y fuera de producción usa el modo desarrollo", async () => {
            const id = await new LabsMobileSmsService(
                configOf({ NODE_ENV: "development" }),
            ).sendOtp("51987654321", "1234");

            expect(id).toBe(SMS_DEV_MESSAGE_ID);
            expect(fetchMock).not.toHaveBeenCalled();
        });

        it("sin credenciales en producción no envía y devuelve null", async () => {
            const id = await new LabsMobileSmsService(
                configOf({
                    NODE_ENV: "production",
                    LABSMOBILE_USER: "user@mail.com",
                }),
            ).sendOtp("51987654321", "1234");

            expect(id).toBeNull();
            expect(fetchMock).not.toHaveBeenCalled();
        });
    });

    describe("BrevoSmsService", () => {
        it("envía con api-key y devuelve el messageId", async () => {
            fetchMock.mockResolvedValue(jsonResponse(201, { messageId: 987 }));

            const id = await new BrevoSmsService(configOf(BREVO)).sendOtp(
                "51987654321",
                "1234",
            );

            const req = lastRequest();
            expect(id).toBe("987");
            expect(req.headers["api-key"]).toBe("brevo-key");
            expect(req.body).toMatchObject({
                recipient: "+51987654321",
                content: "Tu código de App Taxi es: 1234",
                sender: "AppTaxi",
            });
        });

        it("devuelve null ante un error HTTP", async () => {
            fetchMock.mockResolvedValue(
                jsonResponse(400, { message: "Invalid sender" }),
            );

            const id = await new BrevoSmsService(configOf(BREVO)).sendOtp(
                "51987654321",
                "1234",
            );

            expect(id).toBeNull();
        });
    });

    describe("smsSenderProvider (SMS_PROVIDER)", () => {
        const factory = (smsProvider?: string) => {
            const config = configOf(
                smsProvider ? { SMS_PROVIDER: smsProvider } : {},
            );
            const brevo = new BrevoSmsService(config);
            const labsMobile = new LabsMobileSmsService(config);
            const create = (
                smsSenderProvider as unknown as {
                    useFactory: (...args: unknown[]) => unknown;
                }
            ).useFactory;
            return {
                result: () => create(config, brevo, labsMobile),
                brevo,
                labsMobile,
            };
        };

        it("usa Brevo por defecto", () => {
            const { result, brevo } = factory();
            expect(result()).toBe(brevo);
        });

        it("usa LabsMobile con SMS_PROVIDER=labsmobile (sin importar mayúsculas)", () => {
            const { result, labsMobile } = factory("LabsMobile");
            expect(result()).toBe(labsMobile);
        });

        it("falla al arrancar con un proveedor desconocido", () => {
            const { result } = factory("twilio");
            expect(result).toThrow('SMS_PROVIDER inválido: "twilio"');
        });
    });
});
