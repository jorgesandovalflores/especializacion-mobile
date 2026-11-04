import { AdminDto } from "../dto/admin.dto";
import { AdminEntity } from "../entities/admin.entity";

export const toAdminDto = (entity: AdminEntity): AdminDto => ({
    id: entity.id,
    fullName: entity.fullName,
    email: entity.email,
    status: entity.status,
    lastLoginAt: entity.lastLoginAt ? entity.lastLoginAt.toISOString() : null,
    createdAt: entity.createdAt.toISOString(),
});
