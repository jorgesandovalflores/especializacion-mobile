import { MigrationInterface, QueryRunner } from "typeorm";

/* -------------------------------------------------------
   Módulo 4 · Clase 03 (incremental sobre Schema1760371904959)
   - Crea entity_passenger_device: tokens de FCM por pasajero
-------------------------------------------------------- */
export class Schema1792540800000 implements MigrationInterface {
    name = 'Schema1792540800000'

    public async up(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`CREATE TABLE \`entity_passenger_device\` (\`id_passenger_device\` varchar(36) NOT NULL, \`id_passenger\` varchar(36) NOT NULL, \`token\` varchar(255) NOT NULL, \`platform\` enum ('ANDROID', 'IOS') NOT NULL DEFAULT 'ANDROID', \`created_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), \`updated_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), INDEX \`IDX_passenger_device_passenger\` (\`id_passenger\`), UNIQUE INDEX \`UQ_passenger_device_token\` (\`token\`), PRIMARY KEY (\`id_passenger_device\`)) ENGINE=InnoDB`);
        await queryRunner.query(`ALTER TABLE \`entity_passenger_device\` ADD CONSTRAINT \`FK_passenger_device_passenger\` FOREIGN KEY (\`id_passenger\`) REFERENCES \`entity_passenger\`(\`id_passenger\`) ON DELETE CASCADE ON UPDATE NO ACTION`);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`ALTER TABLE \`entity_passenger_device\` DROP FOREIGN KEY \`FK_passenger_device_passenger\``);
        await queryRunner.query(`DROP INDEX \`UQ_passenger_device_token\` ON \`entity_passenger_device\``);
        await queryRunner.query(`DROP INDEX \`IDX_passenger_device_passenger\` ON \`entity_passenger_device\``);
        await queryRunner.query(`DROP TABLE \`entity_passenger_device\``);
    }

}
