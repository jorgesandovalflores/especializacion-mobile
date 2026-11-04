import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger";
import { Transform, Type } from "class-transformer";
import {
    IsEmail,
    IsEnum,
    IsOptional,
    IsString,
    Length,
    Matches,
    MaxLength,
    ValidateNested,
} from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";
import { DriverStatus } from "../enum/driver-status.enum";
import { VehicleDto } from "./vehicle.dto";

const trim = ({ value }: { value: unknown }) =>
    typeof value === "string" ? value.trim() : value;

class DriverProfileRequestDto {
    @ApiProperty({ example: "51900000003" })
    @Transform(trim)
    @Matches(/^\d{11}$/, { message: i18nValidationMessage("validation.phone") })
    phone!: string;

    @ApiProperty({ example: "Rosa" })
    @Transform(trim)
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(2, 60, { message: i18nValidationMessage("validation.length") })
    givenName!: string;

    @ApiProperty({ example: "Huamán" })
    @Transform(trim)
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(2, 60, { message: i18nValidationMessage("validation.length") })
    familyName!: string;

    @ApiPropertyOptional({ example: "rosa@apptaxi.pe" })
    @IsOptional()
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string"
            ? value.trim().toLowerCase() || undefined
            : value,
    )
    @IsEmail({}, { message: i18nValidationMessage("validation.email") })
    @MaxLength(120, { message: i18nValidationMessage("validation.email") })
    email?: string;

    @ApiProperty({ type: VehicleDto })
    @ValidateNested({ message: i18nValidationMessage("validation.object") })
    @Type(() => VehicleDto)
    vehicle!: VehicleDto;
}

export class DriverCreateRequestDto extends DriverProfileRequestDto {
    @ApiProperty({ example: "Driver123*" })
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(8, 64, { message: i18nValidationMessage("validation.password") })
    password!: string;
}

export class DriverUpdateRequestDto extends DriverProfileRequestDto {
    @ApiPropertyOptional({ example: "Driver123*" })
    @IsOptional()
    @Transform(({ value }: { value: unknown }) =>
        value === "" ? undefined : value,
    )
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(8, 64, { message: i18nValidationMessage("validation.password") })
    password?: string;

    @ApiProperty({ enum: DriverStatus, enumName: "DriverStatus" })
    @IsEnum(DriverStatus, {
        message: i18nValidationMessage("validation.status"),
    })
    status!: DriverStatus;
}
