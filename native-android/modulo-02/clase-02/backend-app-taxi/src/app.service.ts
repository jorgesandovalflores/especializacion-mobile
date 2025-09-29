import { Injectable } from '@nestjs/common';
import { Trip } from './trip.model';

@Injectable()
export class AppService {
    // Comentario: base in-memory de ejemplo con distintos estados
    private readonly trips: Trip[] = [
        {
            id: 't-1001',
            passengerName: 'María Pérez',
            pickup: { lat: -12.0464, lng: -77.0428, address: 'Plaza Mayor, Lima' },
            dropoff: { lat: -12.0901, lng: -77.0460, address: 'Av. La Marina 1234, San Miguel' },
            requestedAt: '2025-09-29T12:00:00Z',
            status: 'PENDING',
        },
        {
            id: 't-1002',
            passengerName: 'Jorge Sandoval',
            pickup: { lat: -12.0720, lng: -77.0868, address: 'Av. Universitaria 500, San Miguel' },
            dropoff: { lat: -12.1245, lng: -77.0297, address: 'Av. Primavera 800, Surco' },
            requestedAt: '2025-09-29T12:05:00Z',
            status: 'ASSIGNED',
        },
        {
            id: 't-1003',
            passengerName: 'Lucía Rojas',
            pickup: { lat: -12.0584, lng: -77.0365, address: 'Av. Abancay 300, Cercado' },
            dropoff: { lat: -12.0980, lng: -77.0350, address: 'Av. Arequipa 2100, Lince' },
            requestedAt: '2025-09-29T12:10:00Z',
            status: 'PENDING',
        }
    ];

    // Comentario: expone solo los viajes con estado PENDING
    getPendingTrips(): Trip[] {
        return this.trips.filter(t => t.status === 'PENDING');
    }
}
