import { ApiProperty, ApiPropertyOptional } from "@nestjs/swagger";
import { VehicleDto } from "src/features/drivers/dto/vehicle.dto";
import { OfferStatus } from "../enum/offer-status.enum";
import { TripCancelledBy, TripStatus } from "../enum/trip-status.enum";

export class TripPlaceDto {
    @ApiProperty({ example: -12.0464 })
    lat!: number;

    @ApiProperty({ example: -77.0428 })
    lng!: number;

    @ApiPropertyOptional({ example: "Plaza San Martín" })
    address!: string | null;
}

export class QuoteDto {
    @ApiProperty({ type: TripPlaceDto })
    origin!: TripPlaceDto;

    @ApiProperty({ type: TripPlaceDto })
    destination!: TripPlaceDto;

    @ApiProperty({ example: 9838 })
    distanceMeters!: number;

    @ApiProperty({ example: 787 })
    durationSeconds!: number;

    @ApiProperty({ description: "Encoded polyline (precision 5)" })
    polyline!: string;

    @ApiProperty({ example: 19.76 })
    estimatedFare!: number;

    @ApiProperty({ example: "PEN" })
    currency!: string;
}

export class TripDriverDto {
    @ApiProperty({ format: "uuid" })
    id!: string;

    @ApiProperty()
    givenName!: string;

    @ApiProperty()
    familyName!: string;

    @ApiProperty()
    phone!: string;

    @ApiPropertyOptional()
    photoUrl!: string | null;

    @ApiProperty({ example: 4.9 })
    rating!: number;

    @ApiProperty({ type: VehicleDto })
    vehicle!: VehicleDto;
}

export class TripPassengerDto {
    @ApiProperty({ format: "uuid" })
    id!: string;

    @ApiPropertyOptional()
    givenName!: string | null;

    @ApiPropertyOptional()
    familyName!: string | null;

    @ApiProperty()
    phone!: string;

    @ApiPropertyOptional()
    photoUrl!: string | null;
}

export class TripDto extends QuoteDto {
    @ApiProperty({ format: "uuid" })
    id!: string;

    @ApiProperty({ enum: TripStatus, enumName: "TripStatus" })
    status!: TripStatus;

    @ApiPropertyOptional({ example: 18.5 })
    finalFare!: number | null;

    @ApiProperty({ type: TripPassengerDto })
    passenger!: TripPassengerDto | null;

    @ApiPropertyOptional({ type: TripDriverDto })
    driver!: TripDriverDto | null;

    @ApiPropertyOptional()
    ratingByPassenger!: number | null;

    @ApiPropertyOptional()
    ratingByDriver!: number | null;

    @ApiProperty()
    requestedAt!: string;

    @ApiPropertyOptional()
    acceptedAt!: string | null;

    @ApiPropertyOptional()
    arrivedAt!: string | null;

    @ApiPropertyOptional()
    startedAt!: string | null;

    @ApiPropertyOptional()
    completedAt!: string | null;

    @ApiPropertyOptional()
    cancelledAt!: string | null;

    @ApiPropertyOptional({ enum: TripCancelledBy })
    cancelledBy!: TripCancelledBy | null;
}

export class OfferDto {
    @ApiProperty({ format: "uuid" })
    id!: string;

    @ApiProperty({ format: "uuid" })
    tripId!: string;

    @ApiProperty({ example: 18.5 })
    price!: number;

    @ApiProperty({ example: "PEN" })
    currency!: string;

    @ApiPropertyOptional({ example: 240 })
    etaSeconds!: number | null;

    @ApiPropertyOptional({ example: 1300 })
    distanceToPickupMeters!: number | null;

    @ApiProperty({ enum: OfferStatus, enumName: "OfferStatus" })
    status!: OfferStatus;

    @ApiProperty()
    createdAt!: string;

    @ApiProperty({ type: TripDriverDto })
    driver!: TripDriverDto | null;
}

export class PassengerCurrentTripDto {
    @ApiPropertyOptional({ type: TripDto })
    trip!: TripDto | null;

    @ApiProperty({ type: [OfferDto] })
    offers!: OfferDto[];
}

export class DriverCurrentTripDto {
    @ApiPropertyOptional({ type: TripDto })
    trip!: TripDto | null;
}

export class TrackPointDto {
    @ApiProperty()
    lat!: number;

    @ApiProperty()
    lng!: number;

    @ApiProperty()
    at!: string;
}

export class AdminTripDetailDto extends TripDto {
    @ApiProperty({ type: [OfferDto] })
    offers!: OfferDto[];

    @ApiProperty({ type: [TrackPointDto] })
    track!: TrackPointDto[];
}
