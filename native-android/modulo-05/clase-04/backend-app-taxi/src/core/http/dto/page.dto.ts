import { ApiPropertyOptional } from "@nestjs/swagger";
import { Transform } from "class-transformer";
import {
    IsInt,
    IsOptional,
    IsString,
    Max,
    MaxLength,
    Min,
} from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

export class PageQueryDto {
    @ApiPropertyOptional({ example: "carlos" })
    @IsOptional()
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string" ? value.trim() : value,
    )
    @IsString({ message: i18nValidationMessage("validation.string") })
    @MaxLength(60, { message: i18nValidationMessage("validation.length") })
    search?: string;

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

export interface PageDto<T> {
    items: T[];
    total: number;
    page: number;
    size: number;
}
