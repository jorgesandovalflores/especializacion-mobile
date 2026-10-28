import { NestFactory } from "@nestjs/core";
import { AppModule } from "../../../app.module";
import { NotificationService } from "../services/notification.service";

const DEFAULT_DEEPLINK = "app-taxi://passenger/home";

/* -------------------------------------------------------
   Envía un push de prueba a todos los dispositivos de un
   pasajero: node dist/features/notifications/cli/send-push.js <telefono> [deeplink]
-------------------------------------------------------- */
async function main() {
    const [phone, deeplink = DEFAULT_DEEPLINK] = process.argv.slice(2);
    if (!phone) {
        console.error(
            "[push] uso: send-push <telefono 51XXXXXXXXX> [deeplink]",
        );
        process.exit(1);
    }

    const app = await NestFactory.createApplicationContext(AppModule, {
        logger: ["error", "warn"],
    });
    try {
        const result = await app.get(NotificationService).sendToPhone(phone, {
            title: "Tu taxi está llegando",
            body: "Carlos llega en 3 min · Toyota Yaris plomo · ABC-123",
            deeplink,
        });
        if (!result) {
            console.error(
                `[push] no existe un pasajero con el teléfono ${phone}`,
            );
            process.exitCode = 1;
            return;
        }
        console.log(
            `[push] enviados: ${result.sent} · tokens inválidos borrados: ${result.invalidTokens.length}`,
        );
    } finally {
        await app.close();
    }
}

main().catch((err) => {
    console.error("[push] failed:", err);
    process.exit(1);
});
