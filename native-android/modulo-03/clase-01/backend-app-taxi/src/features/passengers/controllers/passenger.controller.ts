import { Controller } from "@nestjs/common";
import { ApiTags } from "@nestjs/swagger";
import { PassengerService } from "../services/passenger.service";

/* -------------------------------------------------------
   Controller de pasajeros
   - El login por OTP está en AuthController (/auth/*)
-------------------------------------------------------- */
@ApiTags("passenger")
@Controller("passenger")
export class PassengerController {
    constructor(private readonly service: PassengerService) {}
}
