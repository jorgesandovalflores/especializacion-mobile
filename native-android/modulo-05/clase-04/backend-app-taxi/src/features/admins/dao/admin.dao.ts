import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Not, Repository } from "typeorm";
import { AdminEntity } from "../entities/admin.entity";

@Injectable()
export class AdminDao {
    constructor(
        @InjectRepository(AdminEntity)
        private readonly repo: Repository<AdminEntity>,
    ) {}

    async findAll(): Promise<AdminEntity[]> {
        return this.repo.find({ order: { createdAt: "ASC" } });
    }

    async findById(id: string): Promise<AdminEntity | null> {
        return this.repo.findOne({ where: { id } });
    }

    async findByEmailWithPassword(email: string): Promise<AdminEntity | null> {
        return this.repo
            .createQueryBuilder("admin")
            .addSelect("admin.passwordHash")
            .where("admin.email = :email", { email })
            .getOne();
    }

    async isEmailTaken(email: string, excludeId?: string): Promise<boolean> {
        return this.repo.exists({
            where: { email, ...(excludeId ? { id: Not(excludeId) } : {}) },
            withDeleted: true,
        });
    }

    async create(values: Partial<AdminEntity>): Promise<AdminEntity> {
        const saved = await this.repo.save(this.repo.create(values));
        return (await this.findById(saved.id))!;
    }

    async update(
        id: string,
        values: Partial<AdminEntity>,
    ): Promise<AdminEntity | null> {
        await this.repo.update({ id }, values);
        return this.findById(id);
    }

    async softDelete(id: string): Promise<void> {
        await this.repo.softDelete({ id });
    }

    async touchLastLoginAt(id: string): Promise<void> {
        await this.repo.update({ id }, { lastLoginAt: new Date() });
    }
}
