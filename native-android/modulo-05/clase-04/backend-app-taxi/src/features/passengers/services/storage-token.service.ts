import { HttpStatus, Injectable } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import { FirebaseAdminService } from "src/core/firebase/firebase-admin.service";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { StorageTokenResponseDto } from "../dto/storage-token-response.dto";
import { passengerAvatarPath } from "../utils/passenger-avatar";

/* -------------------------------------------------------
   Puente entre nuestra sesión (JWT propio) y Firebase
   - La app no inicia sesión con Firebase Auth: el backend
     firma un custom token con uid = id del pasajero
   - storage.rules compara request.auth.uid con la ruta
-------------------------------------------------------- */
@Injectable()
export class StorageTokenService {
    constructor(
        private readonly firebase: FirebaseAdminService,
        private readonly i18n: I18nService,
    ) {}

    async create(
        passengerId: string,
        lang?: string,
    ): Promise<StorageTokenResponseDto> {
        if (!this.firebase.isConfigured()) {
            throw new HttpCustomException(
                await this.i18n.t("photo.notConfigured", { lang }),
                HttpStatus.SERVICE_UNAVAILABLE,
            );
        }

        return {
            token: await this.firebase.createCustomToken(passengerId, {
                role: "passenger",
            }),
            bucket: this.firebase.storageBucket(),
            photoPath: passengerAvatarPath(passengerId),
        };
    }
}
