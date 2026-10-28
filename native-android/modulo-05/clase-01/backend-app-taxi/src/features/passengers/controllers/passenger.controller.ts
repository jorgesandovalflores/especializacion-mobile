import {
    Body,
    Controller,
    Get,
    HttpCode,
    HttpStatus,
    Post,
    Put,
    Req,
    UseGuards,
} from "@nestjs/common";
import {
    ApiBadRequestResponse,
    ApiBearerAuth,
    ApiOkResponse,
    ApiOperation,
    ApiServiceUnavailableResponse,
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
import { PassengerPhotoRequestDto } from "../dto/passenger-photo-request.dto";
import { StorageTokenResponseDto } from "../dto/storage-token-response.dto";
import { StorageTokenService } from "../services/storage-token.service";

/* -------------------------------------------------------
   Controller de pasajeros
   - El login por OTP está en AuthController (/auth/*)
   - El registro completa el perfil del pasajero autenticado
   - La foto se sube a Firebase Storage desde la app: aquí
     se entrega el custom token y se guarda la URL final
-------------------------------------------------------- */
@ApiTags("passenger")
@ApiBearerAuth()
@Controller("passenger")
export class PassengerController {
    constructor(
        private readonly service: PassengerService,
        private readonly storageToken: StorageTokenService,
    ) {}

    @Get("me")
    @UseGuards(AccessTokenGuard)
    @ApiOperation({ summary: "Profile of the authenticated passenger" })
    @ApiOkResponse({ type: PassengerDto })
    @ApiUnauthorizedResponse({
        description: "Missing, invalid or expired access token",
    })
    async me(
        @Req() request: { user: AccessTokenPayload },
        @I18nLang() lang: string,
    ): Promise<PassengerDto> {
        return this.service.me(request.user.sub, lang);
    }

    @Post("storage-token")
    @UseGuards(AccessTokenGuard)
    @HttpCode(HttpStatus.OK)
    @ApiOperation({
        summary: "Firebase custom token to upload the profile photo",
    })
    @ApiOkResponse({ type: StorageTokenResponseDto })
    @ApiUnauthorizedResponse({
        description: "Missing, invalid or expired access token",
    })
    @ApiServiceUnavailableResponse({ description: "Firebase not configured" })
    async createStorageToken(
        @Req() request: { user: AccessTokenPayload },
        @I18nLang() lang: string,
    ): Promise<StorageTokenResponseDto> {
        return this.storageToken.create(request.user.sub, lang);
    }

    @Put("photo")
    @UseGuards(AccessTokenGuard)
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Save the profile photo already uploaded" })
    @ApiOkResponse({ type: PassengerDto })
    @ApiBadRequestResponse({ description: "Validation error" })
    @ApiUnauthorizedResponse({
        description: "Missing, invalid or expired access token",
    })
    @ApiUnprocessableEntityResponse({
        description: "The URL is not the avatar of this passenger",
    })
    async updatePhoto(
        @Req() request: { user: AccessTokenPayload },
        @Body() dto: PassengerPhotoRequestDto,
        @I18nLang() lang: string,
    ): Promise<PassengerDto> {
        return this.service.updatePhoto(request.user.sub, dto, lang);
    }

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
