import {
    ExceptionFilter,
    Catch,
    ArgumentsHost,
    HttpException,
    HttpStatus,
    Logger,
} from "@nestjs/common";
import { Request, Response } from "express";
import { I18nService } from "nestjs-i18n";

@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
    private readonly logger = new Logger(HttpExceptionFilter.name);

    constructor(private readonly i18n: I18nService) {}

    async catch(exception: unknown, host: ArgumentsHost) {
        const ctx = host.switchToHttp();
        const response = ctx.getResponse<Response>();
        const request = ctx.getRequest<Request>();

        /* Errores no controlados (BD, Redis, bugs): mismo formato que el resto */
        if (!(exception instanceof HttpException)) {
            this.logger.error(
                exception instanceof Error
                    ? exception.stack
                    : String(exception),
            );
            return response.status(HttpStatus.INTERNAL_SERVER_ERROR).json({
                status_code: HttpStatus.INTERNAL_SERVER_ERROR,
                message: this.i18n.translate("common.internalError", {
                    lang: request.headers["accept-language"] || "es",
                }),
                errors: [],
            });
        }

        const status = exception.getStatus();
        const exceptionResponse = exception.getResponse();

        if (status === 200) {
            return response.status(status).json(exceptionResponse);
        }

        const responseBody: {
            status_code: number;
            message: string;
            errors: Array<{ field: string; message: string }>;
        } = {
            status_code: status,
            message: "Error",
            errors: [],
        };

        if (typeof exceptionResponse === "string") {
            responseBody.message = exceptionResponse;
        } else if (typeof exceptionResponse === "object") {
            const res: any = exceptionResponse;

            if (res.status_code === 422) {
                return response.status(status).json(res);
            }

            if (res.status_code === 500) {
                responseBody.status_code = 500;
            }

            if (Array.isArray(res.message)) {
                responseBody.message = "Error de validación";

                responseBody.errors = await Promise.all(
                    res.message.map(async (error: any) => {
                        if (error.property && error.constraints) {
                            const firstKey = Object.values(
                                error.constraints,
                            )[0] as string;
                            const [key, payloadRaw] = firstKey.split("|");
                            let payload = {};
                            try {
                                payload = JSON.parse(payloadRaw);
                            } catch (_) {}

                            const message = await this.i18n.translate(key, {
                                lang:
                                    request.headers["accept-language"] || "es",
                                args: payload,
                            });

                            return {
                                field: error.property,
                                message,
                            };
                        }

                        // fallback general
                        const rawMessage =
                            typeof error === "string"
                                ? error
                                : "Error desconocido";
                        const [key, payloadRaw] = rawMessage.split("|");
                        let payload = {};
                        try {
                            payload = JSON.parse(payloadRaw);
                        } catch (_) {}

                        const message = await this.i18n.translate(key, {
                            lang: request.headers["accept-language"] || "es",
                            args: payload,
                        });

                        return {
                            field: "general",
                            message,
                        };
                    }),
                );
            } else if (res.message) {
                responseBody.message = res.message;
            }

            if (res.error) {
                responseBody.message = res.error;
            }
        }

        return response.status(status).json(responseBody);
    }
}
