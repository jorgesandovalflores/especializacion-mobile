import { isPassengerAvatarUrl, passengerAvatarPath } from "./passenger-avatar";

const ID = "93facaba-726d-4dc7-9b42-e50bc1499e1b";
const urlFor = (path: string, host = "firebasestorage.googleapis.com") =>
    `https://${host}/v0/b/app-taxi.firebasestorage.app/o/${encodeURIComponent(path)}?alt=media&token=abc`;

describe("passenger-avatar", () => {
    it("builds the avatar path that storage.rules expects", () => {
        expect(passengerAvatarPath(ID)).toBe(`passengers/${ID}/avatar.jpg`);
    });

    it("accepts the download URL of the passenger avatar", () => {
        expect(isPassengerAvatarUrl(urlFor(passengerAvatarPath(ID)), ID)).toBe(
            true,
        );
    });

    it("rejects the avatar of another passenger", () => {
        expect(
            isPassengerAvatarUrl(urlFor(passengerAvatarPath("otro")), ID),
        ).toBe(false);
    });

    it("rejects other hosts and plain text", () => {
        expect(
            isPassengerAvatarUrl(
                urlFor(passengerAvatarPath(ID), "evil.example.com"),
                ID,
            ),
        ).toBe(false);
        expect(isPassengerAvatarUrl("no es una url", ID)).toBe(false);
    });
});
