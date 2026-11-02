import { MigrationInterface, QueryRunner } from "typeorm";

/* -------------------------------------------------------
   Módulo 5 · Clase 04 (incremental sobre Schema1792540800000)
   - Crea entity_admin, entity_driver y entity_tariff
   - Crea entity_trip y entity_trip_offer (viajes y ofertas)
-------------------------------------------------------- */
export class Schema1793664000000 implements MigrationInterface {
    name = 'Schema1793664000000'

    public async up(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`CREATE TABLE \`entity_admin\` (\`id_admin\` varchar(36) NOT NULL, \`full_name\` varchar(120) NOT NULL, \`email\` varchar(120) NOT NULL, \`password_hash\` varchar(255) NOT NULL, \`status\` enum ('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE', \`last_login_at\` datetime NULL, \`created_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), \`updated_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), \`deleted_at\` datetime(6) NULL, UNIQUE INDEX \`UQ_admin_email\` (\`email\`), PRIMARY KEY (\`id_admin\`)) ENGINE=InnoDB`);
        await queryRunner.query(`CREATE TABLE \`entity_driver\` (\`id_driver\` varchar(36) NOT NULL, \`phone_number\` varchar(20) NOT NULL, \`given_name\` varchar(60) NOT NULL, \`family_name\` varchar(60) NOT NULL, \`email\` varchar(120) NULL, \`password_hash\` varchar(255) NOT NULL, \`photo_url\` varchar(255) NULL, \`vehicle_plate\` varchar(10) NOT NULL, \`vehicle_brand\` varchar(40) NOT NULL, \`vehicle_model\` varchar(40) NOT NULL, \`vehicle_color\` varchar(30) NOT NULL, \`status\` enum ('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE', \`rating_avg\` decimal(3,2) NOT NULL DEFAULT '5.00', \`rating_count\` int NOT NULL DEFAULT '0', \`last_login_at\` datetime NULL, \`created_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), \`updated_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), \`deleted_at\` datetime(6) NULL, UNIQUE INDEX \`UQ_driver_phone_number\` (\`phone_number\`), UNIQUE INDEX \`UQ_driver_email\` (\`email\`), UNIQUE INDEX \`UQ_driver_vehicle_plate\` (\`vehicle_plate\`), INDEX \`IDX_driver_status\` (\`status\`), PRIMARY KEY (\`id_driver\`)) ENGINE=InnoDB`);
        await queryRunner.query(`CREATE TABLE \`entity_tariff\` (\`id_tariff\` tinyint UNSIGNED NOT NULL, \`base_fare\` decimal(10,2) NOT NULL, \`price_per_km\` decimal(10,2) NOT NULL, \`currency\` char(3) NOT NULL DEFAULT 'PEN', \`updated_by\` varchar(36) NULL, \`updated_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), PRIMARY KEY (\`id_tariff\`)) ENGINE=InnoDB`);
        await queryRunner.query(`CREATE TABLE \`entity_trip\` (\`id_trip\` varchar(36) NOT NULL, \`id_passenger\` varchar(36) NOT NULL, \`id_driver\` varchar(36) NULL, \`status\` enum ('REQUESTED', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'REQUESTED', \`origin_lat\` decimal(10,7) NOT NULL, \`origin_lng\` decimal(10,7) NOT NULL, \`origin_address\` varchar(255) NULL, \`destination_lat\` decimal(10,7) NOT NULL, \`destination_lng\` decimal(10,7) NOT NULL, \`destination_address\` varchar(255) NULL, \`distance_meters\` int NOT NULL, \`duration_seconds\` int NOT NULL, \`route_polyline\` text NOT NULL, \`estimated_fare\` decimal(10,2) NOT NULL, \`final_fare\` decimal(10,2) NULL, \`currency\` char(3) NOT NULL DEFAULT 'PEN', \`rating_by_passenger\` tinyint NULL, \`comment_by_passenger\` varchar(255) NULL, \`rating_by_driver\` tinyint NULL, \`comment_by_driver\` varchar(255) NULL, \`cancelled_by\` enum ('PASSENGER', 'DRIVER') NULL, \`requested_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), \`accepted_at\` datetime NULL, \`arrived_at\` datetime NULL, \`started_at\` datetime NULL, \`completed_at\` datetime NULL, \`cancelled_at\` datetime NULL, \`updated_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), INDEX \`IDX_trip_status\` (\`status\`), INDEX \`IDX_trip_passenger\` (\`id_passenger\`), INDEX \`IDX_trip_driver\` (\`id_driver\`), PRIMARY KEY (\`id_trip\`)) ENGINE=InnoDB`);
        await queryRunner.query(`CREATE TABLE \`entity_trip_offer\` (\`id_trip_offer\` varchar(36) NOT NULL, \`id_trip\` varchar(36) NOT NULL, \`id_driver\` varchar(36) NOT NULL, \`price\` decimal(10,2) NOT NULL, \`eta_seconds\` int NULL, \`distance_to_pickup_meters\` int NULL, \`status\` enum ('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING', \`created_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), \`updated_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), UNIQUE INDEX \`UQ_trip_offer_trip_driver\` (\`id_trip\`, \`id_driver\`), PRIMARY KEY (\`id_trip_offer\`)) ENGINE=InnoDB`);
        await queryRunner.query(`ALTER TABLE \`entity_trip\` ADD CONSTRAINT \`FK_trip_passenger\` FOREIGN KEY (\`id_passenger\`) REFERENCES \`entity_passenger\`(\`id_passenger\`) ON DELETE NO ACTION ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE \`entity_trip\` ADD CONSTRAINT \`FK_trip_driver\` FOREIGN KEY (\`id_driver\`) REFERENCES \`entity_driver\`(\`id_driver\`) ON DELETE NO ACTION ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE \`entity_trip_offer\` ADD CONSTRAINT \`FK_trip_offer_trip\` FOREIGN KEY (\`id_trip\`) REFERENCES \`entity_trip\`(\`id_trip\`) ON DELETE CASCADE ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE \`entity_trip_offer\` ADD CONSTRAINT \`FK_trip_offer_driver\` FOREIGN KEY (\`id_driver\`) REFERENCES \`entity_driver\`(\`id_driver\`) ON DELETE NO ACTION ON UPDATE NO ACTION`);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`ALTER TABLE \`entity_trip_offer\` DROP FOREIGN KEY \`FK_trip_offer_driver\``);
        await queryRunner.query(`ALTER TABLE \`entity_trip_offer\` DROP FOREIGN KEY \`FK_trip_offer_trip\``);
        await queryRunner.query(`ALTER TABLE \`entity_trip\` DROP FOREIGN KEY \`FK_trip_driver\``);
        await queryRunner.query(`ALTER TABLE \`entity_trip\` DROP FOREIGN KEY \`FK_trip_passenger\``);
        await queryRunner.query(`DROP TABLE \`entity_trip_offer\``);
        await queryRunner.query(`DROP TABLE \`entity_trip\``);
        await queryRunner.query(`DROP TABLE \`entity_tariff\``);
        await queryRunner.query(`DROP TABLE \`entity_driver\``);
        await queryRunner.query(`DROP TABLE \`entity_admin\``);
    }

}
