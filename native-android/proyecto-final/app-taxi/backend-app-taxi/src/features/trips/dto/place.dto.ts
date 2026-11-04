import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger";
import { Transform, Type } from "class-transformer";
import {
    IsLatitude,
    IsLongitude,
    IsOptional,
    IsString,
    MaxLength,
    ValidateNested,
} from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

export class PlaceDto {
    @ApiProperty({ example: -12.0464 })
    @IsLatitude({ message: i18nValidationMessage("validation.coordinate") })
    lat!: number;

    @ApiProperty({ example: -77.0428 })
    @IsLongitude({ message: i18nValidationMessage("validation.coordinate") })
    lng!: number;

    @ApiPropertyOptional({ example: "Plaza San Martín" })
    @IsOptional()
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string" ? value.trim() || undefined : value,
    )
    @IsString({ message: i18nValidationMessage("validation.string") })
    @MaxLength(255, { message: i18nValidationMessage("validation.length") })
    address?: string;
}

export class TripRouteRequestDto {
    @ApiProperty({ type: PlaceDto })
    @ValidateNested({ message: i18nValidationMessage("validation.object") })
    @Type(() => PlaceDto)
    origin!: PlaceDto;

    @ApiProperty({ type: PlaceDto })
    @ValidateNested({ message: i18nValidationMessage("validation.object") })
    @Type(() => PlaceDto)
    destination!: PlaceDto;
}
