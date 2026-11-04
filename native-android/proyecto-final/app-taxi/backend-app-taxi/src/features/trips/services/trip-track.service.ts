import { Injectable } from "@nestjs/common";
import { CacheService } from "src/core/cache/cache.service";
import { TrackPointDto } from "../dto/trip.dto";

const TRACK_TTL_SEC = 7 * 24 * 3600;
const trackKey = (tripId: string) => `trip:${tripId}:track`;

@Injectable()
export class TripTrackService {
    constructor(private readonly cache: CacheService) {}

    async append(tripId: string, point: TrackPointDto): Promise<void> {
        await this.cache.rPush(trackKey(tripId), point);
        await this.cache.expire(trackKey(tripId), TRACK_TTL_SEC);
    }

    async points(tripId: string): Promise<TrackPointDto[]> {
        return this.cache.lRange<TrackPointDto>(trackKey(tripId));
    }
}
