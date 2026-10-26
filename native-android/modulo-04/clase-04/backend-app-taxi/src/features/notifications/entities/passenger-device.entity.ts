import {
    Column,
    CreateDateColumn,
    Entity,
    Index,
    PrimaryGeneratedColumn,
    UpdateDateColumn,
} from "typeorm";
import { DevicePlatform } from "../enum/device-platform.enum";

/* -------------------------------------------------------
   Dispositivo de un pasajero (token de FCM)
   - Un pasajero puede tener varios dispositivos
   - El token es único: si otro pasajero inicia sesión en
     el mismo teléfono, el token cambia de dueño
-------------------------------------------------------- */
@Entity({ name: "entity_passenger_device" })
@Index("UQ_passenger_device_token", ["token"], { unique: true })
@Index("IDX_passenger_device_passenger", ["passengerId"])
export class PassengerDeviceEntity {
    @PrimaryGeneratedColumn("uuid", { name: "id_passenger_device" })
    id!: string;

    @Column("varchar", { name: "id_passenger", length: 36 })
    passengerId!: string;

    @Column("varchar", { name: "token", length: 255 })
    token!: string;

    @Column({
        type: "enum",
        enum: DevicePlatform,
        name: "platform",
        default: DevicePlatform.ANDROID,
    })
    platform!: DevicePlatform;

    @CreateDateColumn({ name: "created_at", type: "datetime" })
    createdAt!: Date;

    @UpdateDateColumn({ name: "updated_at", type: "datetime" })
    updatedAt!: Date;
}
