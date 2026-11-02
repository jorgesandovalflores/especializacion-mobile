import {
    Column,
    CreateDateColumn,
    Entity,
    Index,
    JoinColumn,
    ManyToOne,
    PrimaryGeneratedColumn,
    UpdateDateColumn,
} from "typeorm";
import { decimalTransformer } from "src/core/database/transformers/decimal.transformer";
import { DriverEntity } from "src/features/drivers/entities/driver.entity";
import { OfferStatus } from "../enum/offer-status.enum";

@Entity({ name: "entity_trip_offer" })
@Index("UQ_trip_offer_trip_driver", ["tripId", "driverId"], { unique: true })
export class TripOfferEntity {
    @PrimaryGeneratedColumn("uuid", { name: "id_trip_offer" })
    id!: string;

    @Column("varchar", { name: "id_trip", length: 36 })
    tripId!: string;

    @Column("varchar", { name: "id_driver", length: 36 })
    driverId!: string;

    @ManyToOne(() => DriverEntity)
    @JoinColumn({ name: "id_driver" })
    driver?: DriverEntity;

    @Column("decimal", {
        name: "price",
        precision: 10,
        scale: 2,
        transformer: decimalTransformer,
    })
    price!: number;

    @Column("int", { name: "eta_seconds", nullable: true })
    etaSeconds?: number | null;

    @Column("int", { name: "distance_to_pickup_meters", nullable: true })
    distanceToPickupMeters?: number | null;

    @Column({
        type: "enum",
        enum: OfferStatus,
        name: "status",
        default: OfferStatus.PENDING,
    })
    status!: OfferStatus;

    @CreateDateColumn({ name: "created_at", type: "datetime" })
    createdAt!: Date;

    @UpdateDateColumn({ name: "updated_at", type: "datetime" })
    updatedAt!: Date;
}
