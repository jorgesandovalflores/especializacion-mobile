import { Body, Controller, HttpCode, HttpStatus, Post } from "@nestjs/common";
import {
    ApiBadRequestResponse,
    ApiOkResponse,
    ApiOperation,
    ApiTags,
    ApiTooManyRequestsResponse,
    ApiUnprocessableEntityResponse,
} from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import { AuthService } from "../services/auth.service";
import { PassengerOtpCreatedRequestDto } from "../dto/passenger-otp-created-request.dto";
import { PassengerOtpCreatedResponseDto } from "../dto/passenger-otp-created-response.dto";
import { PassengerOtpValidatedRequestDto } from "../dto/passenger-otp-validated-request.dto";
import { PassengerLoginResponseDto } from "../dto/passenger-login-response.dto";

@ApiTags("Auth")
@Controller("auth")
export class AuthController {
    constructor(private readonly authService: AuthService) {}

    @Post("otp-generate")
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Generate OTP and send it via SMS" })
    @ApiOkResponse({ type: PassengerOtpCreatedResponseDto })
    @ApiBadRequestResponse({ description: "Validation error" })
    @ApiUnprocessableEntityResponse({
        description: "Active code already exists or SMS delivery failed",
    })
    async generateOtp(
        @Body() dto: PassengerOtpCreatedRequestDto,
        @I18nLang() lang: string,
    ): Promise<PassengerOtpCreatedResponseDto> {
        return this.authService.generateOtpByPhone(dto, lang);
    }

    @Post("otp-validate")
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Validate OTP and return session tokens + user" })
    @ApiOkResponse({ type: PassengerLoginResponseDto })
    @ApiBadRequestResponse({ description: "Validation error" })
    @ApiUnprocessableEntityResponse({
        description:
            "Invalid, expired or already used OTP, or passenger not found",
    })
    @ApiTooManyRequestsResponse({ description: "Too many failed attempts" })
    async validateOtp(
        @Body() dto: PassengerOtpValidatedRequestDto,
        @I18nLang() lang: string,
    ): Promise<PassengerLoginResponseDto> {
        return this.authService.verifyOtpByPhone(dto, lang);
    }
}
