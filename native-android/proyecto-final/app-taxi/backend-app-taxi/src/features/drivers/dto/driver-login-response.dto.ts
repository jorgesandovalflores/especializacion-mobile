import { ApiProperty } from "@nestjs/swagger";
import { DriverDto } from "./driver.dto";

export class DriverLoginResponseDto {
    @ApiProperty()
    accessToken!: string;

    @ApiProperty()
    refreshToken!: string;

    @ApiProperty({ type: DriverDto })
    driver!: DriverDto;
}
