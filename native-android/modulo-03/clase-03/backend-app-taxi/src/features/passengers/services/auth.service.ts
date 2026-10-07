import { HttpStatus, Injectable } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import { randomInt } from "crypto";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { CacheService } from "src/core/cache/cache.service";

import { PassengerDao } from "../dao/passenger.dao";
import { PassengerOtpDao } from "../dao/passenger-otp.dao";
import { PassengerOtpCreatedRequestDto } from "../dto/passenger-otp-created-request.dto";
import { PassengerOtpValidatedRequestDto } from "../dto/passenger-otp-validated-request.dto";
import { PassengerOtpCreatedResponseDto } from "../dto/passenger-otp-created-response.dto";
import { PassengerLoginResponseDto } from "../dto/passenger-login-response.dto";
import { PassengerRefreshTokenRequestDto } from "../dto/passenger-refresh-token-request.dto";
import { PassengerEntity } from "../entities/passenger.entity";
import { toPassengerDto } from "../mapper/passenger.mapper";
import { SmsSender } from "../remote/sms-sender";

const OTP_LENGTH = 4;

@Injectable()
export class AuthService {
    constructor(
        private readonly passengerDao: PassengerDao,
        private readonly passengerOtpDao: PassengerOtpDao,
        private readonly i18n: I18nService,
        private readonly cache: CacheService,
        private readonly config: ConfigService,
        private readonly jwt: JwtService,
        private readonly sms: SmsSender,
    ) {}

    /**
     * Genera y envía una OTP por SMS para autenticación por teléfono.
     * - Si el pasajero NO existe, se crea con estado INACTIVE_REGISTER.
     * - Rate limit en Redis: una OTP por pasajero cada OTP_RATE_TTL_SEC (60 s).
     * - Vigencia de la OTP: OTP_TTL_SEC (120 s).
     */
    async generateOtpByPhone(
        request: PassengerOtpCreatedRequestDto,
        lang?: string,
    ): Promise<PassengerOtpCreatedResponseDto> {
        const phone = request.phone.trim();

        let passenger = await this.passengerDao.findByPhoneNumber(phone);
        if (!passenger) {
            passenger = await this.passengerDao.createInactiveByPhone(phone);
        }

        const rateKey = this.rateKey(passenger.id);
        if (await this.cache.get<string>(rateKey)) {
            throw new HttpCustomException(
                await this.i18n.t("auth.activeCodeExists", { lang }),
            );
        }

        const code = String(randomInt(0, 10 ** OTP_LENGTH)).padStart(
            OTP_LENGTH,
            "0",
        );
        const ttlSec = this.numberConfig("OTP_TTL_SEC", 120);
        const expiresAt = new Date(Date.now() + ttlSec * 1000);

        const otp = await this.passengerOtpDao.createOtp(
            passenger.id,
            code,
            expiresAt,
        );

        const messageId = await this.sms.sendOtp(passenger.phoneNumber, code);
        if (!messageId) {
            throw new HttpCustomException(
                await this.i18n.t("auth.deliveryFailed", { lang }),
            );
        }

        await this.cache.set(
            rateKey,
            "1",
            this.numberConfig("OTP_RATE_TTL_SEC", 60),
        );
        await this.cache.del(this.attemptsKey(passenger.id));

        return {
            success: true,
            expiresAt: otp.expiresAt.toISOString(),
            ttlSec,
            messageId,
        };
    }

