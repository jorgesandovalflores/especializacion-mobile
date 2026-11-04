import { Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { SMS_TIMEOUT_MS, SmsSender } from "./sms-sender";

const BREVO_SMS_URL = "https://api.brevo.com/v3/transactionalSMS/sms";
const BREVO_TAG_SMS = "accountValidation";

/* -------------------------------------------------------
   Brevo: POST /v3/transactionalSMS/sms con header api-key
   Éxito: HTTP 2xx con messageId
-------------------------------------------------------- */
@Injectable()
export class BrevoSmsService extends SmsSender {
    protected readonly providerName = "Brevo";

    constructor(config: ConfigService) {
        super(config);
    }

    protected isConfigured(): boolean {
        return !!this.config.get<string>("BREVO_API_KEY");
    }

    protected messagePrefix(): string {
        return this.config.get<string>("BREVO_TEXT_SMS") ?? "";
    }

    protected async deliver(
        msisdn: string,
        text: string,
    ): Promise<string | null> {
        const response = await fetch(BREVO_SMS_URL, {
            method: "POST",
            headers: {
                accept: "application/json",
                "api-key": this.config.get<string>("BREVO_API_KEY") ?? "",
                "content-type": "application/json",
            },
            body: JSON.stringify({
                type: "transactional",
                unicodeEnabled: true,
                sender: this.config.get<string>("BREVO_SENDER") ?? "",
                recipient: `+${msisdn}`,
                content: text,
                tag: BREVO_TAG_SMS,
            }),
            signal: AbortSignal.timeout(SMS_TIMEOUT_MS),
        });

        const data = (await response.json().catch(() => ({}))) as {
            messageId?: string | number;
            message?: string;
        };

        if (!response.ok) {
            this.logger.error(
                `Brevo respondió ${response.status}: ${data.message ?? "sin detalle"}`,
            );
            return null;
        }

        return data.messageId != null ? String(data.messageId) : null;
    }
}
