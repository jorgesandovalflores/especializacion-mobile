import { HttpStatus, Injectable } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { PassengerDao } from "../dao/passenger.dao";
import { PassengerDto } from "../dto/passenger.dto";
import { PassengerSignUpRequestDto } from "../dto/passenger-signup-request.dto";
import { toPassengerDto } from "../mapper/passenger.mapper";

/* -------------------------------------------------------
   Service de pasajeros
   - La autenticación por OTP vive en AuthService
-------------------------------------------------------- */
@Injectable()
export class PassengerService {
    constructor(
        private readonly passengerDao: PassengerDao,
        private readonly i18n: I18nService,
    ) {}

    /**
     * Completa el registro del pasajero autenticado.
     * - El id sale del access token (nunca del body).
     * - El email debe ser único entre pasajeros.
     * - Guarda nombres y email y deja el estado en ACTIVE.
     */
    async signUp(
        passengerId: string,
        request: PassengerSignUpRequestDto,
        lang?: string,
    ): Promise<PassengerDto> {
        const passenger = await this.passengerDao.findById(passengerId);
        if (!passenger) {
            throw new HttpCustomException(
                await this.i18n.t("auth.unauthorized", { lang }),
                HttpStatus.UNAUTHORIZED,
            );
        }

        if (
            await this.passengerDao.isEmailTakenByAnother(
                passenger.id,
                request.email,
            )
        ) {
            throw new HttpCustomException(
                await this.i18n.t("signup.emailAlreadyUsed", { lang }),
            );
        }

        const updated = await this.passengerDao.completeRegistration(
            passenger.id,
            request.givenName,
            request.familyName,
            request.email,
        );
        if (!updated) {
            throw new HttpCustomException(
                await this.i18n.t("signup.emailAlreadyUsed", { lang }),
            );
        }

        return toPassengerDto(updated);
    }
}