    /**
     * Valida la OTP y devuelve tokens de sesión + perfil.
     * - Máximo OTP_MAX_ATTEMPTS intentos fallidos por pasajero y ventana de OTP.
     * - La OTP debe estar vigente (no usada y no expirada) y se marca como usada.
     * - Toca lastLoginAt y emite access_token y refresh_token.
     */
    async verifyOtpByPhone(
        request: PassengerOtpValidatedRequestDto,
        lang?: string,
    ): Promise<PassengerLoginResponseDto> {
        const phone = request.phone.trim();
        const code = request.code.trim();

        const passenger = await this.passengerDao.findByPhoneNumber(phone);
        if (!passenger) {
            throw new HttpCustomException(
                await this.i18n.t("auth.passenger.notExists", { lang }),
            );
        }

        const attemptsKey = this.attemptsKey(passenger.id);
        const maxAttempts = this.numberConfig("OTP_MAX_ATTEMPTS", 5);
        const attempts = (await this.cache.get<number>(attemptsKey)) ?? 0;
        if (attempts >= maxAttempts) {
            throw new HttpCustomException(
                await this.i18n.t("auth.tooManyAttempts", { lang }),
                HttpStatus.TOO_MANY_REQUESTS,
            );
        }

        const otp = await this.passengerOtpDao.findValidOtp(passenger.id, code);
        if (!otp) {
            await this.cache.set(
                attemptsKey,
                attempts + 1,
                this.numberConfig("OTP_TTL_SEC", 120),
            );
            throw new HttpCustomException(
                await this.i18n.t("auth.invalidOrExpired", { lang }),
            );
        }

        const marked = await this.passengerOtpDao.markAsUsed(otp.id);
        if (!marked) {
            throw new HttpCustomException(
                await this.i18n.t("auth.alreadyUsed", { lang }),
            );
        }

        await this.passengerDao.touchLastLoginAtById(passenger.id);

        await this.cache.del(this.rateKey(passenger.id));
        await this.cache.del(attemptsKey);

        return this.issueSession(passenger);
    }

    /**
     * Renueva la sesión con un refresh token vigente.
     * - Verifica firma y expiración con JWT_REFRESH_SECRET.
     * - Solo acepta refresh tokens (rt: true) de pasajeros que aún existen.
     * - Devuelve un par nuevo: access token y refresh token (rotación).
     */
    async refreshTokens(
        request: PassengerRefreshTokenRequestDto,
        lang?: string,
    ): Promise<PassengerLoginResponseDto> {
        let payload: { sub?: string; typ?: string; rt?: boolean };
        try {
            payload = await this.jwt.verifyAsync(request.refreshToken, {
                secret: this.config.get<string>("JWT_REFRESH_SECRET"),
            });
        } catch {
            throw await this.invalidRefreshToken(lang);
        }

        if (!payload?.sub || payload.typ !== "passenger" || !payload.rt) {
            throw await this.invalidRefreshToken(lang);
        }

        const passenger = await this.passengerDao.findById(payload.sub);
        if (!passenger) {
            throw await this.invalidRefreshToken(lang);
        }

        return this.issueSession(passenger);
    }

    private async issueSession(
        passenger: PassengerEntity,
    ): Promise<PassengerLoginResponseDto> {
        const basePayload = {
            sub: passenger.id,
            typ: "passenger",
            phone: passenger.phoneNumber,
        };

        const accessToken = await this.jwt.signAsync(basePayload, {
            secret: this.config.get<string>("JWT_ACCESS_SECRET"),
            expiresIn: this.numberConfig("JWT_ACCESS_TTL_SEC", 900),
        });

        const refreshToken = await this.jwt.signAsync(
            { ...basePayload, rt: true },
            {
                secret: this.config.get<string>("JWT_REFRESH_SECRET"),
                expiresIn: this.numberConfig("JWT_REFRESH_TTL_SEC", 2592000),
            },
        );

        return {
            accessToken,
            refreshToken,
            user: toPassengerDto(passenger),
        };
    }

    private async invalidRefreshToken(
        lang?: string,
    ): Promise<HttpCustomException> {
        return new HttpCustomException(
            await this.i18n.t("auth.invalidRefreshToken", { lang }),
            HttpStatus.UNAUTHORIZED,
        );
    }

    private rateKey(passengerId: string): string {
        return `otp:passenger:${passengerId}:lock`;
    }

    private attemptsKey(passengerId: string): string {
        return `otp:passenger:${passengerId}:attempts`;
    }

    private numberConfig(key: string, fallback: number): number {
        const value = Number(this.config.get(key));
        return Number.isFinite(value) && value > 0 ? value : fallback;
    }
}
