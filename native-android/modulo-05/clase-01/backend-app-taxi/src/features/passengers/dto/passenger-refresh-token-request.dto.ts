import { ApiProperty } from "@nestjs/swagger";
import { Transform } from "class-transformer";
import { IsNotEmpty, IsString } from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

/* -------------------------------------------------------
   DTO de renovación de sesión
   - refreshToken: el JWT de refresco recibido en otp-validate
     o en un refresh anterior
-------------------------------------------------------- */
export class PassengerRefreshTokenRequestDto {
    @ApiProperty({
        description: "Refresh token (JWT) issued by otp-validate or refresh",
        example: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    })
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string" ? value.trim() : value,
    )
    @IsString({
        message: i18nValidationMessage("auth.refreshTokenRequired"),
    })
    @IsNotEmpty({
        message: i18nValidationMessage("auth.refreshTokenRequired"),
    })
    refreshToken!: string;
}
