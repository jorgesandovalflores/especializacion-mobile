export enum TripStatus {
    REQUESTED = "REQUESTED",
    ACCEPTED = "ACCEPTED",
    ARRIVED = "ARRIVED",
    IN_PROGRESS = "IN_PROGRESS",
    COMPLETED = "COMPLETED",
    CANCELLED = "CANCELLED",
}

export const ACTIVE_TRIP_STATUSES = [
    TripStatus.REQUESTED,
    TripStatus.ACCEPTED,
    TripStatus.ARRIVED,
    TripStatus.IN_PROGRESS,
];

export const DRIVER_TRIP_STATUSES = [
    TripStatus.ACCEPTED,
    TripStatus.ARRIVED,
    TripStatus.IN_PROGRESS,
];

export const CANCELLABLE_TRIP_STATUSES = [
    TripStatus.REQUESTED,
    TripStatus.ACCEPTED,
    TripStatus.ARRIVED,
];

export const NEXT_DRIVER_STATUS: Partial<Record<TripStatus, TripStatus>> = {
    [TripStatus.ACCEPTED]: TripStatus.ARRIVED,
    [TripStatus.ARRIVED]: TripStatus.IN_PROGRESS,
    [TripStatus.IN_PROGRESS]: TripStatus.COMPLETED,
};

export enum TripCancelledBy {
    PASSENGER = "PASSENGER",
    DRIVER = "DRIVER",
}
