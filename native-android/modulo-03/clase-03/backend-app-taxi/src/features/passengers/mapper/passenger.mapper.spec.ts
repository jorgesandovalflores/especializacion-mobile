import { toPassengerDto } from "./passenger.mapper";
import { PassengerEntity } from "../entities/passenger.entity";
import { PassengerStatus } from "../enum/passenger-status.enum";

describe("toPassengerDto", () => {
    it("maps only the public passenger fields", () => {
        const entity = new PassengerEntity();
        entity.id = "0b0f5b0e-6a55-4f1c-9a49-0d6f6c1f9f10";
        entity.phoneNumber = "51987654321";
        entity.givenName = "Ana";
        entity.familyName = "Perez";
        entity.email = "ana@example.com";
        entity.photoUrl = null;
        entity.status = PassengerStatus.ACTIVE;
        entity.lastLoginAt = new Date();
        entity.createdAt = new Date();
        entity.updatedAt = new Date();
        entity.deletedAt = null;

        expect(toPassengerDto(entity)).toEqual({
            id: "0b0f5b0e-6a55-4f1c-9a49-0d6f6c1f9f10",
            phoneNumber: "51987654321",
            givenName: "Ana",
            familyName: "Perez",
            email: "ana@example.com",
            photoUrl: null,
            status: PassengerStatus.ACTIVE,
        });
    });

    it("normalizes missing optional fields to null", () => {
        const entity = new PassengerEntity();
        entity.id = "0b0f5b0e-6a55-4f1c-9a49-0d6f6c1f9f10";
        entity.phoneNumber = "51987654321";
        entity.status = PassengerStatus.SUSPENDED;

        expect(toPassengerDto(entity)).toEqual({
            id: "0b0f5b0e-6a55-4f1c-9a49-0d6f6c1f9f10",
            phoneNumber: "51987654321",
            givenName: null,
            familyName: null,
            email: null,
            photoUrl: null,
            status: PassengerStatus.SUSPENDED,
        });
    });
});
