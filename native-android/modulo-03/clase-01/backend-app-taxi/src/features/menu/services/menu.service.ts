import { Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { CacheService } from "src/core/cache/cache.service";

import { MenuDao } from "../dao/menu.dao";
import { MenuDto } from "../dto/menu.dto";
import { MenuApplication } from "../enum/menu-application.enum";
import { toMenuDto } from "../mapper/menu.mapper";

@Injectable()
export class MenuService {
    constructor(
        private readonly menuDao: MenuDao,
        private readonly cache: CacheService,
        private readonly config: ConfigService,
    ) {}

    /**
     * Lista los menús activos de una aplicación, ordenados por `order`.
     * - Primero intenta Redis (`menu:{application}:active`).
     * - Si no hay caché, consulta MySQL y la guarda por MENU_CACHE_TTL_SEC (60 s).
     */
    async getActiveMenusByApplication(
        application: MenuApplication,
    ): Promise<MenuDto[]> {
        const cacheKey = this.cacheKey(application);

        const cached = await this.cache.get<MenuDto[]>(cacheKey);
        if (Array.isArray(cached)) {
            return cached;
        }

        const entities =
            await this.menuDao.findActiveByApplication(application);
        const menus = entities.map(toMenuDto);

        await this.cache.set(
            cacheKey,
            menus,
            this.numberConfig("MENU_CACHE_TTL_SEC", 60),
        );

        return menus;
    }

    private cacheKey(application: MenuApplication): string {
        return `menu:${application}:active`;
    }

    private numberConfig(key: string, fallback: number): number {
        const value = Number(this.config.get(key));
        return Number.isFinite(value) && value > 0 ? value : fallback;
    }
}
