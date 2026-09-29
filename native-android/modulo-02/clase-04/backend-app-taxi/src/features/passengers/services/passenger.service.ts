import { Injectable } from "@nestjs/common";
import { PassengerDao } from "../dao/passenger.dao";

/* -------------------------------------------------------
   Service de pasajeros
   - La autenticación por OTP vive en AuthService
-------------------------------------------------------- */
@Injectable()
export class PassengerService {
    constructor(private readonly passengerDao: PassengerDao) {}
}
