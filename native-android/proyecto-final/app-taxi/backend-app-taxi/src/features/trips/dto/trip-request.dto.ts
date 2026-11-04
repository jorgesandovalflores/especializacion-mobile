import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger";
import { Transform } from "class-transformer";
import {
    IsIn,
    IsInt,
    IsNumber,
    IsOptional,
    IsString,
    Max,
    MaxLength,
    Min,
} from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";
import { TripStatus } from "../enum/trip-status.enum";

export class OfferRequestDto {
    @ApiProperty({ example: 18.5 })
    @IsNumber(
        { maxDecimalPlaces: 2 },
        { message: i18nValidationMessage("validation.price") },
    )
    @Min(1, { message: i18nValidationMessage("validation.price") })
    @Max(9999, { message: i18nValidationMessage("validation.price") })
    price!: number;
}

export class TripStatusRequestDto {
    @ApiProperty({
        enum: [
            TripStatus.ARRIVED,
            TripStatus.IN_PROGRESS,
            TripStatus.COMPLETED,
        ],
    })
    @IsIn([TripStatus.ARRIVED, TripStatus.IN_PROGRESS, TripStatus.COMPLETED], {
        message: i18nValidationMessage("validation.status"),
    })
    status!: TripStatus;
}

export class RatingRequestDto {
    @ApiProperty({ example: 5, minimum: 1, maximum: 5 })
    @IsInt({ message: i18nValidationMessage("validation.rating") })
    @Min(1, { message: i18nValidationMessage("validation.rating") })
    @Max(5, { message: i18nValidationMessage("validation.rating") })
    rating!: number;

    @ApiPropertyOptional({ example: "Muy amable" })
    @IsOptional()
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string" ? value.trim() || undefined : value,
    )
    @IsString({ message: i18nValidationMessage("validation.string") })
    @MaxLength(255, { message: i18nValidationMessage("validation.length") })
    comment?: string;
}

export class TripPageQueryDto {
    @ApiPropertyOptional({ enum: TripStatus })
    @IsOptional()
    @IsIn(Object.values(TripStatus), {
        message: i18nValidationMessage("validation.status"),
    })
    status?: TripStatus;

    @ApiPropertyOptional({ example: 1, default: 1 })
    @IsOptional()
    @IsInt({ message: i18nValidationMessage("validation.number") })
    @Min(1, { message: i18nValidationMessage("validation.number") })
    page: number = 1;

    @ApiPropertyOptional({ example: 20, default: 20 })
    @IsOptional()
    @IsInt({ message: i18nValidationMessage("validation.number") })
    @Min(1, { message: i18nValidationMessage("validation.number") })
    @Max(100, { message: i18nValidationMessage("validation.number") })
    size: number = 20;
}
