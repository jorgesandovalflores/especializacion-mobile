import { ExecutionContext, HttpStatus } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import { I18nService } from "nestjs-i18n";
import { HttpCustomException } from "../exception/http.exception";
import { AccessTokenGuard } from "./access-token.guard";

const contextFor = (request: Record<string, unknown>): ExecutionContext =>
    ({
        switchToHttp: () => ({ getRequest: () => request }),
    }) as unknown as ExecutionContext;

describe("AccessTokenGuard", () => {
    let jwt: { verifyAsync: jest.Mock };
    let guard: AccessTokenGuard;

    beforeEach(() => {
        jwt = {
            verifyAsync: jest
                .fn()
                .mockResolvedValue({ sub: "passenger-1", typ: "passenger" }),
        };
        const config = { get: jest.fn(() => "secret") };
        const i18n = { t: jest.fn((key: string) => Promise.resolve(key)) };

        guard = new AccessTokenGuard(
            jwt as unknown as JwtService,
            config as unknown as ConfigService,
            i18n as unknown as I18nService,
        );
    });

    const rejection = async (request: Record<string, unknown>) =>
        guard.canActivate(contextFor(request)).then(
            () => {
                throw new Error("Se esperaba una excepción");
            },
            (error: HttpCustomException) => error,
        );

    it("accepts a valid access token and exposes the payload", async () => {
        const request: Record<string, unknown> = {
            headers: { authorization: "Bearer good-token" },
        };

        await expect(guard.canActivate(contextFor(request))).resolves.toBe(
            true,
        );
        expect(jwt.verifyAsync).toHaveBeenCalledWith("good-token", {
            secret: "secret",
        });
        expect(request.user).toEqual({ sub: "passenger-1", typ: "passenger" });
    });

    it("rejects a request without Authorization header with 401", async () => {
        const error = await rejection({ headers: {} });

        expect(error).toBeInstanceOf(HttpCustomException);
        expect(error.getStatus()).toBe(HttpStatus.UNAUTHORIZED);
        expect(error.getResponse()).toEqual({
            status_code: 401,
            message: "auth.unauthorized",
        });
        expect(jwt.verifyAsync).not.toHaveBeenCalled();
    });

    it("rejects an invalid or expired token with 401", async () => {
        jwt.verifyAsync.mockRejectedValue(new Error("jwt expired"));

        const error = await rejection({
            headers: { authorization: "Bearer expired" },
        });

        expect(error.getStatus()).toBe(HttpStatus.UNAUTHORIZED);
    });

    it("rejects a refresh token used as access token", async () => {
        jwt.verifyAsync.mockResolvedValue({
            sub: "passenger-1",
            typ: "passenger",
            rt: true,
        });

        const error = await rejection({
            headers: { authorization: "Bearer refresh" },
        });

        expect(error.getStatus()).toBe(HttpStatus.UNAUTHORIZED);
    });
});
