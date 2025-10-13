import { Body, Controller, HttpCode, HttpStatus, Post } from "@nestjs/common";
import { ApiTags } from "@nestjs/swagger";
import { PassengerService } from "../services/passenger.service";

/* -------------------------------------------------------
   Controller: endpoint público de login de pasajero
   - POST /passenger/login
   - Valida DTO, delega en Service y retorna tokens + user
-------------------------------------------------------- */
@ApiTags("passenger")
@Controller("passenger")
export class PassengerController {
    constructor(private readonly service: PassengerService) {}
}
