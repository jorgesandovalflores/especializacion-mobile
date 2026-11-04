import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { In, IsNull, MoreThan, Repository } from "typeorm";
import { TripEntity } from "../entities/trip.entity";
import {
    ACTIVE_TRIP_STATUSES,
    DRIVER_TRIP_STATUSES,
    TripStatus,
} from "../enum/trip-status.enum";

const RELATIONS = { passenger: true, driver: true } as const;

@Injectable()
export class TripDao {
    constructor(
        @InjectRepository(TripEntity)
        private readonly repo: Repository<TripEntity>,
    ) {}

    async create(values: Partial<TripEntity>): Promise<TripEntity> {
        const saved = await this.repo.save(this.repo.create(values));
        return (await this.findById(saved.id))!;
    }

    async findById(id: string): Promise<TripEntity | null> {
        return this.repo.findOne({ where: { id }, relations: RELATIONS });
    }

    async findActiveByPassenger(
        passengerId: string,
    ): Promise<TripEntity | null> {
        return this.repo.findOne({
            where: { passengerId, status: In(ACTIVE_TRIP_STATUSES) },
            relations: RELATIONS,
            order: { requestedAt: "DESC" },
        });
    }

    async findActiveByDriver(driverId: string): Promise<TripEntity | null> {
        return this.repo.findOne({
            where: { driverId, status: In(DRIVER_TRIP_STATUSES) },
            relations: RELATIONS,
            order: { requestedAt: "DESC" },
        });
    }

    async findUnratedByPassenger(
        passengerId: string,
        since: Date,
    ): Promise<TripEntity | null> {
        return this.repo.findOne({
            where: {
                passengerId,
                status: TripStatus.COMPLETED,
                ratingByPassenger: IsNull(),
                completedAt: MoreThan(since),
            },
            relations: RELATIONS,
            order: { completedAt: "DESC" },
        });
    }

    async findUnratedByDriver(
        driverId: string,
        since: Date,
    ): Promise<TripEntity | null> {
        return this.repo.findOne({
            where: {
                driverId,
                status: TripStatus.COMPLETED,
                ratingByDriver: IsNull(),
                completedAt: MoreThan(since),
            },
            relations: RELATIONS,
            order: { completedAt: "DESC" },
        });
    }

    async findOpenRequests(since: Date): Promise<TripEntity[]> {
        return this.repo.find({
            where: {
                status: TripStatus.REQUESTED,
                requestedAt: MoreThan(since),
            },
            relations: RELATIONS,
            order: { requestedAt: "DESC" },
            take: 50,
        });
    }

    async findActive(): Promise<TripEntity[]> {
        return this.repo.find({
            where: { status: In(ACTIVE_TRIP_STATUSES) },
            relations: RELATIONS,
            order: { requestedAt: "DESC" },
        });
    }

    async findPage(
        status: TripStatus | undefined,
        page: number,
        size: number,
    ): Promise<[TripEntity[], number]> {
        return this.repo.findAndCount({
            where: status ? { status } : {},
            relations: RELATIONS,
            order: { requestedAt: "DESC" },
            skip: (page - 1) * size,
            take: size,
        });
    }

    async transition(
        id: string,
        from: TripStatus[],
        values: Partial<TripEntity>,
    ): Promise<boolean> {
        const result = await this.updateWhere(id, values)
            .andWhere("status IN (:...from)", { from })
            .execute();
        return (result.affected ?? 0) > 0;
    }

    async rateByPassenger(
        id: string,
        rating: number,
        comment: string | null,
    ): Promise<boolean> {
        const result = await this.updateWhere(id, {
            ratingByPassenger: rating,
            commentByPassenger: comment,
        })
            .andWhere("rating_by_passenger IS NULL")
            .execute();
        return (result.affected ?? 0) > 0;
    }

    async rateByDriver(
        id: string,
        rating: number,
        comment: string | null,
    ): Promise<boolean> {
        const result = await this.updateWhere(id, {
            ratingByDriver: rating,
            commentByDriver: comment,
        })
            .andWhere("rating_by_driver IS NULL")
            .execute();
        return (result.affected ?? 0) > 0;
    }

    private updateWhere(id: string, values: Partial<TripEntity>) {
        return this.repo
            .createQueryBuilder()
            .update(TripEntity)
            .set(values)
            .where("id_trip = :id", { id });
    }
}
