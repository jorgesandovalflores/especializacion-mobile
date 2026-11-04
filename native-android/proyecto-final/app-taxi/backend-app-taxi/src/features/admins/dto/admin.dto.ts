import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger";
import { AdminStatus } from "../enum/admin-status.enum";

export class AdminDto {
    @ApiProperty({ format: "uuid" })
    id!: string;

    @ApiProperty({ example: "Administrador App Taxi" })
    fullName!: string;

    @ApiProperty({ example: "admin@apptaxi.pe" })
    email!: string;

    @ApiProperty({ enum: AdminStatus, enumName: "AdminStatus" })
    status!: AdminStatus;

    @ApiPropertyOptional()
    lastLoginAt!: string | null;

    @ApiProperty()
    createdAt!: string;
}

export class AdminLoginResponseDto {
    @ApiProperty()
    accessToken!: string;

    @ApiProperty({ type: AdminDto })
    admin!: AdminDto;
}
