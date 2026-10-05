import { Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { SMS_TIMEOUT_MS, SmsSender } from "./sms-sender";

const LABSMOBILE_SMS_URL = "https://api.labsmobile.com/json/send";
const LABSMOBILE_SUCCESS_CODE = "0";

/* -------------------------------------------------------
   LabsMobile: POST /json/send con Basic Auth (usuario:api_key)
   Éxito: {"code":"0","message":"...","subid":"..."}
   El msisdn va sin "+" (E.164 solo dígitos)
-------------------------------------------------------- */
@Injectable()
export class LabsMobileSmsService extends SmsSender {
    protected readonly providerName = "LabsMobile";

    constructor(config: ConfigService) {
        super(config);
    }

    protected isConfigured(): boolean {
        return (
            !!this.config.get<string>("LABSMOBILE_USER") &&
            !!this.config.get<string>("LABSMOBILE_API_KEY")
        );
    }

    protected messagePrefix(): string {
        return this.config.get<string>("LABSMOBILE_TEXT_SMS") ?? "";
    }

    protected async deliver(
        msisdn: string,
        text: string,
    ): Promise<string | null> {
        const user = this.config.get<string>("LABSMOBILE_USER") ?? "";
        const apiKey = this.config.get<string>("LABSMOBILE_API_KEY") ?? "";
        const credentials = Buffer.from(`${user}:${apiKey}`).toString("base64");

        const response = await fetch(LABSMOBILE_SMS_URL, {
            method: "POST",
            headers: {
                authorization: `Basic ${credentials}`,
                "content-type": "application/json",
            },
            body: JSON.stringify({
                message: text,
                tpoa: this.config.get<string>("LABSMOBILE_SENDER") ?? "",
                recipient: [{ msisdn: msisdn.replace(/^\+/, "") }],
            }),
            signal: AbortSignal.timeout(SMS_TIMEOUT_MS),
        });

        const data = (await response.json().catch(() => ({}))) as {
            code?: string | number;
            message?: string;
            subid?: string;
        };

        if (
            !response.ok ||
            String(data.code) !== LABSMOBILE_SUCCESS_CODE ||
            !data.subid
        ) {
            this.logger.error(
                `LabsMobile respondió ${response.status} (code ${data.code ?? "-"}): ${data.message ?? "sin detalle"}`,
            );
            return null;
        }

        return data.subid;
    }
}
