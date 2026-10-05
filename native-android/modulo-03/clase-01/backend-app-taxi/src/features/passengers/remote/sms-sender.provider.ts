import { Provider } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { SmsSender } from "./sms-sender";
import { BrevoSmsService } from "./brevo-sms.service";
import { LabsMobileSmsService } from "./labsmobile-sms.service";

export const SMS_PROVIDERS = ["brevo", "labsmobile"] as const;
export type SmsProviderName = (typeof SMS_PROVIDERS)[number];

/* -------------------------------------------------------
   Elige la implementación de SmsSender según SMS_PROVIDER
   (por defecto "brevo"). Un valor desconocido detiene el
   arranque para no descubrir el error al enviar el primer SMS.
-------------------------------------------------------- */
export const smsSenderProvider: Provider = {
    provide: SmsSender,
    inject: [ConfigService, BrevoSmsService, LabsMobileSmsService],
    useFactory: (
        config: ConfigService,
        brevo: BrevoSmsService,
        labsMobile: LabsMobileSmsService,
    ): SmsSender => {
        const provider = (config.get<string>("SMS_PROVIDER") ?? "brevo")
            .trim()
            .toLowerCase();
        switch (provider as SmsProviderName) {
            case "brevo":
                return brevo;
            case "labsmobile":
                return labsMobile;
            default:
                throw new Error(
                    `SMS_PROVIDER inválido: "${provider}". Valores permitidos: ${SMS_PROVIDERS.join(", ")}`,
                );
        }
    },
};
