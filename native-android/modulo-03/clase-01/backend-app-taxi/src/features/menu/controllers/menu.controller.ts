import {
    Controller,
    Get,
    HttpCode,
    HttpStatus,
    Param,
    ParseEnumPipe,
    UseGuards,
} from "@nestjs/common";
import {
    ApiBadRequestResponse,
    ApiBearerAuth,
    ApiOkResponse,
    ApiOperation,
    ApiParam,
    ApiTags,
    ApiUnauthorizedResponse,
} from "@nestjs/swagger";
import { AccessTokenGuard } from "src/core/http/guard/access-token.guard";
import { MenuService } from "../services/menu.service";
import { MenuApplication } from "../enum/menu-application.enum";
import { MenuDto } from "../dto/menu.dto";

@ApiTags("Menu")
@ApiBearerAuth()
@Controller("menu")
export class MenuController {
    constructor(private readonly menuService: MenuService) {}

    @Get("active/:application")
    @UseGuards(AccessTokenGuard)
    @HttpCode(HttpStatus.OK)
    @ApiOperation({
        summary: "List active menus by application (ordered by `order` ASC)",
    })
    @ApiParam({
        name: "application",
        enum: MenuApplication,
        enumName: "MenuApplication",
    })
    @ApiOkResponse({ type: MenuDto, isArray: true })
    @ApiBadRequestResponse({ description: "Unknown application" })
    @ApiUnauthorizedResponse({
        description: "Missing, invalid or expired access token",
    })
    async getActiveByApplication(
        @Param("application", new ParseEnumPipe(MenuApplication))
        application: MenuApplication,
    ): Promise<MenuDto[]> {
        return this.menuService.getActiveMenusByApplication(application);
    }
}
