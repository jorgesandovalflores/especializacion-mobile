import { Injectable } from "@nestjs/common";
import { PassengerDao } from "../dao/passenger.dao";
import { toPassengerDto } from "../mapper/passenger.mapper";
import { I18nService } from "nestjs-i18n";
import { JwtService } from "@nestjs/jwt";
import { ConfigService } from "@nestjs/config";
import { PassengerLoginResponseDto } from "../dto/passenger-login-response.dto";
import { PassengerLoginRequestDto } from "../dto/passenger-login-request.dto";
import { HttpCustomException } from "src/core/http/exception/http.exception";

/* -------------------------------------------------------
   Service: login por phone con emisión de JWTs
   - Actualiza lastLoginAt
   - Firma access_token y refresh_token
   - Devuelve DTO con usuario + tokens
-------------------------------------------------------- */
@Injectable()
export class PassengerService {
    constructor(
        private readonly passengerDao: PassengerDao,
        protected readonly i18n: I18nService,
        private readonly jwt: JwtService,
        private readonly config: ConfigService,
    ) {}

    /** Login por phone: busca, toca lastLoginAt y retorna tokens + user. */
    async loginByPhone(
        request: PassengerLoginRequestDto,
        lang?: string,
    ): Promise<PassengerLoginResponseDto> {
        const normalized = request.phone.trim();

        // 1) Buscar pasajero por phone
        const passenger = await this.passengerDao.findByPhoneNumber(normalized);
        if (!passenger) {
            throw new HttpCustomException(
                await this.i18n.t("passenger.notExists", { lang }),
            );
        }

        // 2) Actualizar last_login_at (verificar afectación)
        await this.passengerDao.touchLastLoginAtById(passenger.id);

        // 3) Emitir tokens (JWT)
        const iat = Math.floor(Date.now() / 1000);

        // TTLs (segundos) y secretos desde configuración
        const accessTtlSec = Number(this.config.get("JWT_ACCESS_TTL_SEC"));
        const refreshTtlSec = Number(this.config.get("JWT_REFRESH_TTL_SEC"));
        const accessSecret = this.config.get<string>("JWT_ACCESS_SECRET");
        const refreshSecret = this.config.get<string>("JWT_REFRESH_SECRET");

        // Payload mínimo recomendado (no incluir data sensible)
        const basePayload = {
            sub: passenger.id,
            typ: "passenger",
            phone: passenger.phoneNumber,
        };

        const accessToken = await this.jwt.signAsync(basePayload, {
            secret: accessSecret,
            expiresIn: accessTtlSec, // en segundos
        });

        const refreshToken = await this.jwt.signAsync(
            { ...basePayload, rt: true }, // flag para distinguir refresh tokens
            {
                secret: refreshSecret,
                expiresIn: refreshTtlSec,
            },
        );

        // 4) Armar respuesta
        return {
            accessToken,
            refreshToken,
            user: toPassengerDto(passenger),
        };
    }
}
