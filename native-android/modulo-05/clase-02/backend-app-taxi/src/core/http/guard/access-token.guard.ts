import {
    CanActivate,
    ExecutionContext,
    HttpStatus,
    Injectable,
} from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import { I18nService } from "nestjs-i18n";
import { HttpCustomException } from "../exception/http.exception";

export interface AccessTokenPayload {
    sub: string;
    typ: string;
    phone?: string;
}

interface AuthenticatedRequest {
    headers: Record<string, string | undefined>;
    user?: AccessTokenPayload;
}

/* -------------------------------------------------------
   Guard del access token (JWT)
   - Lee el header Authorization: Bearer <token>
   - Verifica firma y expiración con JWT_ACCESS_SECRET
   - Rechaza los refresh tokens (rt: true)
   - Deja el payload en request.user
-------------------------------------------------------- */
@Injectable()
export class AccessTokenGuard implements CanActivate {
    constructor(
        private readonly jwt: JwtService,
        private readonly config: ConfigService,
        private readonly i18n: I18nService,
    ) {}

    async canActivate(context: ExecutionContext): Promise<boolean> {
        const request = context
            .switchToHttp()
            .getRequest<AuthenticatedRequest>();
        const header = request.headers.authorization;

        if (!header?.startsWith("Bearer ")) {
            throw await this.unauthorized(request);
        }

        let payload: AccessTokenPayload & { rt?: boolean };
        try {
            payload = await this.jwt.verifyAsync<
                AccessTokenPayload & { rt?: boolean }
            >(header.slice(7).trim(), {
                secret: this.config.get<string>("JWT_ACCESS_SECRET"),
            });
        } catch {
            throw await this.unauthorized(request);
        }

        if (!payload?.sub || !payload?.typ || payload.rt) {
            throw await this.unauthorized(request);
        }

        request.user = payload;
        return true;
    }

    private async unauthorized(
        request: AuthenticatedRequest,
    ): Promise<HttpCustomException> {
        const message: string = await this.i18n.t("auth.unauthorized", {
            lang: request.headers["accept-language"] || "es",
        });
        return new HttpCustomException(message, HttpStatus.UNAUTHORIZED);
    }
}
