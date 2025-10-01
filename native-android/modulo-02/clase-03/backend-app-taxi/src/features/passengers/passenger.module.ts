import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";
import { JwtModule } from "@nestjs/jwt";
import { ConfigModule } from "@nestjs/config";

import { PassengerEntity } from "./entities/passenger.entity";
import { PassengerDao } from "./dao/passenger.dao";
import { PassengerService } from "./services/passenger.service";
import { PassengerController } from "./controllers/passenger.controller";

/* -------------------------------------------------------
   PassengerModule
   - Importa TypeORM (entidad), Jwt y Config
   - Expone controller + service + dao
   - JwtModule: la firma usa secretos/TTLs desde ConfigService
     (el service pasa secret/expiresIn explícitamente)
-------------------------------------------------------- */
@Module({
    imports: [
        TypeOrmModule.forFeature([PassengerEntity]),
        ConfigModule,
        JwtModule.register({}),
    ],
    controllers: [PassengerController],
    providers: [PassengerService, PassengerDao],
    exports: [PassengerService, PassengerDao],
})
export class PassengerModule {}
