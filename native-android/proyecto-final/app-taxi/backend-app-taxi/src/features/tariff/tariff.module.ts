import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";
import { JwtModule } from "@nestjs/jwt";
import { ConfigModule } from "@nestjs/config";
import { AdminAccessGuard } from "src/core/http/guard/access-token.guard";
import { TariffEntity } from "./entities/tariff.entity";
import { TariffDao } from "./dao/tariff.dao";
import { TariffService } from "./services/tariff.service";
import { AdminTariffController } from "./controllers/admin-tariff.controller";

@Module({
    imports: [
        TypeOrmModule.forFeature([TariffEntity]),
        ConfigModule,
        JwtModule.register({}),
    ],
    controllers: [AdminTariffController],
    providers: [TariffDao, TariffService, AdminAccessGuard],
    exports: [TariffService],
})
export class TariffModule {}
