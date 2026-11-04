import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { In, Not, Repository } from "typeorm";
import { TripOfferEntity } from "../entities/trip-offer.entity";
import { OfferStatus } from "../enum/offer-status.enum";

@Injectable()
export class TripOfferDao {
    constructor(
        @InjectRepository(TripOfferEntity)
        private readonly repo: Repository<TripOfferEntity>,
    ) {}

    async create(values: Partial<TripOfferEntity>): Promise<TripOfferEntity> {
        const saved = await this.repo.save(this.repo.create(values));
        return (await this.findById(saved.id))!;
    }

    async findById(id: string): Promise<TripOfferEntity | null> {
        return this.repo.findOne({
            where: { id },
            relations: { driver: true },
        });
    }

    async findByTrip(
        tripId: string,
        status?: OfferStatus,
    ): Promise<TripOfferEntity[]> {
        return this.repo.find({
            where: { tripId, ...(status ? { status } : {}) },
            relations: { driver: true },
            order: { createdAt: "ASC" },
        });
    }

    async exists(tripId: string, driverId: string): Promise<boolean> {
        return this.repo.exists({ where: { tripId, driverId } });
    }

    async offeredTripIds(
        driverId: string,
        tripIds: string[],
    ): Promise<Set<string>> {
        if (tripIds.length === 0) return new Set();
        const offers = await this.repo.find({
            select: { tripId: true },
            where: { driverId, tripId: In(tripIds) },
        });
        return new Set(offers.map((offer) => offer.tripId));
    }

    async accept(tripId: string, offerId: string): Promise<void> {
        await this.repo.update(
            { id: offerId, tripId },
            { status: OfferStatus.ACCEPTED },
        );
        await this.repo.update(
            { tripId, id: Not(offerId), status: OfferStatus.PENDING },
            { status: OfferStatus.REJECTED },
        );
    }

    async reject(offerId: string): Promise<void> {
        await this.repo.update(
            { id: offerId },
            { status: OfferStatus.REJECTED },
        );
    }

    async rejectAll(tripId: string): Promise<void> {
        await this.repo.update(
            { tripId, status: OfferStatus.PENDING },
            { status: OfferStatus.REJECTED },
        );
    }
}
