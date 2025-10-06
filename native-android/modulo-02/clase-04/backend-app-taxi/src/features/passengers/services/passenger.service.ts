import { Injectable } from "@nestjs/common";
import { PassengerDao } from "../dao/passenger.dao";
import { I18nService } from "nestjs-i18n";
import { JwtService } from "@nestjs/jwt";
import { ConfigService } from "@nestjs/config";
import { CacheService } from "src/core/cache/cache.service";

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
        protected readonly cache: CacheService,
        private readonly jwt: JwtService,
        private readonly config: ConfigService,
    ) {}
}
