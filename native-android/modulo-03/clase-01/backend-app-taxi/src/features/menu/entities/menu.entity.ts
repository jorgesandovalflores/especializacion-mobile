import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    CreateDateColumn,
    UpdateDateColumn,
    DeleteDateColumn,
} from "typeorm";
import { MenuApplication } from "../enum/menu-application.enum";
import { MenuStatus } from "../enum/menu-status.enum";

@Entity({ name: "entity_menu" })
@Index("UQ_menu_key", ["key"], { unique: true })
@Index("UQ_menu_application_order", ["application", "order"], { unique: true })
export class MenuEntity {
    /* ---------------------------------------
       PK UUID v4 almacenado como CHAR(36)
       --------------------------------------- */
    @PrimaryGeneratedColumn("uuid", { name: "id_menu" })
    id!: string;

    /* ---------------------------------------
       Identificador estable de la opción
       - La app lo usa como clave primaria local (Room)
       --------------------------------------- */
    @Column("varchar", { name: "key", length: 24 })
    key!: string;

    @Column("varchar", { name: "text", length: 164 })
    text!: string;

    /* ---------------------------------------
       Nombre lógico del ícono (home, profile, history, support)
       - La app lo traduce a un ícono local
       --------------------------------------- */
    @Column("varchar", { name: "icon", length: 40 })
    icon!: string;

    @Column("varchar", { name: "deeplink", length: 255 })
    deeplink!: string;

    @Column("int", { name: "order" })
    order!: number;

    @Column({
        type: "enum",
        enum: MenuApplication,
        name: "application",
        default: MenuApplication.PASSENGER,
    })
    application!: MenuApplication;

    @Column({
        type: "enum",
        enum: MenuStatus,
        name: "status",
        default: MenuStatus.ACTIVE,
    })
    status!: MenuStatus;

    @CreateDateColumn({ name: "created_at", type: "datetime" })
    createdAt!: Date;

    @UpdateDateColumn({ name: "updated_at", type: "datetime" })
    updatedAt!: Date;

    @DeleteDateColumn({ name: "deleted_at", type: "datetime", nullable: true })
    deletedAt?: Date | null;
}
