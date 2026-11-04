import { Controller, Get, Query, UseGuards } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { AdminAccessGuard } from "src/core/http/guard/access-token.guard";
import { PageDto, PageQueryDto } from "src/core/http/dto/page.dto";
import { PassengerDao } from "../dao/passenger.dao";
import { AdminPassengerDto } from "../dto/admin-passenger.dto";
import { toAdminPassengerDto } from "../mapper/passenger.mapper";

@ApiTags("Admin")
@ApiBearerAuth()
@UseGuards(AdminAccessGuard)
@Controller("admin/passengers")
export class AdminPassengerController {
    constructor(private readonly passengerDao: PassengerDao) {}

    @Get()
    @ApiOperation({ summary: "Paged list of passengers" })
    async page(
        @Query() query: PageQueryDto,
    ): Promise<PageDto<AdminPassengerDto>> {
        const [items, total] = await this.passengerDao.findPage(
            query.search,
            query.page,
            query.size,
        );
        return {
            items: items.map(toAdminPassengerDto),
            total,
            page: query.page,
            size: query.size,
        };
    }
}
