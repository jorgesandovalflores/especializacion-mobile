export const RealtimeEvents = {
    SessionReady: "session:ready",
    TripRequested: "trip:requested",
    TripOffer: "trip:offer",
    TripOfferAccepted: "trip:offer-accepted",
    TripUnavailable: "trip:unavailable",
    TripStatus: "trip:status",
    TripUpdated: "trip:updated",
    DriverLocation: "driver:location",
    DriverAvailability: "driver:availability",
} as const;

export const Rooms = {
    passenger: (id: string) => `passenger:${id}`,
    driver: (id: string) => `driver:${id}`,
    admins: "admins",
} as const;

export type RealtimeUserType = "passenger" | "driver" | "admin";

export interface RealtimeSession {
    userType: RealtimeUserType;
    userId: string;
}
