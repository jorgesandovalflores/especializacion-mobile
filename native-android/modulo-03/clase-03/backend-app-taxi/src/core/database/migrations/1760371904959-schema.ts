import { MigrationInterface, QueryRunner } from "typeorm";

/* -------------------------------------------------------
   Módulo 3 · Clase 01 (incremental sobre Schema1759768881969)
   - Crea entity_menu: opciones de menú por aplicación
-------------------------------------------------------- */
export class Schema1760371904959 implements MigrationInterface {
    name = 'Schema1760371904959'

    public async up(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`CREATE TABLE \`entity_menu\` (\`id_menu\` varchar(36) NOT NULL, \`key\` varchar(24) NOT NULL, \`text\` varchar(164) NOT NULL, \`icon\` varchar(40) NOT NULL, \`deeplink\` varchar(255) NOT NULL, \`order\` int NOT NULL, \`application\` enum ('BACKOFFICE', 'PASSENGER', 'DRIVER') NOT NULL DEFAULT 'PASSENGER', \`status\` enum ('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE', \`created_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), \`updated_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), \`deleted_at\` datetime(6) NULL, UNIQUE INDEX \`UQ_menu_application_order\` (\`application\`, \`order\`), UNIQUE INDEX \`UQ_menu_key\` (\`key\`), PRIMARY KEY (\`id_menu\`)) ENGINE=InnoDB`);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`DROP INDEX \`UQ_menu_key\` ON \`entity_menu\``);
        await queryRunner.query(`DROP INDEX \`UQ_menu_application_order\` ON \`entity_menu\``);
        await queryRunner.query(`DROP TABLE \`entity_menu\``);
    }

}
