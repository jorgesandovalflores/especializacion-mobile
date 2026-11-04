import { Body, Controller, Get, Put, Req, UseGuards } from "@nestjs/common";
import {
    ApiBearerAuth,
    ApiOkResponse,
    ApiOperation,
    ApiTags,
    ApiUnprocessableEntityResponse,
} from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import {
    AccessTokenPayload,
    DriverAccessGuard,
} from "src/core/http/guard/access-token.guard";
import { DriverDto } from "../dto/driver.dto";
import {
    DriverAvailabilityRequestDto,
    DriverAvailabilityResponseDto,
} from "../dto/driver-availability-request.dto";
import { DriverService } from "../services/driver.service";

@ApiTags("Driver")
@ApiBearerAuth()
@UseGuards(DriverAccessGuard)
@Controller("driver")
export class DriverController {
    constructor(private readonly service: DriverService) {}

    @Get("me")
    @ApiOperation({ summary: "Profile of the authenticated driver" })
    @ApiOkResponse({ type: DriverDto })
    async me(
        @Req() request: { user: AccessTokenPayload },
        @I18nLang() lang: string,
    ): Promise<DriverDto> {
        return this.service.me(request.user.sub, lang);
    }

    @Put("availability")
    @ApiOperation({ summary: "Start or finish the work shift" })
    @ApiOkResponse({ type: DriverAvailabilityResponseDto })
    @ApiUnprocessableEntityResponse({
        description: "Cannot go offline with an active trip",
    })
    async availability(
        @Req() request: { user: AccessTokenPayload },
        @Body() dto: DriverAvailabilityRequestDto,
        @I18nLang() lang: string,
    ): Promise<DriverAvailabilityResponseDto> {
        return this.service.setAvailability(request.user.sub, dto, lang);
    }
}
