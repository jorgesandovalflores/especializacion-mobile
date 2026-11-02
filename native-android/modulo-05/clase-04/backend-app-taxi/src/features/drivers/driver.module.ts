import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";
import { JwtModule } from "@nestjs/jwt";
import { ConfigModule } from "@nestjs/config";
import {
    AdminAccessGuard,
    DriverAccessGuard,
} from "src/core/http/guard/access-token.guard";
import { DriverEntity } from "./entities/driver.entity";
import { DriverDao } from "./dao/driver.dao";
import { DriverAuthService } from "./services/driver-auth.service";
import { DriverService } from "./services/driver.service";
import { DriverPresenceService } from "./services/driver-presence.service";
import { DriverAuthController } from "./controllers/driver-auth.controller";
import { DriverController } from "./controllers/driver.controller";
import { AdminDriverController } from "./controllers/admin-driver.controller";

@Module({
    imports: [
        TypeOrmModule.forFeature([DriverEntity]),
        ConfigModule,
        JwtModule.register({}),
    ],
    controllers: [
        DriverAuthController,
        DriverController,
        AdminDriverController,
    ],
    providers: [
        DriverDao,
        DriverAuthService,
        DriverService,
        DriverPresenceService,
        DriverAccessGuard,
        AdminAccessGuard,
    ],
    exports: [DriverDao, DriverPresenceService],
})
export class DriverModule {}
