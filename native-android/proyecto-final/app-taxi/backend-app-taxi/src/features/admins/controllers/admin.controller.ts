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
    Req,
    UseGuards,
} from "@nestjs/common";
import {
    ApiBearerAuth,
    ApiNoContentResponse,
    ApiOkResponse,
    ApiTags,
} from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import {
    AccessTokenPayload,
    AdminAccessGuard,
} from "src/core/http/guard/access-token.guard";
import { AdminDto } from "../dto/admin.dto";
import {
    AdminCreateRequestDto,
    AdminUpdateRequestDto,
} from "../dto/admin-request.dto";
import { AdminService } from "../services/admin.service";

@ApiTags("Admin")
@ApiBearerAuth()
@UseGuards(AdminAccessGuard)
@Controller("admin/admins")
export class AdminController {
    constructor(private readonly service: AdminService) {}

    @Get()
    @ApiOkResponse({ type: [AdminDto] })
    async findAll(): Promise<AdminDto[]> {
        return this.service.findAll();
    }

    @Post()
    @ApiOkResponse({ type: AdminDto })
    async create(
        @Body() dto: AdminCreateRequestDto,
        @I18nLang() lang: string,
    ): Promise<AdminDto> {
        return this.service.create(dto, lang);
    }

    @Put(":id")
    @ApiOkResponse({ type: AdminDto })
    async update(
        @Req() request: { user: AccessTokenPayload },
        @Param("id", ParseUUIDPipe) id: string,
        @Body() dto: AdminUpdateRequestDto,
        @I18nLang() lang: string,
    ): Promise<AdminDto> {
        return this.service.update(request.user.sub, id, dto, lang);
    }

    @Delete(":id")
    @HttpCode(HttpStatus.NO_CONTENT)
    @ApiNoContentResponse()
    async remove(
        @Req() request: { user: AccessTokenPayload },
        @Param("id", ParseUUIDPipe) id: string,
        @I18nLang() lang: string,
    ): Promise<void> {
        await this.service.remove(request.user.sub, id, lang);
    }
}
