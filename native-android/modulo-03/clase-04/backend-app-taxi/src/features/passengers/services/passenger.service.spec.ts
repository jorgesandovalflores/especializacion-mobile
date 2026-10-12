import { HttpStatus } from "@nestjs/common";
import { I18nService } from "nestjs-i18n";
import { HttpCustomException } from "src/core/http/exception/http.exception";
import { PassengerDao } from "../dao/passenger.dao";
import { PassengerSignUpRequestDto } from "../dto/passenger-signup-request.dto";
import { PassengerEntity } from "../entities/passenger.entity";
import { PassengerStatus } from "../enum/passenger-status.enum";
import { PassengerService } from "./passenger.service";

const REQUEST: PassengerSignUpRequestDto = {
    givenName: "Ana",
    familyName: "Perez",
    email: "ana@example.com",
};

const buildPassenger = (
    overrides: Partial<PassengerEntity> = {},
): PassengerEntity =>
    Object.assign(new PassengerEntity(), {
        id: "passenger-1",
        phoneNumber: "51987654321",
        status: PassengerStatus.INACTIVE_REGISTER,
        ...overrides,
    });

describe("PassengerService", () => {
    let passengerDao: jest.Mocked<
        Pick<
            PassengerDao,
            "findById" | "isEmailTakenByAnother" | "completeRegistration"
        >
    >;
    let service: PassengerService;

    beforeEach(() => {
        passengerDao = {
            findById: jest.fn().mockResolvedValue(buildPassenger()),
            isEmailTakenByAnother: jest.fn().mockResolvedValue(false),
            completeRegistration: jest.fn().mockResolvedValue(
                buildPassenger({
                    ...REQUEST,
                    status: PassengerStatus.ACTIVE,
                }),
            ),
        };
        const i18n = { t: jest.fn((key: string) => Promise.resolve(key)) };

        service = new PassengerService(
            passengerDao as unknown as PassengerDao,
            i18n as unknown as I18nService,
        );
    });

    const rejection = async () =>
        service.signUp("passenger-1", REQUEST).then(
            () => {
                throw new Error("Se esperaba una excepción");
            },
            (error: HttpCustomException) => error,
        );

    it("saves the profile and returns the passenger as ACTIVE", async () => {
        const result = await service.signUp("passenger-1", REQUEST);

        expect(passengerDao.completeRegistration).toHaveBeenCalledWith(
            "passenger-1",
            "Ana",
            "Perez",
            "ana@example.com",
        );
        expect(result).toEqual({
            id: "passenger-1",
            phoneNumber: "51987654321",
            givenName: "Ana",
            familyName: "Perez",
            email: "ana@example.com",
            photoUrl: null,
            status: PassengerStatus.ACTIVE,
        });
    });

    it("rejects with 401 when the token passenger no longer exists", async () => {
        passengerDao.findById.mockResolvedValue(null);

        const error = await rejection();

        expect(error.getStatus()).toBe(HttpStatus.UNAUTHORIZED);
        expect(passengerDao.completeRegistration).not.toHaveBeenCalled();
    });

    it("rejects with 422 when another passenger already uses the email", async () => {
        passengerDao.isEmailTakenByAnother.mockResolvedValue(true);

        const error = await rejection();

        expect(error.getStatus()).toBe(HttpStatus.UNPROCESSABLE_ENTITY);
        expect(error.getResponse()).toEqual({
            status_code: 422,
            message: "signup.emailAlreadyUsed",
        });
        expect(passengerDao.completeRegistration).not.toHaveBeenCalled();
    });

    it("rejects with 422 when the email is taken by a parallel request", async () => {
        passengerDao.completeRegistration.mockResolvedValue(null);

        const error = await rejection();

        expect(error.getStatus()).toBe(HttpStatus.UNPROCESSABLE_ENTITY);
    });
});
