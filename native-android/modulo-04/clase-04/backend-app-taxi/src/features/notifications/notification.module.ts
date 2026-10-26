import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";
import { JwtModule } from "@nestjs/jwt";
import { ConfigModule } from "@nestjs/config";

import { AccessTokenGuard } from "src/core/http/guard/access-token.guard";
import { FirebaseAdminService } from "src/core/firebase/firebase-admin.service";
import { PassengerModule } from "src/features/passengers/passenger.module";
import { PassengerDeviceEntity } from "./entities/passenger-device.entity";
import { PassengerDeviceDao } from "./dao/passenger-device.dao";
import { DeviceController } from "./controllers/device.controller";
import { NotificationService } from "./services/notification.service";
import { PushSender } from "./remote/push-sender";
import { FcmPushService } from "./remote/fcm-push.service";

/* -------------------------------------------------------
   NotificationModule
   - Guarda los tokens de FCM (entity_passenger_device)
   - PushSender se resuelve con FcmPushService
   - FirebaseAdminService (core) inicializa firebase-admin
   - Usa PassengerDao para enviar por número de teléfono
-------------------------------------------------------- */
@Module({
    imports: [
        TypeOrmModule.forFeature([PassengerDeviceEntity]),
        ConfigModule,
        JwtModule.register({}),
        PassengerModule,
    ],
    controllers: [DeviceController],
    providers: [
        NotificationService,
        PassengerDeviceDao,
        AccessTokenGuard,
        FirebaseAdminService,
        { provide: PushSender, useClass: FcmPushService },
    ],
    exports: [NotificationService],
})
export class NotificationModule {}
