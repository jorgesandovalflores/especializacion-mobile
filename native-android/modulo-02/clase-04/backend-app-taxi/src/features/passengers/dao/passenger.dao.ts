import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { PassengerEntity } from "../entities/passenger.entity";
import { PassengerStatus } from "../enum/passenger-status.enum";

@Injectable()
export class PassengerDao {
    constructor(
        @InjectRepository(PassengerEntity)
        private readonly repo: Repository<PassengerEntity>,
    ) {}

    /** Busca por phone_number (sin incluir borrados lógicos de forma explícita). */
    async findByPhoneNumber(
        phoneNumber: string,
    ): Promise<PassengerEntity | null> {
        const normalized = phoneNumber.trim();
        return this.repo.findOne({ where: { phoneNumber: normalized } });
    }

    /** Crea un pasajero INACTIVE_REGISTER solo con phone_number. */
    async createInactiveByPhone(phoneNumber: string): Promise<PassengerEntity> {
        const normalized = phoneNumber.trim();
        const entity = this.repo.create({
            phoneNumber: normalized,
            status: PassengerStatus.INACTIVE_REGISTER,
        } as Partial<PassengerEntity>);

        try {
            return await this.repo.save(entity);
        } catch (err: any) {
            // Condición de carrera: si alguien lo creó en paralelo por unique(phone)
            // MySQL duplicate key
            if (err?.code === "ER_DUP_ENTRY") {
                const existing = await this.findByPhoneNumber(normalized);
                if (existing) return existing;
            }
            throw err;
        }
    }

    /** Actualiza la última fecha de login (NOW()) por id. Devuelve true si afectó una fila. */
    async touchLastLoginAtById(id: string): Promise<boolean> {
        const result = await this.repo
            .createQueryBuilder()
            .update(PassengerEntity)
            .set({ lastLoginAt: () => "NOW()" }) // MySQL NOW()
            .where("id = :id", { id })
            .execute();

        return (result.affected ?? 0) > 0;
    }
}
