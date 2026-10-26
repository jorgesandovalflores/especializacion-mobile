import { ApiProperty } from "@nestjs/swagger";
import { IsString, IsUrl, MaxLength } from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

/* -------------------------------------------------------
   DTO para guardar la foto de perfil
   - photoUrl: downloadUrl que devolvió Firebase Storage
-------------------------------------------------------- */
export class PassengerPhotoRequestDto {
    @ApiProperty({
        example:
            "https://firebasestorage.googleapis.com/v0/b/app-taxi-xxxxx.firebasestorage.app/o/passengers%2F<id>%2Favatar.jpg?alt=media&token=<uuid>",
        maxLength: 255,
    })
    @IsString({ message: i18nValidationMessage("photo.validation.url") })
    @IsUrl(
        { protocols: ["https"], require_protocol: true },
        { message: i18nValidationMessage("photo.validation.url") },
    )
    @MaxLength(255, { message: i18nValidationMessage("photo.validation.url") })
    photoUrl!: string;
}
