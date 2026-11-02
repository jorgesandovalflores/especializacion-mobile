import { ApiProperty } from "@nestjs/swagger";
import { IsNotEmpty, IsString } from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

export class DriverRefreshRequestDto {
    @ApiProperty()
    @IsString({ message: i18nValidationMessage("validation.string") })
    @IsNotEmpty({ message: i18nValidationMessage("auth.refreshTokenRequired") })
    refreshToken!: string;
}
