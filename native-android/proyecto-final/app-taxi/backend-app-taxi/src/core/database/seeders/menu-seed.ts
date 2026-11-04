import * as path from "path";
import { promises as fs } from "fs";
import { AppDataSource } from "../typeorm.migration";
import { MenuEntity } from "src/features/menu/entities/menu.entity";
import { MenuApplication } from "src/features/menu/enum/menu-application.enum";
import { MenuStatus } from "src/features/menu/enum/menu-status.enum";

async function resolveMenusFile(): Promise<string> {
    const candidates = [
        path.resolve(__dirname, "data/menus.json"),
        path.resolve(
            process.cwd(),
            "src/core/database/seeders/data/menus.json",
        ),
        path.resolve(
            process.cwd(),
            "dist/core/database/seeders/data/menus.json",
        ),
    ];

    for (const p of candidates) {
        try {
            await fs.access(p);
            return p;
        } catch {}
    }

    throw new Error("Menus seed file not found (menus.json).");
}

export async function seedMenus() {
    await AppDataSource.initialize();
    const runner = AppDataSource.createQueryRunner();
    await runner.connect();
    await runner.startTransaction();

    try {
        const filePath = await resolveMenusFile();
        const raw = await fs.readFile(filePath, "utf-8");
        const menus: Array<{
            key: string;
            text: string;
            icon: string;
            deeplink: string;
            order: number;
            application: string;
            status: string;
        }> = JSON.parse(raw);

        const repo = runner.manager.getRepository(MenuEntity);

        for (const item of menus) {
            const exists = await repo.findOne({ where: { key: item.key } });
            if (exists) {
                console.log(`Menú ya existía: ${exists.key}`);
                continue;
            }
            await repo.insert({
                key: item.key,
                text: item.text,
                icon: item.icon,
                deeplink: item.deeplink,
                order: item.order,
                application: item.application as MenuApplication,
                status: item.status as MenuStatus,
            });

            console.log(`Menú registrado: ${item.key}`);
        }

        await runner.commitTransaction();
        console.log("Seed de menus completado - OK");
    } catch (err) {
        await runner.rollbackTransaction();
        console.error("Error en seed de menus - FAILED", err);
        process.exitCode = 1;
    } finally {
        await runner.release();
        await AppDataSource.destroy();
    }
}
