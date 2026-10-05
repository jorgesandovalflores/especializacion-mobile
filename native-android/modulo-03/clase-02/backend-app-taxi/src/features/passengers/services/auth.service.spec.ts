import { HttpStatus } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import { I18nService } from "nestjs-i18n";
import { CacheService } from "src/core/cache/cache.service";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { PassengerDao } from "../dao/passenger.dao";
import { PassengerOtpDao } from "../dao/passenger-otp.dao";
import { PassengerEntity } from "../entities/passenger.entity";
import { PassengerOtpEntity } from "../entities/passenger-otp.entity";
import { PassengerStatus } from "../enum/passenger-status.enum";
import { SmsSender } from "../remote/sms-sender";
import { AuthService } from "./auth.service";

const PHONE = "51987654321";

const buildPassenger = (status = PassengerStatus.ACTIVE): PassengerEntity =>
    Object.assign(new PassengerEntity(), {
        id: "passenger-1",
        phoneNumber: PHONE,
        givenName: "Ana",
        status,
    });

const buildOtp = (): PassengerOtpEntity =>
    Object.assign(new PassengerOtpEntity(), {
        id: "otp-1",
        passengerId: "passenger-1",
        code: "1234",
        expiresAt: new Date("2026-09-28T15:02:00.000Z"),
        used: false,
    });

