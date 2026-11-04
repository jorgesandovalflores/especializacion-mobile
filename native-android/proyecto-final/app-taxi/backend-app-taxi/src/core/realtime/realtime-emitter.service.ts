import { Injectable } from "@nestjs/common";
import { Namespace } from "socket.io";

@Injectable()
export class RealtimeEmitter {
    private namespace?: Namespace;

    attach(namespace: Namespace): void {
        this.namespace = namespace;
    }

    emit(rooms: string | string[], event: string, payload: unknown): void {
        const targets = Array.isArray(rooms) ? rooms : [rooms];
        if (!this.namespace || targets.length === 0) return;
        this.namespace.to(targets).emit(event, payload);
    }
}
