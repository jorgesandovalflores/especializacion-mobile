import { Body, Controller, Get, Put, Req, UseGuards } from "@nestjs/common";
import { ApiBearerAuth, ApiOkResponse, ApiTags } from "@nestjs/swagger";
import {
    AccessTokenPayload,
    AdminAccessGuard,
} from "src/core/http/guard/access-token.guard";
import { TariffDto, TariffUpdateRequestDto } from "../dto/tariff.dto";
import { TariffService } from "../services/tariff.service";

@ApiTags("Admin")
@ApiBearerAuth()
@UseGuards(AdminAccessGuard)
@Controller("admin/tariff")
export class AdminTariffController {
    constructor(private readonly service: TariffService) {}

    @Get()
    @ApiOkResponse({ type: TariffDto })
    async current(): Promise<TariffDto> {
        return this.service.current();
    }

    @Put()
    @ApiOkResponse({ type: TariffDto })
    async update(
        @Req() request: { user: AccessTokenPayload },
        @Body() dto: TariffUpdateRequestDto,
    ): Promise<TariffDto> {
        return this.service.update(request.user.sub, dto);
    }
}
