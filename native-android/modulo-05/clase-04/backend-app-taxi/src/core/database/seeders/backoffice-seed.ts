import * as path from "path";
import { promises as fs } from "fs";
import * as argon2 from "argon2";
import { AppDataSource } from "../typeorm.migration";
import { AdminEntity } from "src/features/admins/entities/admin.entity";
import { AdminStatus } from "src/features/admins/enum/admin-status.enum";
import { DriverEntity } from "src/features/drivers/entities/driver.entity";
import { DriverStatus } from "src/features/drivers/enum/driver-status.enum";
import { TariffEntity } from "src/features/tariff/entities/tariff.entity";
import { TARIFF_ID } from "src/features/tariff/dao/tariff.dao";

interface DriverSeed {
    phoneNumber: string;
    givenName: string;
    familyName: string;
    email: string;
    vehiclePlate: string;
    vehicleBrand: string;
    vehicleModel: string;
    vehicleColor: string;
}

async function resolveDriversFile(): Promise<string> {
    const candidates = [
        path.resolve(__dirname, "data/drivers.json"),
        path.resolve(
            process.cwd(),
            "src/core/database/seeders/data/drivers.json",
        ),
        path.resolve(
            process.cwd(),
            "dist/core/database/seeders/data/drivers.json",
        ),
    ];
    for (const candidate of candidates) {
        try {
            await fs.access(candidate);
            return candidate;
        } catch {
            continue;
        }
    }
    throw new Error("drivers.json seed file not found");
}

export async function seedBackoffice() {
    await AppDataSource.initialize();
    const runner = AppDataSource.createQueryRunner();
    await runner.connect();
    await runner.startTransaction();

    try {
        const admins = runner.manager.getRepository(AdminEntity);
        const adminEmail = process.env.SEED_ADMIN_EMAIL || "admin@apptaxi.pe";
        if (
            !(await admins.exists({
                where: { email: adminEmail },
                withDeleted: true,
            }))
        ) {
            await admins.insert({
                fullName: "Administrador App Taxi",
                email: adminEmail,
                passwordHash: await argon2.hash(
                    process.env.SEED_ADMIN_PASSWORD || "Admin123*",
                ),
                status: AdminStatus.ACTIVE,
            });
            console.log(`Admin registrado: ${adminEmail}`);
        }

        const drivers = runner.manager.getRepository(DriverEntity);
        const seeds = JSON.parse(
            await fs.readFile(await resolveDriversFile(), "utf-8"),
        ) as DriverSeed[];
        const driverPassword = await argon2.hash(
            process.env.SEED_DRIVER_PASSWORD || "Driver123*",
        );
        for (const seed of seeds) {
            if (
                await drivers.exists({
                    where: { phoneNumber: seed.phoneNumber },
                    withDeleted: true,
                })
            ) {
                continue;
            }
            await drivers.insert({
                ...seed,
                passwordHash: driverPassword,
                status: DriverStatus.ACTIVE,
            });
            console.log(`Conductor registrado: ${seed.phoneNumber}`);
        }

        const tariffs = runner.manager.getRepository(TariffEntity);
        if (!(await tariffs.exists({ where: { id: TARIFF_ID } }))) {
            await tariffs.insert({
                id: TARIFF_ID,
                baseFare: 5,
                pricePerKm: 1.5,
                currency: "PEN",
            });
            console.log("Tarifa inicial registrada");
        }

        await runner.commitTransaction();
        console.log("Seed de backoffice completado - OK");
    } catch (err) {
        await runner.rollbackTransaction();
        console.error("Error en seed de backoffice - FAILED", err);
        process.exitCode = 1;
    } finally {
        await runner.release();
        await AppDataSource.destroy();
    }
}
