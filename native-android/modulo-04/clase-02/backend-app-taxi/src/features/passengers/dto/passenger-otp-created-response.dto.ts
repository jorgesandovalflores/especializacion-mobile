import { ApiProperty } from "@nestjs/swagger";

/* -------------------------------------------------------
   Respuesta de generación de OTP
   - expiresAt en ISO-8601 (UTC)
-------------------------------------------------------- */
export class PassengerOtpCreatedResponseDto {
    @ApiProperty({ example: true })
    success!: boolean;

    @ApiProperty({ example: "2026-09-28T15:02:00.000Z" })
    expiresAt!: string;

    @ApiProperty({ example: 120, description: "OTP time to live in seconds" })
    ttlSec!: number;

    @ApiProperty({ example: "abc123", nullable: true })
    messageId!: string | null;
}
