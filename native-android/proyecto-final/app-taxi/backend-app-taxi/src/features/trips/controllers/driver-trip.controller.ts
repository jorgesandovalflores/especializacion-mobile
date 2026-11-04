import {
    Body,
    Controller,
    Get,
    HttpCode,
    HttpStatus,
    Param,
    ParseUUIDPipe,
    Post,
    Put,
    Req,
    UseGuards,
} from "@nestjs/common";
import {
    ApiBearerAuth,
    ApiOkResponse,
    ApiOperation,
    ApiTags,
} from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import {
    AccessTokenPayload,
    DriverAccessGuard,
} from "src/core/http/guard/access-token.guard";
import {
    OfferRequestDto,
    RatingRequestDto,
    TripStatusRequestDto,
} from "../dto/trip-request.dto";
import { DriverCurrentTripDto, OfferDto, TripDto } from "../dto/trip.dto";
import { DriverTripService } from "../services/driver-trip.service";

type AuthRequest = { user: AccessTokenPayload };

@ApiTags("Driver")
@ApiBearerAuth()
@UseGuards(DriverAccessGuard)
@Controller("driver/trips")
export class DriverTripController {
    constructor(private readonly service: DriverTripService) {}

    @Get("requests")
    @ApiOperation({ summary: "Open nearby requests without an offer from me" })
    @ApiOkResponse({ type: [TripDto] })
    async requests(@Req() request: AuthRequest): Promise<TripDto[]> {
        return this.service.openRequests(request.user.sub);
    }

    @Get("current")
    @ApiOkResponse({ type: DriverCurrentTripDto })
    async current(@Req() request: AuthRequest): Promise<DriverCurrentTripDto> {
        return this.service.current(request.user.sub);
    }

    @Post(":id/offers")
    @ApiOperation({ summary: "Send a price offer for a requested trip" })
    @ApiOkResponse({ type: OfferDto })
    async offer(
        @Req() request: AuthRequest,
        @Param("id", ParseUUIDPipe) id: string,
        @Body() dto: OfferRequestDto,
        @I18nLang() lang: string,
    ): Promise<OfferDto> {
        return this.service.offer(request.user.sub, id, dto, lang);
    }

    @Put(":id/status")
    @ApiOperation({ summary: "ARRIVED → IN_PROGRESS → COMPLETED" })
    @ApiOkResponse({ type: TripDto })
    async status(
        @Req() request: AuthRequest,
        @Param("id", ParseUUIDPipe) id: string,
        @Body() dto: TripStatusRequestDto,
        @I18nLang() lang: string,
    ): Promise<TripDto> {
        return this.service.changeStatus(request.user.sub, id, dto, lang);
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
