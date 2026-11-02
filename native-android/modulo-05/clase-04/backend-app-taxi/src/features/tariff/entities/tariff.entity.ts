import { Column, Entity, PrimaryColumn, UpdateDateColumn } from "typeorm";
import { decimalTransformer } from "src/core/database/transformers/decimal.transformer";

@Entity({ name: "entity_tariff" })
export class TariffEntity {
    @PrimaryColumn("tinyint", { name: "id_tariff", unsigned: true })
    id!: number;

    @Column("decimal", {
        name: "base_fare",
        precision: 10,
        scale: 2,
        transformer: decimalTransformer,
    })
    baseFare!: number;

    @Column("decimal", {
        name: "price_per_km",
        precision: 10,
        scale: 2,
        transformer: decimalTransformer,
    })
    pricePerKm!: number;

    @Column("char", { name: "currency", length: 3, default: "PEN" })
    currency!: string;

    @Column("varchar", { name: "updated_by", length: 36, nullable: true })
    updatedBy?: string | null;

    @UpdateDateColumn({ name: "updated_at", type: "datetime" })
    updatedAt!: Date;
}
