import { HttpStatus, Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import { I18nService } from "nestjs-i18n";
import * as argon2 from "argon2";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { AdminDao } from "../dao/admin.dao";
import { AdminDto, AdminLoginResponseDto } from "../dto/admin.dto";
import {
    AdminCreateRequestDto,
    AdminLoginRequestDto,
    AdminUpdateRequestDto,
} from "../dto/admin-request.dto";
import { AdminEntity } from "../entities/admin.entity";
import { AdminStatus } from "../enum/admin-status.enum";
import { toAdminDto } from "../mapper/admin.mapper";

@Injectable()
export class AdminService {
    constructor(
        private readonly adminDao: AdminDao,
        private readonly jwt: JwtService,
        private readonly config: ConfigService,
        private readonly i18n: I18nService,
    ) {}

    async login(
        request: AdminLoginRequestDto,
        lang?: string,
    ): Promise<AdminLoginResponseDto> {
        const admin = await this.adminDao.findByEmailWithPassword(
            request.email,
        );
        const valid =
            admin !== null &&
            admin.status === AdminStatus.ACTIVE &&
            (await argon2.verify(admin.passwordHash, request.password));
        if (!admin || !valid) {
            throw new HttpCustomException(
                await this.i18n.t("auth.invalidCredentials", { lang }),
                HttpStatus.UNAUTHORIZED,
            );
        }

        await this.adminDao.touchLastLoginAt(admin.id);
        const ttl = Number(this.config.get("ADMIN_JWT_TTL_SEC"));
        const accessToken = await this.jwt.signAsync(
            { sub: admin.id, typ: "admin" },
            {
                secret: this.config.get<string>("JWT_ACCESS_SECRET"),
                expiresIn: Number.isFinite(ttl) && ttl > 0 ? ttl : 28800,
            },
        );
        const refreshed = await this.adminDao.findById(admin.id);
        return { accessToken, admin: toAdminDto(refreshed!) };
    }

    async me(id: string, lang?: string): Promise<AdminDto> {
        return toAdminDto(await this.requireAdmin(id, lang));
    }

    async findAll(): Promise<AdminDto[]> {
        return (await this.adminDao.findAll()).map(toAdminDto);
    }

    async create(
        request: AdminCreateRequestDto,
        lang?: string,
    ): Promise<AdminDto> {
        await this.assertEmailFree(request.email, undefined, lang);
        const admin = await this.adminDao.create({
            fullName: request.fullName,
            email: request.email,
            passwordHash: await argon2.hash(request.password),
            status: AdminStatus.ACTIVE,
        });
        return toAdminDto(admin);
    }

    async update(
        currentAdminId: string,
        id: string,
        request: AdminUpdateRequestDto,
        lang?: string,
    ): Promise<AdminDto> {
        await this.requireAdmin(id, lang);
        await this.assertEmailFree(request.email, id, lang);
        if (id === currentAdminId && request.status !== AdminStatus.ACTIVE) {
            throw new HttpCustomException(
                await this.i18n.t("admin.selfDeactivate", { lang }),
            );
        }
        const admin = await this.adminDao.update(id, {
            fullName: request.fullName,
            email: request.email,
            status: request.status,
            ...(request.password
                ? { passwordHash: await argon2.hash(request.password) }
                : {}),
        });
        return toAdminDto(admin!);
    }

    async remove(
        currentAdminId: string,
        id: string,
        lang?: string,
    ): Promise<void> {
        if (id === currentAdminId) {
            throw new HttpCustomException(
                await this.i18n.t("admin.selfDelete", { lang }),
            );
        }
        await this.requireAdmin(id, lang);
        await this.adminDao.softDelete(id);
    }

    private async assertEmailFree(
        email: string,
        excludeId: string | undefined,
        lang?: string,
    ): Promise<void> {
        if (await this.adminDao.isEmailTaken(email, excludeId)) {
            throw new HttpCustomException(
                await this.i18n.t("admin.emailTaken", { lang }),
            );
        }
    }

    private async requireAdmin(
        id: string,
        lang?: string,
    ): Promise<AdminEntity> {
        const admin = await this.adminDao.findById(id);
        if (!admin) {
            throw new HttpCustomException(
                await this.i18n.t("admin.notFound", { lang }),
                HttpStatus.NOT_FOUND,
            );
        }
        return admin;
    }
}
