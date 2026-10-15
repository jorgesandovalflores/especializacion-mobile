import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";
import { JwtModule } from "@nestjs/jwt";
import { ConfigModule } from "@nestjs/config";

import { CacheService } from "src/core/cache/cache.service";
import { AccessTokenGuard } from "src/core/http/guard/access-token.guard";
import { MenuEntity } from "./entities/menu.entity";
import { MenuController } from "./controllers/menu.controller";
import { MenuService } from "./services/menu.service";
import { MenuDao } from "./dao/menu.dao";

/* -------------------------------------------------------
   MenuModule
   - Importa TypeORM (entidad), Jwt y Config
   - JwtModule: AccessTokenGuard verifica el access token
     con el secreto que lee de ConfigService
-------------------------------------------------------- */
@Module({
    imports: [
        TypeOrmModule.forFeature([MenuEntity]),
        ConfigModule,
        JwtModule.register({}),
    ],
    controllers: [MenuController],
    providers: [MenuService, MenuDao, CacheService, AccessTokenGuard],
    exports: [MenuService, MenuDao],
})
export class MenuModule {}
