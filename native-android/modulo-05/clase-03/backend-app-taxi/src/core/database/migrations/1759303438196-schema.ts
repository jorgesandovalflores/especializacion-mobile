import { MigrationInterface, QueryRunner } from "typeorm";

export class Schema1759303438196 implements MigrationInterface {
    name = 'Schema1759303438196'

    public async up(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`CREATE TABLE \`entity_passenger\` (\`id_passenger\` varchar(36) NOT NULL, \`phone_number\` varchar(20) NOT NULL, \`given_name\` varchar(60) NULL, \`family_name\` varchar(60) NULL, \`email\` varchar(120) NULL, \`photo_url\` varchar(255) NULL, \`status\` enum ('ACTIVE', 'SUSPENDED', 'DELETED') NOT NULL DEFAULT 'ACTIVE', \`last_login_at\` datetime NULL COMMENT 'Último inicio de sesión confirmado en servidor', \`created_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), \`updated_at\` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), \`deleted_at\` datetime(6) NULL, UNIQUE INDEX \`UQ_passenger_email\` (\`email\`), UNIQUE INDEX \`UQ_passenger_phone_number\` (\`phone_number\`), INDEX \`IDX_passenger_status\` (\`status\`), PRIMARY KEY (\`id_passenger\`)) ENGINE=InnoDB`);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`DROP INDEX \`IDX_passenger_status\` ON \`entity_passenger\``);
        await queryRunner.query(`DROP INDEX \`UQ_passenger_phone_number\` ON \`entity_passenger\``);
        await queryRunner.query(`DROP INDEX \`UQ_passenger_email\` ON \`entity_passenger\``);
        await queryRunner.query(`DROP TABLE \`entity_passenger\``);
    }

}
