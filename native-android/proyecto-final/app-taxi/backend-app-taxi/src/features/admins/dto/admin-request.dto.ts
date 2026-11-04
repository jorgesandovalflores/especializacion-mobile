import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger";
import { Transform } from "class-transformer";
import {
    IsEmail,
    IsEnum,
    IsOptional,
    IsString,
    Length,
    MaxLength,
} from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";
import { AdminStatus } from "../enum/admin-status.enum";

const normalizeEmail = ({ value }: { value: unknown }) =>
    typeof value === "string" ? value.trim().toLowerCase() : value;

export class AdminLoginRequestDto {
    @ApiProperty({ example: "admin@apptaxi.pe" })
    @Transform(normalizeEmail)
    @IsEmail({}, { message: i18nValidationMessage("validation.email") })
    email!: string;

    @ApiProperty({ example: "Admin123*" })
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(8, 64, { message: i18nValidationMessage("validation.password") })
    password!: string;
}

class AdminProfileRequestDto {
    @ApiProperty({ example: "Lucía Torres" })
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string" ? value.trim() : value,
    )
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(3, 120, { message: i18nValidationMessage("validation.length") })
    fullName!: string;

    @ApiProperty({ example: "lucia@apptaxi.pe" })
    @Transform(normalizeEmail)
    @IsEmail({}, { message: i18nValidationMessage("validation.email") })
    @MaxLength(120, { message: i18nValidationMessage("validation.email") })
    email!: string;
}

export class AdminCreateRequestDto extends AdminProfileRequestDto {
    @ApiProperty({ example: "Admin123*" })
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(8, 64, { message: i18nValidationMessage("validation.password") })
    password!: string;
}

export class AdminUpdateRequestDto extends AdminProfileRequestDto {
    @ApiPropertyOptional({ example: "Admin123*" })
    @IsOptional()
    @Transform(({ value }: { value: unknown }) =>
        value === "" ? undefined : value,
    )
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(8, 64, { message: i18nValidationMessage("validation.password") })
    password?: string;

    @ApiProperty({ enum: AdminStatus, enumName: "AdminStatus" })
    @IsEnum(AdminStatus, {
        message: i18nValidationMessage("validation.status"),
    })
    status!: AdminStatus;
}
