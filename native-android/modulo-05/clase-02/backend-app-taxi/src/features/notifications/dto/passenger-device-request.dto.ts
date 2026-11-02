import { ApiProperty } from "@nestjs/swagger";
import { Transform } from "class-transformer";
import { IsEnum, IsOptional, IsString, Length } from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";
import { DevicePlatform } from "../enum/device-platform.enum";

/* -------------------------------------------------------
   DTO para registrar el dispositivo del pasajero
   - token: el que entrega FirebaseMessaging en la app
   - platform: ANDROID por defecto
-------------------------------------------------------- */
export class PassengerDeviceRequestDto {
    @ApiProperty({ example: "fcm-token-del-dispositivo", maxLength: 255 })
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string" ? value.trim() : value,
    )
    @IsString({ message: i18nValidationMessage("device.validation.token") })
    @Length(1, 255, {
        message: i18nValidationMessage("device.validation.token"),
    })
    token!: string;

    @ApiProperty({
        enum: DevicePlatform,
        required: false,
        default: DevicePlatform.ANDROID,
    })
    @IsOptional()
    @IsEnum(DevicePlatform, {
        message: i18nValidationMessage("device.validation.platform"),
    })
    platform?: DevicePlatform;
}
