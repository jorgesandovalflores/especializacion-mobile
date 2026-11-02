import { HttpStatus, Injectable } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { PassengerDao } from "../dao/passenger.dao";
import { PassengerDto } from "../dto/passenger.dto";
import { PassengerSignUpRequestDto } from "../dto/passenger-signup-request.dto";
import { PassengerPhotoRequestDto } from "../dto/passenger-photo-request.dto";
import { toPassengerDto } from "../mapper/passenger.mapper";
import { isPassengerAvatarUrl } from "../utils/passenger-avatar";

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

    /** Perfil del pasajero autenticado. */
    async me(passengerId: string, lang?: string): Promise<PassengerDto> {
        const passenger = await this.passengerDao.findById(passengerId);
        if (!passenger) {
            throw new HttpCustomException(
                await this.i18n.t("auth.unauthorized", { lang }),
                HttpStatus.UNAUTHORIZED,
            );
        }
        return toPassengerDto(passenger);
    }

    /**
     * Guarda la foto de perfil que la app ya subió a Firebase Storage.
     * - Solo acepta la URL del avatar de ESTE pasajero (passengers/{id}/avatar.jpg)
     */
    async updatePhoto(
        passengerId: string,
        request: PassengerPhotoRequestDto,
        lang?: string,
    ): Promise<PassengerDto> {
        if (!isPassengerAvatarUrl(request.photoUrl, passengerId)) {
            throw new HttpCustomException(
                await this.i18n.t("photo.invalidUrl", { lang }),
            );
        }

        const updated = await this.passengerDao.updatePhotoUrl(
            passengerId,
            request.photoUrl,
        );
        if (!updated) {
            throw new HttpCustomException(
                await this.i18n.t("auth.unauthorized", { lang }),
                HttpStatus.UNAUTHORIZED,
            );
        }
        return toPassengerDto(updated);
    }

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
