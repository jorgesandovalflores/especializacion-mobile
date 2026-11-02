import { Injectable } from "@nestjs/common";
import { round2 } from "src/core/geo/geo";
import { TariffDao } from "../dao/tariff.dao";
import { TariffDto, TariffUpdateRequestDto } from "../dto/tariff.dto";
import { TariffEntity } from "../entities/tariff.entity";

export interface Fare {
    amount: number;
    currency: string;
}

const DEFAULT_TARIFF = { baseFare: 5, pricePerKm: 1.5, currency: "PEN" };

export const calculateFare = (
    tariff: Pick<TariffEntity, "baseFare" | "pricePerKm">,
    distanceMeters: number,
): number =>
    round2(tariff.baseFare + (tariff.pricePerKm * distanceMeters) / 1000);

@Injectable()
export class TariffService {
    constructor(private readonly tariffDao: TariffDao) {}

    async current(): Promise<TariffDto> {
        return this.toDto(await this.load());
    }

    async update(
        adminId: string,
        request: TariffUpdateRequestDto,
    ): Promise<TariffDto> {
        const saved = await this.tariffDao.save({
            baseFare: request.baseFare,
            pricePerKm: request.pricePerKm,
            currency: DEFAULT_TARIFF.currency,
            updatedBy: adminId,
        });
        return this.toDto(saved);
    }

    async fareFor(distanceMeters: number): Promise<Fare> {
        const tariff = await this.load();
        return {
            amount: calculateFare(tariff, distanceMeters),
            currency: tariff.currency,
        };
    }

    private async load(): Promise<TariffEntity> {
        return (
            (await this.tariffDao.current()) ??
            Object.assign(new TariffEntity(), {
                ...DEFAULT_TARIFF,
                updatedAt: new Date(0),
            })
        );
    }

    private toDto(entity: TariffEntity): TariffDto {
        return {
            baseFare: entity.baseFare,
            pricePerKm: entity.pricePerKm,
            currency: entity.currency,
            updatedAt: entity.updatedAt.toISOString(),
        };
    }
}
