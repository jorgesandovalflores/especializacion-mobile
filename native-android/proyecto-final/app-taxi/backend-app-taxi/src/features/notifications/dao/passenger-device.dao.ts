import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { In, Repository } from "typeorm";
import { PassengerDeviceEntity } from "../entities/passenger-device.entity";
import { DevicePlatform } from "../enum/device-platform.enum";

@Injectable()
export class PassengerDeviceDao {
    constructor(
        @InjectRepository(PassengerDeviceEntity)
        private readonly repo: Repository<PassengerDeviceEntity>,
    ) {}

    /** Guarda el token o lo reasigna al pasajero si ya existía. */
    async upsert(
        passengerId: string,
        token: string,
        platform: DevicePlatform,
    ): Promise<void> {
        const existing = await this.repo.findOne({ where: { token } });
        if (existing) {
            await this.repo.update(
                { id: existing.id },
                { passengerId, platform },
            );
            return;
        }

        try {
            await this.repo.save(
                this.repo.create({ passengerId, token, platform }),
            );
        } catch (err: unknown) {
            // Condición de carrera: el mismo token llegó dos veces en paralelo
            if ((err as { code?: string })?.code === "ER_DUP_ENTRY") {
                await this.repo.update({ token }, { passengerId, platform });
                return;
            }
            throw err;
        }
    }

    /** Tokens de todos los dispositivos del pasajero. */
    async findTokensByPassengerId(passengerId: string): Promise<string[]> {
        const devices = await this.repo.find({
            where: { passengerId },
            select: { token: true },
        });
        return devices.map((device) => device.token);
    }

    /** Borra tokens que FCM ya no reconoce. */
    async deleteByTokens(tokens: string[]): Promise<void> {
        if (tokens.length === 0) return;
        await this.repo.delete({ token: In(tokens) });
    }
}
