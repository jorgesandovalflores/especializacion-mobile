import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger";
import { DriverStatus } from "../enum/driver-status.enum";
import { VehicleDto } from "./vehicle.dto";

export class DriverDto {
    @ApiProperty({ format: "uuid" })
    id!: string;

    @ApiProperty({ example: "51900000001" })
    phone!: string;

    @ApiProperty({ example: "Carlos" })
    givenName!: string;

    @ApiProperty({ example: "Quispe" })
    familyName!: string;

    @ApiPropertyOptional({ example: "carlos@apptaxi.pe" })
    email!: string | null;

    @ApiPropertyOptional()
    photoUrl!: string | null;

    @ApiProperty({ enum: DriverStatus, enumName: "DriverStatus" })
    status!: DriverStatus;

    @ApiProperty({ example: 4.9 })
    rating!: number;

    @ApiProperty({ example: 12 })
    ratingCount!: number;

    @ApiProperty({ type: VehicleDto })
    vehicle!: VehicleDto;

    @ApiProperty()
    createdAt!: string;
}

export class AdminDriverDto extends DriverDto {
    @ApiProperty({ example: true })
    online!: boolean;
}
