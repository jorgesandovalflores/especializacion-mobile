import { AdminPassengerDto } from "../dto/admin-passenger.dto";
import { PassengerDto } from "../dto/passenger.dto";
import { PassengerEntity } from "../entities/passenger.entity";

export const toPassengerDto = (entity: PassengerEntity): PassengerDto => {
    return {
        id: entity.id,
        phoneNumber: entity.phoneNumber,
        givenName: entity.givenName ?? null,
        familyName: entity.familyName ?? null,
        email: entity.email ?? null,
        photoUrl: entity.photoUrl ?? null,
        status: entity.status,
    };
};

export const toAdminPassengerDto = (
    entity: PassengerEntity,
): AdminPassengerDto => ({
    id: entity.id,
    phone: entity.phoneNumber,
    givenName: entity.givenName ?? null,
    familyName: entity.familyName ?? null,
    email: entity.email ?? null,
    photoUrl: entity.photoUrl ?? null,
    status: entity.status,
    lastLoginAt: entity.lastLoginAt ? entity.lastLoginAt.toISOString() : null,
    createdAt: entity.createdAt.toISOString(),
});
