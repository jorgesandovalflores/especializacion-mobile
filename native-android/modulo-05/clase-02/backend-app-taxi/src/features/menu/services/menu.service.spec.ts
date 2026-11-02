import { ConfigService } from "@nestjs/config";
import { CacheService } from "src/core/cache/cache.service";
import { MenuDao } from "../dao/menu.dao";
import { MenuEntity } from "../entities/menu.entity";
import { MenuApplication } from "../enum/menu-application.enum";
import { MenuStatus } from "../enum/menu-status.enum";
import { MenuService } from "./menu.service";

const buildMenu = (key: string, order: number): MenuEntity =>
    Object.assign(new MenuEntity(), {
        id: `menu-${order}`,
        key,
        text: key,
        icon: "home",
        deeplink: `app-taxi://passenger/${key}`,
        order,
        application: MenuApplication.PASSENGER,
        status: MenuStatus.ACTIVE,
    });

describe("MenuService", () => {
    let store: Map<string, unknown>;
    let cache: { get: jest.Mock; set: jest.Mock };
    let menuDao: jest.Mocked<Pick<MenuDao, "findActiveByApplication">>;
    let configValues: Record<string, string>;
    let service: MenuService;

    beforeEach(() => {
        store = new Map();
        configValues = {};
        cache = {
            get: jest.fn((key: string) =>
                Promise.resolve(store.get(key) ?? null),
            ),
            set: jest.fn((key: string, value: unknown) => {
                store.set(key, value);
                return Promise.resolve();
            }),
        };
        menuDao = {
            findActiveByApplication: jest
                .fn()
                .mockResolvedValue([
                    buildMenu("passenger_home", 1),
                    buildMenu("passenger_profile", 2),
                ]),
        };
        const config = { get: jest.fn((key: string) => configValues[key]) };

        service = new MenuService(
            menuDao as unknown as MenuDao,
            cache as unknown as CacheService,
            config as unknown as ConfigService,
        );
    });

    it("reads MySQL on a cache miss and stores the result for 60 s", async () => {
        const menus = await service.getActiveMenusByApplication(
            MenuApplication.PASSENGER,
        );

        expect(menus.map((m) => m.key)).toEqual([
            "passenger_home",
            "passenger_profile",
        ]);
        expect(menuDao.findActiveByApplication).toHaveBeenCalledWith(
            MenuApplication.PASSENGER,
        );
        expect(cache.set).toHaveBeenCalledWith(
            "menu:PASSENGER:active",
            menus,
            60,
        );
    });

    it("returns the cached list without querying MySQL", async () => {
        await service.getActiveMenusByApplication(MenuApplication.PASSENGER);
        menuDao.findActiveByApplication.mockClear();

        const menus = await service.getActiveMenusByApplication(
            MenuApplication.PASSENGER,
        );

        expect(menus).toHaveLength(2);
        expect(menuDao.findActiveByApplication).not.toHaveBeenCalled();
    });

    it("keeps one cache entry per application", async () => {
        await service.getActiveMenusByApplication(MenuApplication.PASSENGER);
        await service.getActiveMenusByApplication(MenuApplication.DRIVER);

        expect([...store.keys()]).toEqual([
            "menu:PASSENGER:active",
            "menu:DRIVER:active",
        ]);
    });

    it("uses MENU_CACHE_TTL_SEC when it is configured", async () => {
        configValues.MENU_CACHE_TTL_SEC = "300";

        await service.getActiveMenusByApplication(MenuApplication.PASSENGER);

        expect(cache.set).toHaveBeenCalledWith(
            "menu:PASSENGER:active",
            expect.any(Array),
            300,
        );
    });
});
