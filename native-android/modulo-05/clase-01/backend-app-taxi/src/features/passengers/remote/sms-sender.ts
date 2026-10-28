import { Logger } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";

export const SMS_DEV_MESSAGE_ID = "dev-log";
export const SMS_TIMEOUT_MS = 5_000;

/* -------------------------------------------------------
   Contrato de envío de SMS (patrón Strategy)
   - AuthService depende de esta clase, no de un proveedor
   - Cada proveedor implementa isConfigured() y deliver()
   - Sin credenciales y fuera de producción: modo desarrollo
-------------------------------------------------------- */
export abstract class SmsSender {
    protected readonly logger = new Logger(this.constructor.name);

    protected constructor(protected readonly config: ConfigService) {}

    /** Nombre legible del proveedor para los logs. */
    protected abstract readonly providerName: string;

    /** true si están todas las credenciales del proveedor. */
    protected abstract isConfigured(): boolean;

    /** Texto que precede al código (p. ej. "Tu código de App Taxi es:"). */
    protected abstract messagePrefix(): string;

    /** Llama a la API del proveedor. Devuelve el id del mensaje o null si el proveedor lo rechaza. */
    protected abstract deliver(
        msisdn: string,
        text: string,
    ): Promise<string | null>;

    /** Envía el código OTP. Devuelve el id del mensaje o null si no se pudo enviar. */
    async sendOtp(msisdn: string, code: string): Promise<string | null> {
        if (!this.isConfigured()) {
            if (this.config.get<string>("NODE_ENV") === "production") {
                this.logger.error(
                    `${this.providerName} no tiene credenciales configuradas`,
                );
                return null;
            }
            this.logger.warn(`[DEV] OTP para +${msisdn}: ${code}`);
            return SMS_DEV_MESSAGE_ID;
        }

        try {
            return await this.deliver(
                msisdn,
                `${this.messagePrefix()} ${code}`.trim(),
            );
        } catch (error) {
            this.logger.error(
                `Error enviando SMS con ${this.providerName}: ${(error as Error).message}`,
            );
            return null;
        }
    }
}
