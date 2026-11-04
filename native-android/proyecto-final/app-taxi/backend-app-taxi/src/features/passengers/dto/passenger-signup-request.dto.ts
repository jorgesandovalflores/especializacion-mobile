import { ApiProperty } from "@nestjs/swagger";
import { Transform } from "class-transformer";
import { IsEmail, IsString, Length, MaxLength } from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

const trim = ({ value }: { value: unknown }) =>
    typeof value === "string" ? value.trim() : value;

/* -------------------------------------------------------
   DTO de registro de pasajero (completa el perfil)
   - givenName y familyName: 2 a 60 caracteres
   - email: formato válido, se guarda en minúsculas
-------------------------------------------------------- */
export class PassengerSignUpRequestDto {
    @ApiProperty({ example: "Jorge", minLength: 2, maxLength: 60 })
    @Transform(trim)
    @IsString({
        message: i18nValidationMessage("signup.validation.givenName"),
    })
    @Length(2, 60, {
        message: i18nValidationMessage("signup.validation.givenName"),
    })
    givenName!: string;

    @ApiProperty({ example: "Sandoval", minLength: 2, maxLength: 60 })
    @Transform(trim)
    @IsString({
        message: i18nValidationMessage("signup.validation.familyName"),
    })
    @Length(2, 60, {
        message: i18nValidationMessage("signup.validation.familyName"),
    })
    familyName!: string;

    @ApiProperty({ example: "jorge@example.com", maxLength: 120 })
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string" ? value.trim().toLowerCase() : value,
    )
    @IsEmail({}, { message: i18nValidationMessage("signup.validation.email") })
    @MaxLength(120, {
        message: i18nValidationMessage("signup.validation.email"),
    })
    email!: string;
}
