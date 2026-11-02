import { ConfigService } from "@nestjs/config";
import { OsrmRoutePlanner } from "./osrm-route-planner";
import { RouteUnavailableError } from "./route-planner";

const FROM = { lat: -12.0464, lng: -77.0428 };
const TO = { lat: -12.12, lng: -77.03 };

describe("OsrmRoutePlanner", () => {
    const fetchMock = jest.fn();
    let planner: OsrmRoutePlanner;

    beforeEach(() => {
        fetchMock.mockReset();
        global.fetch = fetchMock as unknown as typeof fetch;
        const config = {
            get: jest.fn((key: string) =>
                key === "OSRM_URL" ? "http://osrm:5000/" : undefined,
            ),
        };
        planner = new OsrmRoutePlanner(config as unknown as ConfigService);
    });

    it("asks OSRM for lng,lat pairs and maps the first route", async () => {
        fetchMock.mockResolvedValue({
            json: () =>
                Promise.resolve({
                    code: "Ok",
                    routes: [
                        { distance: 9838.4, duration: 786.5, geometry: "abc" },
                    ],
                }),
        });

        const route = await planner.route(FROM, TO);

        expect(fetchMock.mock.calls[0][0]).toBe(
            "http://osrm:5000/route/v1/driving/-77.0428,-12.0464;-77.03,-12.12" +
                "?overview=full&geometries=polyline&steps=false",
        );
        expect(route).toEqual({
            distanceMeters: 9838,
            durationSeconds: 787,
            polyline: "abc",
        });
    });

    it("fails with RouteUnavailableError when OSRM finds no route", async () => {
        fetchMock.mockResolvedValue({
            json: () => Promise.resolve({ code: "NoRoute", routes: [] }),
        });

        await expect(planner.route(FROM, TO)).rejects.toBeInstanceOf(
            RouteUnavailableError,
        );
    });

    it("fails with RouteUnavailableError when the network fails", async () => {
        fetchMock.mockRejectedValue(new Error("timeout"));

        await expect(planner.route(FROM, TO)).rejects.toBeInstanceOf(
            RouteUnavailableError,
        );
    });
});
