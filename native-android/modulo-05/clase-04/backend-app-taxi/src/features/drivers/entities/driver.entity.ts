import {
    Column,
    CreateDateColumn,
    DeleteDateColumn,
    Entity,
    Index,
    PrimaryGeneratedColumn,
    UpdateDateColumn,
} from "typeorm";
import { decimalTransformer } from "src/core/database/transformers/decimal.transformer";
import { DriverStatus } from "../enum/driver-status.enum";

@Entity({ name: "entity_driver" })
@Index("UQ_driver_phone_number", ["phoneNumber"], { unique: true })
@Index("UQ_driver_email", ["email"], { unique: true })
@Index("UQ_driver_vehicle_plate", ["vehiclePlate"], { unique: true })
@Index("IDX_driver_status", ["status"])
export class DriverEntity {
    @PrimaryGeneratedColumn("uuid", { name: "id_driver" })
    id!: string;

    @Column("varchar", { name: "phone_number", length: 20 })
    phoneNumber!: string;

    @Column("varchar", { name: "given_name", length: 60 })
    givenName!: string;

    @Column("varchar", { name: "family_name", length: 60 })
    familyName!: string;

    @Column("varchar", { name: "email", length: 120, nullable: true })
    email?: string | null;

    @Column("varchar", { name: "password_hash", length: 255, select: false })
    passwordHash!: string;

    @Column("varchar", { name: "photo_url", length: 255, nullable: true })
    photoUrl?: string | null;

    @Column("varchar", { name: "vehicle_plate", length: 10 })
    vehiclePlate!: string;

    @Column("varchar", { name: "vehicle_brand", length: 40 })
    vehicleBrand!: string;

    @Column("varchar", { name: "vehicle_model", length: 40 })
    vehicleModel!: string;

    @Column("varchar", { name: "vehicle_color", length: 30 })
    vehicleColor!: string;

    @Column({
        type: "enum",
        enum: DriverStatus,
        name: "status",
        default: DriverStatus.ACTIVE,
    })
    status!: DriverStatus;

    @Column("decimal", {
        name: "rating_avg",
        precision: 3,
        scale: 2,
        default: 5,
        transformer: decimalTransformer,
    })
    ratingAvg!: number;

    @Column("int", { name: "rating_count", default: 0 })
    ratingCount!: number;

    @Column("datetime", { name: "last_login_at", nullable: true })
    lastLoginAt?: Date | null;

    @CreateDateColumn({ name: "created_at", type: "datetime" })
    createdAt!: Date;

    @UpdateDateColumn({ name: "updated_at", type: "datetime" })
    updatedAt!: Date;

    @DeleteDateColumn({ name: "deleted_at", type: "datetime", nullable: true })
    deletedAt?: Date | null;
}
