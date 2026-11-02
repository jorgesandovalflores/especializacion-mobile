import { Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { App, cert, getApps, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";

const FIREBASE_APP_NAME = "app-taxi";

/* -------------------------------------------------------
   Firebase Admin SDK compartido por toda la API
   - Credenciales de una cuenta de servicio en variables:
     FIREBASE_PROJECT_ID, FIREBASE_CLIENT_EMAIL, FIREBASE_PRIVATE_KEY
   - FIREBASE_STORAGE_BUCKET es opcional
     (por defecto <project-id>.firebasestorage.app)
-------------------------------------------------------- */
@Injectable()
export class FirebaseAdminService {
    constructor(private readonly config: ConfigService) {}

    isConfigured(): boolean {
        return (
            !!this.config.get<string>("FIREBASE_PROJECT_ID") &&
            !!this.config.get<string>("FIREBASE_CLIENT_EMAIL") &&
            !!this.config.get<string>("FIREBASE_PRIVATE_KEY")
        );
    }

    storageBucket(): string {
        return (
            this.config.get<string>("FIREBASE_STORAGE_BUCKET") ||
            `${this.config.get<string>("FIREBASE_PROJECT_ID")}.firebasestorage.app`
        );
    }

    /** Token que la app canjea con signInWithCustomToken (uid = id del pasajero). */
    createCustomToken(
        uid: string,
        claims: Record<string, unknown>,
    ): Promise<string> {
        return getAuth(this.app()).createCustomToken(uid, claims);
    }

    app(): App {
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
                    storageBucket: this.storageBucket(),
                },
                FIREBASE_APP_NAME,
            )
        );
    }
}
