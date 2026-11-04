import { DriverDto } from "../dto/driver.dto";
import { DriverEntity } from "../entities/driver.entity";

export const toDriverDto = (entity: DriverEntity): DriverDto => ({
    id: entity.id,
    phone: entity.phoneNumber,
    givenName: entity.givenName,
    familyName: entity.familyName,
    email: entity.email ?? null,
    photoUrl: entity.photoUrl ?? null,
    status: entity.status,
    rating: Number(entity.ratingAvg),
    ratingCount: entity.ratingCount,
    vehicle: {
        plate: entity.vehiclePlate,
        brand: entity.vehicleBrand,
        model: entity.vehicleModel,
        color: entity.vehicleColor,
    },
    createdAt: entity.createdAt.toISOString(),
});
