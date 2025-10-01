import { Body, Controller, HttpCode, HttpStatus, Post } from "@nestjs/common";
import {
    ApiBadRequestResponse,
    ApiNotFoundResponse,
    ApiOkResponse,
    ApiOperation,
    ApiTags,
} from "@nestjs/swagger";
import { PassengerLoginRequestDto } from "../dto/passenger-login-request.dto";
import { PassengerLoginResponseDto } from "../dto/passenger-login-response.dto";
import { PassengerService } from "../services/passenger.service";
import { I18nLang } from "nestjs-i18n";

/* -------------------------------------------------------
   Controller: endpoint público de login de pasajero
   - POST /passenger/login
   - Valida DTO, delega en Service y retorna tokens + user
-------------------------------------------------------- */
@ApiTags("passenger")
@Controller("passenger")
export class PassengerController {
    constructor(private readonly service: PassengerService) {}

    @Post("login")
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Passenger login by phone" })
    @ApiOkResponse({ type: PassengerLoginResponseDto })
    @ApiBadRequestResponse({ description: "Validation error" })
    @ApiNotFoundResponse({ description: "Passenger not found" })
    async login(
        @Body() body: PassengerLoginRequestDto,
        @I18nLang() lang: string,
    ): Promise<PassengerLoginResponseDto> {
        return this.service.loginByPhone(body, lang);
    }
}
