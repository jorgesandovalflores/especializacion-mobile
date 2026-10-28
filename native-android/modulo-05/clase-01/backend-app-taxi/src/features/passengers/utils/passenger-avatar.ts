const FIREBASE_STORAGE_HOST = "firebasestorage.googleapis.com";

/** Ruta del avatar en Firebase Storage. storage.rules exige el mismo patrón. */
export const passengerAvatarPath = (passengerId: string): string =>
    `passengers/${passengerId}/avatar.jpg`;

/** true si la URL es una descarga de Firebase Storage del avatar de ese pasajero. */
export const isPassengerAvatarUrl = (
    url: string,
    passengerId: string,
): boolean => {
    let parsed: URL;
    try {
        parsed = new URL(url);
    } catch {
        return false;
    }
    return (
        parsed.protocol === "https:" &&
        parsed.host === FIREBASE_STORAGE_HOST &&
        parsed.pathname.endsWith(
            `/o/${encodeURIComponent(passengerAvatarPath(passengerId))}`,
        )
    );
};
