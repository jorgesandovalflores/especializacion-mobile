import { Injectable } from "@nestjs/common";
import { PassengerDao } from "src/features/passengers/dao/passenger.dao";
import { PassengerDeviceDao } from "../dao/passenger-device.dao";
import { PassengerDeviceRequestDto } from "../dto/passenger-device-request.dto";
import { DevicePlatform } from "../enum/device-platform.enum";
import { PushDelivery, PushMessage, PushSender } from "../remote/push-sender";

/* -------------------------------------------------------
   Notificaciones del pasajero
   - registerDevice: guarda el token de FCM del teléfono
   - sendToPassenger: envía a todos sus dispositivos y
     borra los tokens que FCM marcó como inválidos
-------------------------------------------------------- */
@Injectable()
export class NotificationService {
    constructor(
        private readonly devices: PassengerDeviceDao,
        private readonly passengers: PassengerDao,
        private readonly push: PushSender,
    ) {}

    async registerDevice(
        passengerId: string,
        dto: PassengerDeviceRequestDto,
    ): Promise<void> {
        await this.devices.upsert(
            passengerId,
            dto.token,
            dto.platform ?? DevicePlatform.ANDROID,
        );
    }

    async sendToPassenger(
        passengerId: string,
        message: PushMessage,
    ): Promise<PushDelivery> {
        const tokens = await this.devices.findTokensByPassengerId(passengerId);
        const delivery = await this.push.send(tokens, message);
        await this.devices.deleteByTokens(delivery.invalidTokens);
        return delivery;
    }

    /** Devuelve null si no existe un pasajero con ese teléfono. */
    async sendToPhone(
        phoneNumber: string,
        message: PushMessage,
    ): Promise<PushDelivery | null> {
        const passenger = await this.passengers.findByPhoneNumber(phoneNumber);
        if (!passenger) return null;
        return this.sendToPassenger(passenger.id, message);
    }
}
