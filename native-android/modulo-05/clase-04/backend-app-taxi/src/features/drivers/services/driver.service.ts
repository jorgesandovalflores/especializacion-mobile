import { HttpStatus, Injectable } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import * as argon2 from "argon2";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { PageDto, PageQueryDto } from "src/core/http/dto/page.dto";
import { RealtimeEmitter } from "src/core/realtime/realtime-emitter.service";
import { RealtimeEvents, Rooms } from "src/core/realtime/realtime.events";
import { DriverDao, DriverUniqueField } from "../dao/driver.dao";
import { AdminDriverDto, DriverDto } from "../dto/driver.dto";
import {
    DriverAvailabilityRequestDto,
    DriverAvailabilityResponseDto,
} from "../dto/driver-availability-request.dto";
import {
    DriverCreateRequestDto,
    DriverUpdateRequestDto,
} from "../dto/driver-save-request.dto";
import { DriverEntity } from "../entities/driver.entity";
import { DriverStatus } from "../enum/driver-status.enum";
import { toDriverDto } from "../mapper/driver.mapper";
import { DriverPresenceService } from "./driver-presence.service";

export interface DriverLocationView {
    driverId: string;
    tripId: string | null;
    givenName: string;
    familyName: string;
    plate: string;
    online: boolean;
    lat: number;
    lng: number;
    bearing: number | null;
    speed: number | null;
    at: string;
}

const TAKEN_KEYS: Record<DriverUniqueField, string> = {
    phoneNumber: "driver.phoneTaken",
    email: "driver.emailTaken",
    vehiclePlate: "driver.plateTaken",
};

@Injectable()
export class DriverService {
    constructor(
        private readonly driverDao: DriverDao,
        private readonly presence: DriverPresenceService,
        private readonly realtime: RealtimeEmitter,
        private readonly i18n: I18nService,
    ) {}

    async me(driverId: string, lang?: string): Promise<DriverDto> {
        return toDriverDto(await this.requireDriver(driverId, lang));
    }

    async setAvailability(
        driverId: string,
        request: DriverAvailabilityRequestDto,
        lang?: string,
    ): Promise<DriverAvailabilityResponseDto> {
        await this.requireDriver(driverId, lang);

        if (!request.online && (await this.presence.activeTrip(driverId))) {
            throw new HttpCustomException(
                await this.i18n.t("driver.offlineWithTrip", { lang }),
            );
        }

        if (request.lat !== undefined && request.lng !== undefined) {
            await this.presence.saveLocation(driverId, {
                lat: request.lat,
                lng: request.lng,
                bearing: null,
                speed: null,
                accuracy: null,
                at: new Date().toISOString(),
            });
        }
        await this.presence.setOnline(driverId, request.online);
        this.realtime.emit(Rooms.admins, RealtimeEvents.DriverAvailability, {
            driverId,
            online: request.online,
        });
        return { online: request.online };
    }

    async page(query: PageQueryDto): Promise<PageDto<AdminDriverDto>> {
        const [entities, total] = await this.driverDao.findPage(
            query.search,
            query.page,
            query.size,
        );
        const online = new Set(await this.presence.onlineIds());
        return {
            items: entities.map((entity) => ({
                ...toDriverDto(entity),
                online: online.has(entity.id),
            })),
            total,
            page: query.page,
            size: query.size,
        };
    }

    async findOne(id: string, lang?: string): Promise<DriverDto> {
        return toDriverDto(await this.requireDriver(id, lang));
    }

    async create(
        request: DriverCreateRequestDto,
        lang?: string,
    ): Promise<DriverDto> {
        await this.assertUnique(request, undefined, lang);
        const driver = await this.driverDao.create({
            ...this.toColumns(request),
            passwordHash: await argon2.hash(request.password),
            status: DriverStatus.ACTIVE,
        });
        return toDriverDto(driver);
    }

    async update(
        id: string,
        request: DriverUpdateRequestDto,
        lang?: string,
    ): Promise<DriverDto> {
        await this.requireDriver(id, lang);
        await this.assertUnique(request, id, lang);
        const driver = await this.driverDao.update(id, {
            ...this.toColumns(request),
            status: request.status,
            ...(request.password
                ? { passwordHash: await argon2.hash(request.password) }
                : {}),
        });
        if (request.status === DriverStatus.INACTIVE) {
            await this.presence.setOnline(id, false);
        }
        return toDriverDto(driver!);
    }

    async remove(id: string, lang?: string): Promise<void> {
        await this.requireDriver(id, lang);
        if (await this.presence.activeTrip(id)) {
            throw new HttpCustomException(
                await this.i18n.t("driver.deleteWithTrip", { lang }),
            );
        }
        await this.presence.setOnline(id, false);
        await this.driverDao.softDelete(id);
    }

    async locations(): Promise<DriverLocationView[]> {
        const [positions, onlineIds] = await Promise.all([
            this.presence.locations(),
            this.presence.onlineIds(),
        ]);
        const online = new Set(onlineIds);
        const drivers = await this.driverDao.findByIds(Object.keys(positions));

        return Promise.all(
            drivers.map(async (driver) => {
                const position = positions[driver.id];
                const trip = await this.presence.activeTrip(driver.id);
                return {
                    driverId: driver.id,
                    tripId: trip?.tripId ?? null,
                    givenName: driver.givenName,
                    familyName: driver.familyName,
                    plate: driver.vehiclePlate,
                    online: online.has(driver.id),
                    lat: position.lat,
                    lng: position.lng,
                    bearing: position.bearing,
                    speed: position.speed,
                    at: position.at,
                };
            }),
        );
    }

    private toColumns(
        request: DriverCreateRequestDto | DriverUpdateRequestDto,
    ): Partial<DriverEntity> {
        return {
            phoneNumber: request.phone,
            givenName: request.givenName,
            familyName: request.familyName,
            email: request.email ?? null,
            vehiclePlate: request.vehicle.plate,
            vehicleBrand: request.vehicle.brand,
            vehicleModel: request.vehicle.model,
            vehicleColor: request.vehicle.color,
        };
    }

    private async assertUnique(
        request: DriverCreateRequestDto | DriverUpdateRequestDto,
        excludeId: string | undefined,
        lang?: string,
    ): Promise<void> {
        const taken = await this.driverDao.findTakenField(
            {
                phoneNumber: request.phone,
                email: request.email,
                vehiclePlate: request.vehicle.plate,
            },
            excludeId,
        );
        if (taken) {
            throw new HttpCustomException(
                await this.i18n.t(TAKEN_KEYS[taken], { lang }),
            );
        }
    }

    private async requireDriver(
        id: string,
        lang?: string,
    ): Promise<DriverEntity> {
        const driver = await this.driverDao.findById(id);
        if (!driver) {
            throw new HttpCustomException(
                await this.i18n.t("driver.notFound", { lang }),
                HttpStatus.NOT_FOUND,
            );
        }
        return driver;
    }
}
