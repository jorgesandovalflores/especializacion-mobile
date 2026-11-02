import {
    Controller,
    Get,
    Param,
    ParseUUIDPipe,
    Query,
    UseGuards,
} from "@nestjs/common";
import { ApiBearerAuth, ApiOkResponse, ApiTags } from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import { AdminAccessGuard } from "src/core/http/guard/access-token.guard";
import { PageDto } from "src/core/http/dto/page.dto";
import { TripPageQueryDto } from "../dto/trip-request.dto";
import { AdminTripDetailDto, TripDto } from "../dto/trip.dto";
import { AdminTripService } from "../services/admin-trip.service";

@ApiTags("Admin")
@ApiBearerAuth()
@UseGuards(AdminAccessGuard)
@Controller("admin/trips")
export class AdminTripController {
    constructor(private readonly service: AdminTripService) {}

    @Get()
    async page(@Query() query: TripPageQueryDto): Promise<PageDto<TripDto>> {
        return this.service.page(query);
    }

    @Get("active")
    @ApiOkResponse({ type: [TripDto] })
    async active(): Promise<TripDto[]> {
        return this.service.active();
    }

    @Get(":id")
    @ApiOkResponse({ type: AdminTripDetailDto })
    async detail(
        @Param("id", ParseUUIDPipe) id: string,
        @I18nLang() lang: string,
    ): Promise<AdminTripDetailDto> {
        return this.service.detail(id, lang);
    }
}
