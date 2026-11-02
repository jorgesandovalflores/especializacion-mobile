import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger";
import {
    IsBoolean,
    IsLatitude,
    IsLongitude,
    IsOptional,
} from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

export class DriverAvailabilityRequestDto {
    @ApiProperty({ example: true })
    @IsBoolean({ message: i18nValidationMessage("validation.boolean") })
    online!: boolean;

    @ApiPropertyOptional({ example: -12.0464 })
    @IsOptional()
    @IsLatitude({ message: i18nValidationMessage("validation.coordinate") })
    lat?: number;

    @ApiPropertyOptional({ example: -77.0428 })
    @IsOptional()
    @IsLongitude({ message: i18nValidationMessage("validation.coordinate") })
    lng?: number;
}

export class DriverAvailabilityResponseDto {
    @ApiProperty({ example: true })
    online!: boolean;
}
