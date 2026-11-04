import { ApiProperty } from "@nestjs/swagger";
import { Transform } from "class-transformer";
import { IsString, Length } from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

const trim = ({ value }: { value: unknown }) =>
    typeof value === "string" ? value.trim() : value;

export class VehicleDto {
    @ApiProperty({ example: "ABC-123" })
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string" ? value.trim().toUpperCase() : value,
    )
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(5, 10, { message: i18nValidationMessage("validation.length") })
    plate!: string;

    @ApiProperty({ example: "Toyota" })
    @Transform(trim)
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(2, 40, { message: i18nValidationMessage("validation.length") })
    brand!: string;

    @ApiProperty({ example: "Yaris" })
    @Transform(trim)
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(1, 40, { message: i18nValidationMessage("validation.length") })
    model!: string;

    @ApiProperty({ example: "Blanco" })
    @Transform(trim)
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(3, 30, { message: i18nValidationMessage("validation.length") })
    color!: string;
}
