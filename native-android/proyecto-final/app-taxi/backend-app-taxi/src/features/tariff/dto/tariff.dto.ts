import { ApiProperty } from "@nestjs/swagger";
import { IsNumber, Max, Min } from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

export class TariffDto {
    @ApiProperty({ example: 5 })
    baseFare!: number;

    @ApiProperty({ example: 1.5 })
    pricePerKm!: number;

    @ApiProperty({ example: "PEN" })
    currency!: string;

    @ApiProperty()
    updatedAt!: string;
}

export class TariffUpdateRequestDto {
    @ApiProperty({ example: 5 })
    @IsNumber(
        { maxDecimalPlaces: 2 },
        { message: i18nValidationMessage("validation.number") },
    )
    @Min(0, { message: i18nValidationMessage("validation.number") })
    @Max(999, { message: i18nValidationMessage("validation.number") })
    baseFare!: number;

    @ApiProperty({ example: 1.5 })
    @IsNumber(
        { maxDecimalPlaces: 2 },
        { message: i18nValidationMessage("validation.number") },
    )
    @Min(0, { message: i18nValidationMessage("validation.number") })
    @Max(999, { message: i18nValidationMessage("validation.number") })
    pricePerKm!: number;
}
