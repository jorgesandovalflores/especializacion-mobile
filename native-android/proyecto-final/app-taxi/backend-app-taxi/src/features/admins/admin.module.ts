import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";
import { JwtModule } from "@nestjs/jwt";
import { ConfigModule } from "@nestjs/config";
import { AdminAccessGuard } from "src/core/http/guard/access-token.guard";
import { AdminEntity } from "./entities/admin.entity";
import { AdminDao } from "./dao/admin.dao";
import { AdminService } from "./services/admin.service";
import { AdminAuthController } from "./controllers/admin-auth.controller";
import { AdminController } from "./controllers/admin.controller";

@Module({
    imports: [
        TypeOrmModule.forFeature([AdminEntity]),
        ConfigModule,
        JwtModule.register({}),
    ],
    controllers: [AdminAuthController, AdminController],
    providers: [AdminDao, AdminService, AdminAccessGuard],
})
export class AdminModule {}