describe("AuthService", () => {
    let store: Map<string, unknown>;
    let passengerDao: jest.Mocked<
        Pick<
            PassengerDao,
            | "findByPhoneNumber"
            | "createInactiveByPhone"
            | "touchLastLoginAtById"
        >
    >;
    let otpDao: jest.Mocked<
        Pick<PassengerOtpDao, "createOtp" | "findValidOtp" | "markAsUsed">
    >;
    let sms: jest.Mocked<Pick<SmsSender, "sendOtp">>;
    let service: AuthService;

    beforeEach(() => {
        store = new Map();
        const cache = {
            get: jest.fn((key: string) =>
                Promise.resolve(store.get(key) ?? null),
            ),
            set: jest.fn((key: string, value: unknown) => {
                store.set(key, value);
                return Promise.resolve();
            }),
            del: jest.fn((key: string) => {
                store.delete(key);
                return Promise.resolve();
            }),
        };
        passengerDao = {
            findByPhoneNumber: jest.fn().mockResolvedValue(buildPassenger()),
            createInactiveByPhone: jest
                .fn()
                .mockResolvedValue(
                    buildPassenger(PassengerStatus.INACTIVE_REGISTER),
                ),
            touchLastLoginAtById: jest.fn().mockResolvedValue(true),
        };
        otpDao = {
            createOtp: jest.fn().mockResolvedValue(buildOtp()),
            findValidOtp: jest.fn().mockResolvedValue(buildOtp()),
            markAsUsed: jest.fn().mockResolvedValue(true),
        };
        sms = { sendOtp: jest.fn().mockResolvedValue("message-1") };
        const i18n = { t: jest.fn((key: string) => Promise.resolve(key)) };
        const config = {
            get: jest.fn((key: string) =>
                key.endsWith("SECRET") ? "secret" : undefined,
            ),
        };
        const jwt = {
            signAsync: jest.fn((payload: { rt?: boolean }) =>
                Promise.resolve(payload.rt ? "refresh" : "access"),
            ),
        };

        service = new AuthService(
            passengerDao as unknown as PassengerDao,
            otpDao as unknown as PassengerOtpDao,
            i18n as unknown as I18nService,
            cache as unknown as CacheService,
            config as unknown as ConfigService,
            jwt as unknown as JwtService,
            sms as unknown as SmsSender,
        );
    });

    const errorOf = async (
        promise: Promise<unknown>,
    ): Promise<HttpCustomException> => {
        try {
            await promise;
        } catch (error) {
            return error as HttpCustomException;
        }
        throw new Error("Se esperaba una excepción");
    };

    describe("generateOtpByPhone", () => {
        it("crea una OTP de 4 dígitos, la envía y activa el rate limit", async () => {
            const result = await service.generateOtpByPhone({ phone: PHONE });

            const [, code] = otpDao.createOtp.mock.calls[0];
            expect(code).toMatch(/^[0-9]{4}$/);
            expect(sms.sendOtp).toHaveBeenCalledWith(PHONE, code);
            expect(result).toEqual({
                success: true,
                expiresAt: "2026-09-28T15:02:00.000Z",
                ttlSec: 120,
                messageId: "message-1",
            });
            expect(store.get("otp:passenger:passenger-1:lock")).toBe("1");
        });

        it("registra al pasajero como INACTIVE_REGISTER si no existe", async () => {
            passengerDao.findByPhoneNumber.mockResolvedValue(null);

            await service.generateOtpByPhone({ phone: PHONE });

            expect(passengerDao.createInactiveByPhone).toHaveBeenCalledWith(
                PHONE,
            );
        });

        it("responde 422 si ya hay un código activo", async () => {
            store.set("otp:passenger:passenger-1:lock", "1");

            const error = await errorOf(
                service.generateOtpByPhone({ phone: PHONE }),
            );

            expect(error.getStatus()).toBe(HttpStatus.UNPROCESSABLE_ENTITY);
            expect(error.getResponse()).toEqual({
                status_code: 422,
                message: "auth.activeCodeExists",
            });
            expect(sms.sendOtp).not.toHaveBeenCalled();
        });

        it("responde 422 y no activa el rate limit si el SMS falla", async () => {
            sms.sendOtp.mockResolvedValue(null);

            const error = await errorOf(
                service.generateOtpByPhone({ phone: PHONE }),
            );

            expect(error.getResponse()).toEqual({
                status_code: 422,
                message: "auth.deliveryFailed",
            });
            expect(store.has("otp:passenger:passenger-1:lock")).toBe(false);
        });
    });

    describe("verifyOtpByPhone", () => {
        it("devuelve tokens y usuario, marca la OTP y libera el rate limit", async () => {
            store.set("otp:passenger:passenger-1:lock", "1");

            const result = await service.verifyOtpByPhone({
                phone: PHONE,
                code: "1234",
            });

            expect(result.accessToken).toBe("access");
            expect(result.refreshToken).toBe("refresh");
            expect(result.user.status).toBe(PassengerStatus.ACTIVE);
            expect(otpDao.markAsUsed).toHaveBeenCalledWith("otp-1");
            expect(passengerDao.touchLastLoginAtById).toHaveBeenCalledWith(
                "passenger-1",
            );
            expect(store.has("otp:passenger:passenger-1:lock")).toBe(false);
        });

        it("un código inválido responde 422 y suma un intento", async () => {
            otpDao.findValidOtp.mockResolvedValue(null);

            const error = await errorOf(
                service.verifyOtpByPhone({ phone: PHONE, code: "0000" }),
            );

            expect(error.getResponse()).toEqual({
                status_code: 422,
                message: "auth.invalidOrExpired",
            });
            expect(store.get("otp:passenger:passenger-1:attempts")).toBe(1);
        });

        it("bloquea con 429 al superar los intentos permitidos", async () => {
            store.set("otp:passenger:passenger-1:attempts", 5);

            const error = await errorOf(
                service.verifyOtpByPhone({ phone: PHONE, code: "1234" }),
            );

            expect(error.getStatus()).toBe(HttpStatus.TOO_MANY_REQUESTS);
            expect(otpDao.findValidOtp).not.toHaveBeenCalled();
        });

        it("responde 422 si el pasajero no existe", async () => {
            passengerDao.findByPhoneNumber.mockResolvedValue(null);

            const error = await errorOf(
                service.verifyOtpByPhone({ phone: PHONE, code: "1234" }),
            );

            expect(error.getResponse()).toEqual({
                status_code: 422,
                message: "auth.passenger.notExists",
            });
        });
    });
});
