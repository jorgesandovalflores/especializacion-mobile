import { Body, Controller, HttpCode, HttpStatus, Post } from "@nestjs/common";
import {
    ApiBadRequestResponse,
    ApiForbiddenResponse,
    ApiOkResponse,
    ApiOperation,
    ApiTags,
    ApiUnauthorizedResponse,
} from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import { DriverAuthService } from "../services/driver-auth.service";
import { DriverLoginRequestDto } from "../dto/driver-login-request.dto";
import { DriverLoginResponseDto } from "../dto/driver-login-response.dto";
import { DriverRefreshRequestDto } from "../dto/driver-refresh-request.dto";

@ApiTags("Driver")
@Controller("driver/auth")
export class DriverAuthController {
    constructor(private readonly auth: DriverAuthService) {}

    @Post("login")
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Driver login with phone and password" })
    @ApiOkResponse({ type: DriverLoginResponseDto })
    @ApiBadRequestResponse({ description: "Validation error" })
    @ApiUnauthorizedResponse({ description: "Invalid credentials" })
    @ApiForbiddenResponse({ description: "Inactive driver" })
    async login(
        @Body() dto: DriverLoginRequestDto,
        @I18nLang() lang: string,
    ): Promise<DriverLoginResponseDto> {
        return this.auth.login(dto, lang);
    }

    @Post("refresh")
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Exchange a driver refresh token" })
    @ApiOkResponse({ type: DriverLoginResponseDto })
    @ApiUnauthorizedResponse({ description: "Invalid refresh token" })
    async refresh(
        @Body() dto: DriverRefreshRequestDto,
        @I18nLang() lang: string,
    ): Promise<DriverLoginResponseDto> {
        return this.auth.refresh(dto, lang);
    }
}
