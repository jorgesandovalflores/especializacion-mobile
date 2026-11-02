import {
    Body,
    Controller,
    Get,
    HttpCode,
    HttpStatus,
    Post,
    Req,
    UseGuards,
} from "@nestjs/common";
import {
    ApiBearerAuth,
    ApiOkResponse,
    ApiOperation,
    ApiTags,
    ApiUnauthorizedResponse,
} from "@nestjs/swagger";
import { I18nLang } from "nestjs-i18n";
import {
    AccessTokenPayload,
    AdminAccessGuard,
} from "src/core/http/guard/access-token.guard";
import { AdminDto, AdminLoginResponseDto } from "../dto/admin.dto";
import { AdminLoginRequestDto } from "../dto/admin-request.dto";
import { AdminService } from "../services/admin.service";

@ApiTags("Admin")
@Controller("admin")
export class AdminAuthController {
    constructor(private readonly service: AdminService) {}

    @Post("auth/login")
    @HttpCode(HttpStatus.OK)
    @ApiOperation({ summary: "Backoffice login with email and password" })
    @ApiOkResponse({ type: AdminLoginResponseDto })
    @ApiUnauthorizedResponse({ description: "Invalid credentials" })
    async login(
        @Body() dto: AdminLoginRequestDto,
        @I18nLang() lang: string,
    ): Promise<AdminLoginResponseDto> {
        return this.service.login(dto, lang);
    }

    @Get("me")
    @ApiBearerAuth()
    @UseGuards(AdminAccessGuard)
    @ApiOkResponse({ type: AdminDto })
    async me(
        @Req() request: { user: AccessTokenPayload },
        @I18nLang() lang: string,
    ): Promise<AdminDto> {
        return this.service.me(request.user.sub, lang);
    }
}
