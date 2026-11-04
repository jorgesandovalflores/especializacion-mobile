import {
    Body,
    Controller,
    Get,
    HttpCode,
    HttpStatus,
    Param,
    ParseUUIDPipe,
    Post,
    Req,
    UseGuards,
} from "@nestjs/common";
import {
    ApiBearerAuth,
    ApiOkResponse,
    ApiOperation,
    ApiServiceUnavailableResponse,
    ApiTags,
    ApiUnprocessableEntityResponse,
} from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import {
    AccessTokenPayload,
    PassengerAccessGuard,
} from "src/core/http/guard/access-token.guard";
import { TripRouteRequestDto } from "../dto/place.dto";
import { RatingRequestDto } from "../dto/trip-request.dto";
import { PassengerCurrentTripDto, QuoteDto, TripDto } from "../dto/trip.dto";
import { PassengerTripService } from "../services/passenger-trip.service";

type AuthRequest = { user: AccessTokenPayload };

@ApiTags("Trips")
@ApiBearerAuth()
@UseGuards(PassengerAccessGuard)
@Controller("trips")
export class PassengerTripController {
    constructor(private readonly service: PassengerTripService) {}

    @Post("quote")
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Route (OSRM) and estimated fare" })
    @ApiOkResponse({ type: QuoteDto })
    @ApiServiceUnavailableResponse({ description: "Route service unavailable" })
    async quote(
        @Body() dto: TripRouteRequestDto,
        @I18nLang() lang: string,
    ): Promise<QuoteDto> {
        return this.service.quote(dto, lang);
    }

    @Post()
    @ApiOperation({ summary: "Request a trip and notify nearby drivers" })
    @ApiOkResponse({ type: TripDto })
    @ApiUnprocessableEntityResponse({
        description: "Active trip already exists",
    })
    async request(
        @Req() request: AuthRequest,
        @Body() dto: TripRouteRequestDto,
        @I18nLang() lang: string,
    ): Promise<TripDto> {
        return this.service.request(request.user.sub, dto, lang);
    }

    @Get("current")
    @ApiOperation({ summary: "Active or pending-rating trip with its offers" })
    @ApiOkResponse({ type: PassengerCurrentTripDto })
    async current(
        @Req() request: AuthRequest,
    ): Promise<PassengerCurrentTripDto> {
        return this.service.current(request.user.sub);
    }

    @Post(":id/offers/:offerId/accept")
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Accept a driver offer" })
    @ApiOkResponse({ type: TripDto })
    async accept(
        @Req() request: AuthRequest,
        @Param("id", ParseUUIDPipe) id: string,
        @Param("offerId", ParseUUIDPipe) offerId: string,
        @I18nLang() lang: string,
    ): Promise<TripDto> {
        return this.service.acceptOffer(request.user.sub, id, offerId, lang);
    }

    @Post(":id/cancel")
    @HttpCode(HttpStatus.OK)
    @ApiOkResponse({ type: TripDto })
    async cancel(
        @Req() request: AuthRequest,
        @Param("id", ParseUUIDPipe) id: string,
        @I18nLang() lang: string,
    ): Promise<TripDto> {
        return this.service.cancel(request.user.sub, id, lang);
    }

    @Post(":id/rating")
    @HttpCode(HttpStatus.OK)
    @ApiOkResponse({ type: TripDto })
    async rate(
        @Req() request: AuthRequest,
        @Param("id", ParseUUIDPipe) id: string,
        @Body() dto: RatingRequestDto,
        @I18nLang() lang: string,
    ): Promise<TripDto> {
        return this.service.rate(request.user.sub, id, dto, lang);
    }
}
