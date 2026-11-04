import { ApiProperty } from "@nestjs/swagger";
import { Transform } from "class-transformer";
import { IsString, Length, Matches } from "class-validator";
import { i18nValidationMessage } from "nestjs-i18n";

export class DriverLoginRequestDto {
    @ApiProperty({ example: "51900000001" })
    @Transform(({ value }: { value: unknown }) =>
        typeof value === "string" ? value.trim() : value,
    )
    @Matches(/^\d{11}$/, { message: i18nValidationMessage("validation.phone") })
    phone!: string;

    @ApiProperty({ example: "Driver123*" })
    @IsString({ message: i18nValidationMessage("validation.string") })
    @Length(8, 64, { message: i18nValidationMessage("validation.password") })
    password!: string;
}
