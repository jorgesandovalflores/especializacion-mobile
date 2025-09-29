import { Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';
import { Trip } from './trip.model';

@Controller() // Comentario: raíz "/"
export class AppController {
    constructor(private readonly appService: AppService) {}

    @Get() // Comentario: GET "/" devuelve viajes pendientes
    getPendingTrips(): Trip[] {
        return this.appService.getPendingTrips();
    }
}
