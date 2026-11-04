import { calculateFare } from "./tariff.service";

describe("calculateFare", () => {
    it("adds the base fare to the price per kilometer", () => {
        expect(calculateFare({ baseFare: 5, pricePerKm: 1.5 }, 9838)).toBe(
            19.76,
        );
    });

    it("charges only the base fare for a zero distance", () => {
        expect(calculateFare({ baseFare: 5, pricePerKm: 1.5 }, 0)).toBe(5);
    });

    it("rounds to two decimals", () => {
        expect(calculateFare({ baseFare: 4.5, pricePerKm: 1.33 }, 1001)).toBe(
            5.83,
        );
    });
});
