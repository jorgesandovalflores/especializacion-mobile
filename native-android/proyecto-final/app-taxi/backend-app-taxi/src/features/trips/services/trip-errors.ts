import { HttpStatus } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import { HttpCustomException } from "src/core/http/exception/http.exception";

export const tripError = async (
    i18n: I18nService,
    key: string,
    lang?: string,
    status: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
): Promise<HttpCustomException> =>
    new HttpCustomException(await i18n.t(`trip.${key}`, { lang }), status);
