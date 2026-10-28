import { HttpStatus } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import { FirebaseAdminService } from "src/core/firebase/firebase-admin.service";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { StorageTokenService } from "./storage-token.service";

describe("StorageTokenService", () => {
    let firebase: jest.Mocked<
        Pick<
            FirebaseAdminService,
            "isConfigured" | "createCustomToken" | "storageBucket"
        >
    >;
    let service: StorageTokenService;

    beforeEach(() => {
        firebase = {
            isConfigured: jest.fn().mockReturnValue(true),
            createCustomToken: jest.fn().mockResolvedValue("custom-token"),
            storageBucket: jest
                .fn()
                .mockReturnValue("app-taxi.firebasestorage.app"),
        };
        const i18n = { t: jest.fn((key: string) => Promise.resolve(key)) };
        service = new StorageTokenService(
            firebase as unknown as FirebaseAdminService,
            i18n as unknown as I18nService,
        );
    });

    it("signs a custom token whose uid is the passenger id", async () => {
        const response = await service.create("passenger-1");

        expect(firebase.createCustomToken).toHaveBeenCalledWith("passenger-1", {
            role: "passenger",
        });
        expect(response).toEqual({
            token: "custom-token",
            bucket: "app-taxi.firebasestorage.app",
            photoPath: "passengers/passenger-1/avatar.jpg",
        });
    });

    it("answers 503 when Firebase is not configured", async () => {
        firebase.isConfigured.mockReturnValue(false);

        const error = await service
            .create("passenger-1")
            .catch((e: HttpCustomException) => e);

        expect(error).toBeInstanceOf(HttpCustomException);
        expect((error as HttpCustomException).getStatus()).toBe(
            HttpStatus.SERVICE_UNAVAILABLE,
        );
        expect(firebase.createCustomToken).not.toHaveBeenCalled();
    });
});
