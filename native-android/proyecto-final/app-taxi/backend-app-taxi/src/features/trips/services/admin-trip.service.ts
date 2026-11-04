import { HttpStatus, Injectable } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import { PageDto } from "src/core/http/dto/page.dto";
import { TripOfferDao } from "../dao/trip-offer.dao";
import { TripDao } from "../dao/trip.dao";
import { TripPageQueryDto } from "../dto/trip-request.dto";
import { AdminTripDetailDto, TripDto } from "../dto/trip.dto";
import { toOfferDto, toTripDto } from "../mapper/trip.mapper";
import { tripError } from "./trip-errors";
import { TripTrackService } from "./trip-track.service";

@Injectable()
export class AdminTripService {
    constructor(
        private readonly tripDao: TripDao,
        private readonly offerDao: TripOfferDao,
        private readonly track: TripTrackService,
        private readonly i18n: I18nService,
    ) {}

    async page(query: TripPageQueryDto): Promise<PageDto<TripDto>> {
        const [items, total] = await this.tripDao.findPage(
            query.status,
            query.page,
            query.size,
        );
        return {
            items: items.map(toTripDto),
            total,
            page: query.page,
            size: query.size,
        };
    }

    async active(): Promise<TripDto[]> {
        return (await this.tripDao.findActive()).map(toTripDto);
    }

    async detail(id: string, lang?: string): Promise<AdminTripDetailDto> {
        const trip = await this.tripDao.findById(id);
        if (!trip) {
            throw await tripError(
                this.i18n,
                "notFound",
                lang,
                HttpStatus.NOT_FOUND,
            );
        }
        const [offers, track] = await Promise.all([
            this.offerDao.findByTrip(id),
            this.track.points(id),
        ]);
        return {
            ...toTripDto(trip),
            offers: offers.map((offer) => toOfferDto(offer, trip.currency)),
            track,
        };
    }
}
