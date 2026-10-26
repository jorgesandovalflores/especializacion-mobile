import {
    Body,
    Controller,
    HttpCode,
    HttpStatus,
    Put,
    Req,
    UseGuards,
} from "@nestjs/common";
import {
    ApiBadRequestResponse,
    ApiBearerAuth,
    ApiNoContentResponse,
    ApiOperation,
    ApiTags,
    ApiUnauthorizedResponse,
} from "@nestjs/swagger";
import {
    AccessTokenGuard,
    AccessTokenPayload,
} from "src/core/http/guard/access-token.guard";
import { PassengerDeviceRequestDto } from "../dto/passenger-device-request.dto";
import { NotificationService } from "../services/notification.service";

/* -------------------------------------------------------
   Dispositivos del pasajero autenticado
   - La app llama a PUT /passenger/device al entrar a Home
     y cada vez que FCM le entrega un token nuevo
-------------------------------------------------------- */
@ApiTags("passenger")
@ApiBearerAuth()
@Controller("passenger")
export class DeviceController {
    constructor(private readonly service: NotificationService) {}

    @Put("device")
    @UseGuards(AccessTokenGuard)
    @HttpCode(HttpStatus.NO_CONTENT)
    @ApiOperation({ summary: "Register the FCM token of the passenger device" })
    @ApiNoContentResponse({ description: "Device registered" })
    @ApiBadRequestResponse({ description: "Validation error" })
    @ApiUnauthorizedResponse({
        description: "Missing, invalid or expired access token",
    })
    async registerDevice(
        @Req() request: { user: AccessTokenPayload },
        @Body() dto: PassengerDeviceRequestDto,
    ): Promise<void> {
        await this.service.registerDevice(request.user.sub, dto);
    }
}
