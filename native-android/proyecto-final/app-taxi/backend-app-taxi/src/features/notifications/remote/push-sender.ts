import { Logger } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";

export interface PushMessage {
    title: string;
    body: string;
    deeplink: string;
}

export interface PushDelivery {
    sent: number;
    invalidTokens: string[];
}

/* -------------------------------------------------------
   Contrato de envío de push (mismo molde que SmsSender)
   - NotificationService depende de esta clase
   - Sin credenciales y fuera de producción: modo desarrollo
     (escribe el push en el log en vez de enviarlo)
-------------------------------------------------------- */
export abstract class PushSender {
    protected readonly logger = new Logger(this.constructor.name);

    protected constructor(protected readonly config: ConfigService) {}

    /** true si están todas las credenciales del proveedor. */
    protected abstract isConfigured(): boolean;

    /** Llama al proveedor y devuelve cuántos llegaron y qué tokens ya no sirven. */
    protected abstract deliver(
        tokens: string[],
        message: PushMessage,
    ): Promise<PushDelivery>;

    async send(tokens: string[], message: PushMessage): Promise<PushDelivery> {
        if (tokens.length === 0) return { sent: 0, invalidTokens: [] };

        if (!this.isConfigured()) {
            if (this.config.get<string>("NODE_ENV") === "production") {
                this.logger.error("FCM no tiene credenciales configuradas");
                return { sent: 0, invalidTokens: [] };
            }
            this.logger.warn(
                `[DEV] Push a ${tokens.length} dispositivo(s): ${message.title} · ${message.body} → ${message.deeplink}`,
            );
            return { sent: tokens.length, invalidTokens: [] };
        }

        try {
            return await this.deliver(tokens, message);
        } catch (error) {
            this.logger.error(
                `Error enviando push: ${(error as Error).message}`,
            );
            return { sent: 0, invalidTokens: [] };
        }
    }
}
