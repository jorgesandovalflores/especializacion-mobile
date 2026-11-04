import { HttpStatus, Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import { I18nService } from "nestjs-i18n";
import * as argon2 from "argon2";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { DriverDao } from "../dao/driver.dao";
import { DriverLoginRequestDto } from "../dto/driver-login-request.dto";
import { DriverLoginResponseDto } from "../dto/driver-login-response.dto";
import { DriverRefreshRequestDto } from "../dto/driver-refresh-request.dto";
import { DriverEntity } from "../entities/driver.entity";
import { DriverStatus } from "../enum/driver-status.enum";
import { toDriverDto } from "../mapper/driver.mapper";

@Injectable()
export class DriverAuthService {
    constructor(
        private readonly driverDao: DriverDao,
        private readonly jwt: JwtService,
        private readonly config: ConfigService,
        private readonly i18n: I18nService,
    ) {}

    async login(
        request: DriverLoginRequestDto,
        lang?: string,
    ): Promise<DriverLoginResponseDto> {
        const driver = await this.driverDao.findByPhoneWithPassword(
            request.phone,
        );
        const valid =
            driver !== null &&
            (await argon2.verify(driver.passwordHash, request.password));
        if (!driver || !valid) {
            throw new HttpCustomException(
                await this.i18n.t("auth.invalidCredentials", { lang }),
                HttpStatus.UNAUTHORIZED,
            );
        }
        if (driver.status !== DriverStatus.ACTIVE) {
            throw new HttpCustomException(
                await this.i18n.t("driver.inactive", { lang }),
                HttpStatus.FORBIDDEN,
            );
        }

        await this.driverDao.touchLastLoginAt(driver.id);
        return this.issueSession(driver);
    }

    async refresh(
        request: DriverRefreshRequestDto,
        lang?: string,
    ): Promise<DriverLoginResponseDto> {
        let payload: { sub?: string; typ?: string; rt?: boolean };
        try {
            payload = await this.jwt.verifyAsync(request.refreshToken, {
                secret: this.config.get<string>("JWT_REFRESH_SECRET"),
            });
        } catch {
            throw await this.invalidRefreshToken(lang);
        }

        if (!payload?.sub || payload.typ !== "driver" || !payload.rt) {
            throw await this.invalidRefreshToken(lang);
        }

        const driver = await this.driverDao.findById(payload.sub);
        if (!driver || driver.status !== DriverStatus.ACTIVE) {
            throw await this.invalidRefreshToken(lang);
        }
        return this.issueSession(driver);
    }

    private async issueSession(
        driver: DriverEntity,
    ): Promise<DriverLoginResponseDto> {
        const payload = {
            sub: driver.id,
            typ: "driver",
            phone: driver.phoneNumber,
        };
        const accessToken = await this.jwt.signAsync(payload, {
            secret: this.config.get<string>("JWT_ACCESS_SECRET"),
            expiresIn: this.numberConfig("JWT_ACCESS_TTL_SEC", 900),
        });
        const refreshToken = await this.jwt.signAsync(
            { ...payload, rt: true },
            {
                secret: this.config.get<string>("JWT_REFRESH_SECRET"),
                expiresIn: this.numberConfig("JWT_REFRESH_TTL_SEC", 2592000),
            },
        );
        return { accessToken, refreshToken, driver: toDriverDto(driver) };
    }

    private async invalidRefreshToken(
        lang?: string,
    ): Promise<HttpCustomException> {
        return new HttpCustomException(
            await this.i18n.t("auth.invalidRefreshToken", { lang }),
            HttpStatus.UNAUTHORIZED,
        );
    }

    private numberConfig(key: string, fallback: number): number {
        const value = Number(this.config.get(key));
        return Number.isFinite(value) && value > 0 ? value : fallback;
    }
}
