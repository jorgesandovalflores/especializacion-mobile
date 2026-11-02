import {
    Body,
    Controller,
    Delete,
    Get,
    HttpCode,
    HttpStatus,
    Param,
    ParseUUIDPipe,
    Post,
    Put,
    Query,
    UseGuards,
} from "@nestjs/common";
import {
    ApiBearerAuth,
    ApiNoContentResponse,
    ApiOkResponse,
    ApiOperation,
    ApiTags,
} from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import { AdminAccessGuard } from "src/core/http/guard/access-token.guard";
import { PageDto, PageQueryDto } from "src/core/http/dto/page.dto";
import { AdminDriverDto, DriverDto } from "../dto/driver.dto";
import {
    DriverCreateRequestDto,
    DriverUpdateRequestDto,
} from "../dto/driver-save-request.dto";
import { DriverLocationView, DriverService } from "../services/driver.service";

@ApiTags("Admin")
@ApiBearerAuth()
@UseGuards(AdminAccessGuard)
@Controller("admin/drivers")
export class AdminDriverController {
    constructor(private readonly service: DriverService) {}

    @Get()
    @ApiOperation({ summary: "Paged list of drivers" })
    async page(@Query() query: PageQueryDto): Promise<PageDto<AdminDriverDto>> {
        return this.service.page(query);
    }

    @Get("locations")
    @ApiOperation({ summary: "Last known location of every driver" })
    async locations(): Promise<DriverLocationView[]> {
        return this.service.locations();
    }

    @Get(":id")
    @ApiOkResponse({ type: DriverDto })
    async findOne(
        @Param("id", ParseUUIDPipe) id: string,
        @I18nLang() lang: string,
    ): Promise<DriverDto> {
        return this.service.findOne(id, lang);
    }

    @Post()
    @ApiOkResponse({ type: DriverDto })
    async create(
        @Body() dto: DriverCreateRequestDto,
        @I18nLang() lang: string,
    ): Promise<DriverDto> {
        return this.service.create(dto, lang);
    }

    @Put(":id")
    @ApiOkResponse({ type: DriverDto })
    async update(
        @Param("id", ParseUUIDPipe) id: string,
        @Body() dto: DriverUpdateRequestDto,
        @I18nLang() lang: string,
    ): Promise<DriverDto> {
        return this.service.update(id, dto, lang);
    }

    @Delete(":id")
    @HttpCode(HttpStatus.NO_CONTENT)
    @ApiNoContentResponse()
    async remove(
        @Param("id", ParseUUIDPipe) id: string,
        @I18nLang() lang: string,
    ): Promise<void> {
        await this.service.remove(id, lang);
    }
}
