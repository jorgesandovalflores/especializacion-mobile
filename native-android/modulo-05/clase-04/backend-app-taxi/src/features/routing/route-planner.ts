import { GeoPoint } from "src/core/geo/geo";

export interface PlannedRoute {
    distanceMeters: number;
    durationSeconds: number;
    polyline: string;
}

export abstract class RoutePlanner {
    abstract route(from: GeoPoint, to: GeoPoint): Promise<PlannedRoute>;
}

export class RouteUnavailableError extends Error {}
