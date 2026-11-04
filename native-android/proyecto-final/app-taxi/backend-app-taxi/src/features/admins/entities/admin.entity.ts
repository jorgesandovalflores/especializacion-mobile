import {
    Column,
    CreateDateColumn,
    DeleteDateColumn,
    Entity,
    Index,
    PrimaryGeneratedColumn,
    UpdateDateColumn,
} from "typeorm";
import { AdminStatus } from "../enum/admin-status.enum";

@Entity({ name: "entity_admin" })
@Index("UQ_admin_email", ["email"], { unique: true })
export class AdminEntity {
    @PrimaryGeneratedColumn("uuid", { name: "id_admin" })
    id!: string;

    @Column("varchar", { name: "full_name", length: 120 })
    fullName!: string;

    @Column("varchar", { name: "email", length: 120 })
    email!: string;

    @Column("varchar", { name: "password_hash", length: 255, select: false })
    passwordHash!: string;

    @Column({
        type: "enum",
        enum: AdminStatus,
        name: "status",
        default: AdminStatus.ACTIVE,
    })
    status!: AdminStatus;

    @Column("datetime", { name: "last_login_at", nullable: true })
    lastLoginAt?: Date | null;

    @CreateDateColumn({ name: "created_at", type: "datetime" })
    createdAt!: Date;

    @UpdateDateColumn({ name: "updated_at", type: "datetime" })
    updatedAt!: Date;

    @DeleteDateColumn({ name: "deleted_at", type: "datetime", nullable: true })
    deletedAt?: Date | null;
}
