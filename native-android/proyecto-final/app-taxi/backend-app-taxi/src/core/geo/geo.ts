export interface GeoPoint {
    lat: number;
    lng: number;
}

const EARTH_RADIUS_METERS = 6_371_000;

const toRadians = (degrees: number) => (degrees * Math.PI) / 180;

export const distanceMeters = (from: GeoPoint, to: GeoPoint): number => {
    const dLat = toRadians(to.lat - from.lat);
    const dLng = toRadians(to.lng - from.lng);
    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos(toRadians(from.lat)) *
            Math.cos(toRadians(to.lat)) *
            Math.sin(dLng / 2) ** 2;
    return Math.round(
        2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)),
    );
};

export const round2 = (value: number): number =>
    Math.round((value + Number.EPSILON) * 100) / 100;
