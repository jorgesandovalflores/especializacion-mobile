import { Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { getMessaging } from "firebase-admin/messaging";
import { FirebaseAdminService } from "src/core/firebase/firebase-admin.service";
import { PushDelivery, PushMessage, PushSender } from "./push-sender";

const INVALID_TOKEN_CODES = [
    "messaging/registration-token-not-registered",
    "messaging/invalid-registration-token",
];

/* -------------------------------------------------------
   Firebase Cloud Messaging con firebase-admin
   - Credenciales en FirebaseAdminService (core/firebase)
   - Envía mensajes de solo datos (data): la app siempre
     arma la notificación, esté abierta o cerrada
-------------------------------------------------------- */
@Injectable()
export class FcmPushService extends PushSender {
    constructor(
        config: ConfigService,
        private readonly firebase: FirebaseAdminService,
    ) {
        super(config);
    }

    protected isConfigured(): boolean {
        return this.firebase.isConfigured();
    }

    protected async deliver(
        tokens: string[],
        message: PushMessage,
    ): Promise<PushDelivery> {
        const response = await getMessaging(
            this.firebase.app(),
        ).sendEachForMulticast({
            tokens,
            data: {
                title: message.title,
                body: message.body,
                deeplink: message.deeplink,
            },
            android: { priority: "high" },
        });

        const invalidTokens = response.responses.flatMap((result, index) =>
            !result.success &&
            INVALID_TOKEN_CODES.includes(result.error?.code ?? "")
                ? [tokens[index]]
                : [],
        );

        return { sent: response.successCount, invalidTokens };
    }
}
