import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { PassengerEntity } from "../entities/passenger.entity";

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
