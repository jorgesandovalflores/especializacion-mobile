import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";
import { JwtModule } from "@nestjs/jwt";
import { ConfigModule } from "@nestjs/config";
import {
    AdminAccessGuard,
    DriverAccessGuard,
    PassengerAccessGuard,
} from "src/core/http/guard/access-token.guard";
import { DriverModule } from "src/features/drivers/driver.module";
import { TariffModule } from "src/features/tariff/tariff.module";
import { RoutingModule } from "src/features/routing/routing.module";
import { TripEntity } from "./entities/trip.entity";
import { TripOfferEntity } from "./entities/trip-offer.entity";
import { TripDao } from "./dao/trip.dao";
import { TripOfferDao } from "./dao/trip-offer.dao";
import { TripNotifier } from "./services/trip-notifier.service";
import { TripTrackService } from "./services/trip-track.service";
import { PassengerTripService } from "./services/passenger-trip.service";
import { DriverTripService } from "./services/driver-trip.service";
import { DriverLocationService } from "./services/driver-location.service";
import { AdminTripService } from "./services/admin-trip.service";
import { PassengerTripController } from "./controllers/passenger-trip.controller";
import { DriverTripController } from "./controllers/driver-trip.controller";
import { AdminTripController } from "./controllers/admin-trip.controller";

@Module({
    imports: [
        TypeOrmModule.forFeature([TripEntity, TripOfferEntity]),
        ConfigModule,
        JwtModule.register({}),
        DriverModule,
        TariffModule,
        RoutingModule,
    ],
    controllers: [
        PassengerTripController,
        DriverTripController,
        AdminTripController,
    ],
    providers: [
        TripDao,
        TripOfferDao,
        TripNotifier,
        TripTrackService,
        PassengerTripService,
        DriverTripService,
        DriverLocationService,
        AdminTripService,
        PassengerAccessGuard,
        DriverAccessGuard,
        AdminAccessGuard,
    ],
    exports: [DriverLocationService],
})
export class TripModule {}
