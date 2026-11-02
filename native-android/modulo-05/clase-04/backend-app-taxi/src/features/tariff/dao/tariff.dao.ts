import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { TariffEntity } from "../entities/tariff.entity";

export const TARIFF_ID = 1;

@Injectable()
export class TariffDao {
    constructor(
        @InjectRepository(TariffEntity)
        private readonly repo: Repository<TariffEntity>,
    ) {}

    async current(): Promise<TariffEntity | null> {
        return this.repo.findOne({ where: { id: TARIFF_ID } });
    }

    async save(values: Partial<TariffEntity>): Promise<TariffEntity> {
        await this.repo.save({ ...values, id: TARIFF_ID });
        return (await this.current())!;
    }
}
