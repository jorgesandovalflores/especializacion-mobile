import { Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { App, cert, getApps, initializeApp } from "firebase-admin/app";
import { getMessaging } from "firebase-admin/messaging";
import { PushDelivery, PushMessage, PushSender } from "./push-sender";

const FIREBASE_APP_NAME = "app-taxi";
const INVALID_TOKEN_CODES = [
    "messaging/registration-token-not-registered",
    "messaging/invalid-registration-token",
];

/* -------------------------------------------------------
   Firebase Cloud Messaging con firebase-admin
   - Credenciales de una cuenta de servicio en variables:
     FIREBASE_PROJECT_ID, FIREBASE_CLIENT_EMAIL, FIREBASE_PRIVATE_KEY
   - Envía mensajes de solo datos (data): la app siempre
     arma la notificación, esté abierta o cerrada
-------------------------------------------------------- */
@Injectable()
export class FcmPushService extends PushSender {
    constructor(config: ConfigService) {
        super(config);
    }

    protected isConfigured(): boolean {
        return (
            !!this.config.get<string>("FIREBASE_PROJECT_ID") &&
            !!this.config.get<string>("FIREBASE_CLIENT_EMAIL") &&
            !!this.config.get<string>("FIREBASE_PRIVATE_KEY")
        );
    }

    protected async deliver(
        tokens: string[],
        message: PushMessage,
    ): Promise<PushDelivery> {
        const response = await getMessaging(this.app()).sendEachForMulticast({
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

    private app(): App {
        return (
            getApps().find((app) => app.name === FIREBASE_APP_NAME) ??
            initializeApp(
                {
                    credential: cert({
                        projectId: this.config.get<string>(
                            "FIREBASE_PROJECT_ID",
                        ),
                        clientEmail: this.config.get<string>(
                            "FIREBASE_CLIENT_EMAIL",
                        ),
                        privateKey: this.config
                            .get<string>("FIREBASE_PRIVATE_KEY")
                            ?.replace(/\\n/g, "\n"),
                    }),
                },
                FIREBASE_APP_NAME,
            )
        );
    }
}
