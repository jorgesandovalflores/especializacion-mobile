import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Brackets, In, Not, Repository } from "typeorm";
import { DriverEntity } from "../entities/driver.entity";

export type DriverUniqueField = "phoneNumber" | "email" | "vehiclePlate";

@Injectable()
export class DriverDao {
    constructor(
        @InjectRepository(DriverEntity)
        private readonly repo: Repository<DriverEntity>,
    ) {}

    async findById(id: string): Promise<DriverEntity | null> {
        return this.repo.findOne({ where: { id } });
    }

    async findByIds(ids: string[]): Promise<DriverEntity[]> {
        if (ids.length === 0) return [];
        return this.repo.find({ where: { id: In(ids) } });
    }

    async findByPhoneWithPassword(
        phoneNumber: string,
    ): Promise<DriverEntity | null> {
        return this.repo
            .createQueryBuilder("driver")
            .addSelect("driver.passwordHash")
            .where("driver.phoneNumber = :phoneNumber", { phoneNumber })
            .getOne();
    }

    async findPage(
        search: string | undefined,
        page: number,
        size: number,
    ): Promise<[DriverEntity[], number]> {
        const query = this.repo
            .createQueryBuilder("driver")
            .orderBy("driver.createdAt", "DESC")
            .skip((page - 1) * size)
            .take(size);
        if (search) {
            const term = `%${search}%`;
            query.where(
                new Brackets((where) =>
                    where
                        .where("driver.givenName LIKE :term", { term })
                        .orWhere("driver.familyName LIKE :term", { term })
                        .orWhere("driver.phoneNumber LIKE :term", { term })
                        .orWhere("driver.vehiclePlate LIKE :term", { term }),
                ),
            );
        }
        return query.getManyAndCount();
    }

    async findTakenField(
        values: Partial<Record<DriverUniqueField, string | null | undefined>>,
        excludeId?: string,
    ): Promise<DriverUniqueField | null> {
        for (const field of [
            "phoneNumber",
            "email",
            "vehiclePlate",
        ] as DriverUniqueField[]) {
            const value = values[field];
            if (!value) continue;
            const taken = await this.repo.exists({
                where: {
                    [field]: value,
                    ...(excludeId ? { id: Not(excludeId) } : {}),
                },
                withDeleted: true,
            });
            if (taken) return field;
        }
        return null;
    }

    async create(values: Partial<DriverEntity>): Promise<DriverEntity> {
        const saved = await this.repo.save(this.repo.create(values));
        return (await this.findById(saved.id))!;
    }

    async update(
        id: string,
        values: Partial<DriverEntity>,
    ): Promise<DriverEntity | null> {
        await this.repo.update({ id }, values);
        return this.findById(id);
    }

    async softDelete(id: string): Promise<boolean> {
        const result = await this.repo.softDelete({ id });
        return (result.affected ?? 0) > 0;
    }

    async touchLastLoginAt(id: string): Promise<void> {
        await this.repo.update({ id }, { lastLoginAt: new Date() });
    }

    async addRating(id: string, rating: number): Promise<void> {
        await this.repo
            .createQueryBuilder()
            .update(DriverEntity)
            .set({
                ratingAvg: () =>
                    "ROUND((rating_avg * rating_count + :rating) / (rating_count + 1), 2)",
                ratingCount: () => "rating_count + 1",
            })
            .setParameter("rating", rating)
            .where("id_driver = :id", { id })
            .execute();
    }
}
