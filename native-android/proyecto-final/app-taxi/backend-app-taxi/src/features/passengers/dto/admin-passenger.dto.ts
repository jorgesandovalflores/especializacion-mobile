import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger";
import { PassengerStatus } from "../enum/passenger-status.enum";

export class AdminPassengerDto {
    @ApiProperty({ format: "uuid" })
    id!: string;

    @ApiProperty({ example: "51980773166" })
    phone!: string;

    @ApiPropertyOptional()
    givenName!: string | null;

    @ApiPropertyOptional()
    familyName!: string | null;

    @ApiPropertyOptional()
    email!: string | null;

    @ApiPropertyOptional()
    photoUrl!: string | null;

    @ApiProperty({ enum: PassengerStatus, enumName: "PassengerStatus" })
    status!: PassengerStatus;

    @ApiPropertyOptional()
    lastLoginAt!: string | null;

    @ApiProperty()
    createdAt!: string;
}
