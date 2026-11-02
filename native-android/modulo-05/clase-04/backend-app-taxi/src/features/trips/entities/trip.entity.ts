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
import { PassengerEntity } from "src/features/passengers/entities/passenger.entity";
import { DriverEntity } from "src/features/drivers/entities/driver.entity";
import { TripCancelledBy, TripStatus } from "../enum/trip-status.enum";

const coordinate = (name: string) =>
    Column("decimal", {
        name,
        precision: 10,
        scale: 7,
        transformer: decimalTransformer,
    });

@Entity({ name: "entity_trip" })
@Index("IDX_trip_status", ["status"])
@Index("IDX_trip_passenger", ["passengerId"])
@Index("IDX_trip_driver", ["driverId"])
export class TripEntity {
    @PrimaryGeneratedColumn("uuid", { name: "id_trip" })
    id!: string;

    @Column("varchar", { name: "id_passenger", length: 36 })
    passengerId!: string;

    @ManyToOne(() => PassengerEntity)
    @JoinColumn({ name: "id_passenger" })
    passenger?: PassengerEntity;

    @Column("varchar", { name: "id_driver", length: 36, nullable: true })
    driverId?: string | null;

    @ManyToOne(() => DriverEntity, { nullable: true })
    @JoinColumn({ name: "id_driver" })
    driver?: DriverEntity | null;

    @Column({
        type: "enum",
        enum: TripStatus,
        name: "status",
        default: TripStatus.REQUESTED,
    })
    status!: TripStatus;

    @coordinate("origin_lat")
    originLat!: number;

    @coordinate("origin_lng")
    originLng!: number;

    @Column("varchar", { name: "origin_address", length: 255, nullable: true })
    originAddress?: string | null;

    @coordinate("destination_lat")
    destinationLat!: number;

    @coordinate("destination_lng")
    destinationLng!: number;

    @Column("varchar", {
        name: "destination_address",
        length: 255,
        nullable: true,
    })
    destinationAddress?: string | null;

    @Column("int", { name: "distance_meters" })
    distanceMeters!: number;

    @Column("int", { name: "duration_seconds" })
    durationSeconds!: number;

    @Column("text", { name: "route_polyline" })
    routePolyline!: string;

    @Column("decimal", {
        name: "estimated_fare",
        precision: 10,
        scale: 2,
        transformer: decimalTransformer,
    })
    estimatedFare!: number;

    @Column("decimal", {
        name: "final_fare",
        precision: 10,
        scale: 2,
        nullable: true,
        transformer: decimalTransformer,
    })
    finalFare?: number | null;

    @Column("char", { name: "currency", length: 3, default: "PEN" })
    currency!: string;

    @Column("tinyint", { name: "rating_by_passenger", nullable: true })
    ratingByPassenger?: number | null;

    @Column("varchar", {
        name: "comment_by_passenger",
        length: 255,
        nullable: true,
    })
    commentByPassenger?: string | null;

    @Column("tinyint", { name: "rating_by_driver", nullable: true })
    ratingByDriver?: number | null;

    @Column("varchar", {
        name: "comment_by_driver",
        length: 255,
        nullable: true,
    })
    commentByDriver?: string | null;

    @Column({
        type: "enum",
        enum: TripCancelledBy,
        name: "cancelled_by",
        nullable: true,
    })
    cancelledBy?: TripCancelledBy | null;

    @CreateDateColumn({ name: "requested_at", type: "datetime" })
    requestedAt!: Date;

    @Column("datetime", { name: "accepted_at", nullable: true })
    acceptedAt?: Date | null;

    @Column("datetime", { name: "arrived_at", nullable: true })
    arrivedAt?: Date | null;

    @Column("datetime", { name: "started_at", nullable: true })
    startedAt?: Date | null;

    @Column("datetime", { name: "completed_at", nullable: true })
    completedAt?: Date | null;

    @Column("datetime", { name: "cancelled_at", nullable: true })
    cancelledAt?: Date | null;

    @UpdateDateColumn({ name: "updated_at", type: "datetime" })
    updatedAt!: Date;
}
