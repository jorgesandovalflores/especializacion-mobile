import {
    Body,
    Controller,
    HttpCode,
    HttpStatus,
    Put,
    Req,
    UseGuards,
} from "@nestjs/common";
import {
    ApiBadRequestResponse,
    ApiBearerAuth,
    ApiOkResponse,
    ApiOperation,
    ApiTags,
    ApiUnauthorizedResponse,
    ApiUnprocessableEntityResponse,
} from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import {
    AccessTokenGuard,
    AccessTokenPayload,
} from "src/core/http/guard/access-token.guard";
import { PassengerService } from "../services/passenger.service";
import { PassengerSignUpRequestDto } from "../dto/passenger-signup-request.dto";
import { PassengerDto } from "../dto/passenger.dto";

/* -------------------------------------------------------
   Controller de pasajeros
   - El login por OTP está en AuthController (/auth/*)
   - El registro completa el perfil del pasajero autenticado
-------------------------------------------------------- */
@ApiTags("passenger")
@ApiBearerAuth()
@Controller("passenger")
export class PassengerController {
    constructor(private readonly service: PassengerService) {}

    @Put("signup")
    @UseGuards(AccessTokenGuard)
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Complete the passenger profile and activate it" })
    @ApiOkResponse({ type: PassengerDto })
    @ApiBadRequestResponse({ description: "Validation error" })
    @ApiUnauthorizedResponse({
        description: "Missing, invalid or expired access token",
    })
    @ApiUnprocessableEntityResponse({ description: "Email already used" })
    async signUp(
        @Req() request: { user: AccessTokenPayload },
        @Body() dto: PassengerSignUpRequestDto,
        @I18nLang() lang: string,
    ): Promise<PassengerDto> {
        return this.service.signUp(request.user.sub, dto, lang);
    }
}
