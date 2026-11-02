import { Injectable, Logger } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { GeoPoint } from "src/core/geo/geo";
import {
    PlannedRoute,
    RoutePlanner,
    RouteUnavailableError,
} from "./route-planner";

interface OsrmResponse {
    code: string;
    routes?: Array<{ distance: number; duration: number; geometry: string }>;
}

const DEFAULT_OSRM_URL = "https://router.project-osrm.org";

@Injectable()
export class OsrmRoutePlanner extends RoutePlanner {
    private readonly logger = new Logger(OsrmRoutePlanner.name);

    constructor(private readonly config: ConfigService) {
        super();
    }

    async route(from: GeoPoint, to: GeoPoint): Promise<PlannedRoute> {
        const baseUrl = (
            this.config.get<string>("OSRM_URL") || DEFAULT_OSRM_URL
        ).replace(/\/$/, "");
        const timeoutMs = Number(this.config.get("OSRM_TIMEOUT_MS")) || 8000;
        const url =
            `${baseUrl}/route/v1/driving/${from.lng},${from.lat};${to.lng},${to.lat}` +
            "?overview=full&geometries=polyline&steps=false";

        let body: OsrmResponse;
        try {
            const response = await fetch(url, {
                signal: AbortSignal.timeout(timeoutMs),
            });
            body = (await response.json()) as OsrmResponse;
        } catch (error) {
            this.logger.warn(`OSRM request failed: ${String(error)}`);
            throw new RouteUnavailableError("OSRM request failed");
        }

        const route = body.routes?.[0];
        if (body.code !== "Ok" || !route) {
            this.logger.warn(`OSRM answered ${body.code}`);
            throw new RouteUnavailableError(body.code);
        }
        return {
            distanceMeters: Math.round(route.distance),
            durationSeconds: Math.round(route.duration),
            polyline: route.geometry,
        };
    }
}
