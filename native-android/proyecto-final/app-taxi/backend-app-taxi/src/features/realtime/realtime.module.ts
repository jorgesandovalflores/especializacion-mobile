import { Module } from "@nestjs/common";
import { ConfigModule } from "@nestjs/config";
import { JwtModule } from "@nestjs/jwt";
import { TripModule } from "src/features/trips/trip.module";
import { RealtimeGateway } from "./realtime.gateway";

@Module({
    imports: [ConfigModule, JwtModule.register({}), TripModule],
    providers: [RealtimeGateway],
})
export class RealtimeModule {}
