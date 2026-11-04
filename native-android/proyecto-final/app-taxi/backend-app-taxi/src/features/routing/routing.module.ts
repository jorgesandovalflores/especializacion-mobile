import { Module } from "@nestjs/common";
import { ConfigModule } from "@nestjs/config";
import { RoutePlanner } from "./route-planner";
import { OsrmRoutePlanner } from "./osrm-route-planner";

@Module({
    imports: [ConfigModule],
    providers: [{ provide: RoutePlanner, useClass: OsrmRoutePlanner }],
    exports: [RoutePlanner],
})
export class RoutingModule {}
