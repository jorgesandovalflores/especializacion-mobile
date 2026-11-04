import { Logger } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import {
    ConnectedSocket,
    MessageBody,
    OnGatewayConnection,
    OnGatewayInit,
    SubscribeMessage,
    WebSocketGateway,
} from "@nestjs/websockets";
import { Namespace, Socket } from "socket.io";
import { RealtimeEmitter } from "src/core/realtime/realtime-emitter.service";
import {
    RealtimeEvents,
    RealtimeSession,
    RealtimeUserType,
    Rooms,
} from "src/core/realtime/realtime.events";
import type { DriverLocationPayload } from "src/features/trips/services/driver-location.service";
import { DriverLocationService } from "src/features/trips/services/driver-location.service";

interface SocketData {
    session?: RealtimeSession;
}

const USER_TYPES: RealtimeUserType[] = ["passenger", "driver", "admin"];

@WebSocketGateway({ namespace: "realtime", cors: { origin: "*" } })
export class RealtimeGateway implements OnGatewayInit, OnGatewayConnection {
    private readonly logger = new Logger(RealtimeGateway.name);

    constructor(
        private readonly jwt: JwtService,
        private readonly config: ConfigService,
        private readonly emitter: RealtimeEmitter,
        private readonly driverLocation: DriverLocationService,
    ) {}

    afterInit(namespace: Namespace): void {
        this.emitter.attach(namespace);
        namespace.use((socket, next) => {
            this.authenticate(socket)
                .then((session) => {
                    (socket.data as SocketData).session = session;
                    next();
                })
                .catch(() => next(new Error("unauthorized")));
        });
    }

    handleConnection(client: Socket): void {
        const session = this.session(client);
        if (!session) {
            client.disconnect(true);
            return;
        }
        void client.join(this.roomOf(session));
        client.emit(RealtimeEvents.SessionReady, session);
        this.logger.log(`${session.userType} ${session.userId} connected`);
    }

    @SubscribeMessage(RealtimeEvents.DriverLocation)
    async onDriverLocation(
        @ConnectedSocket() client: Socket,
        @MessageBody() payload: DriverLocationPayload,
    ): Promise<{ ok: boolean }> {
        const session = this.session(client);
        if (session?.userType !== "driver") return { ok: false };
        const ok = await this.driverLocation.update(session.userId, payload);
        return { ok };
    }

    private async authenticate(socket: Socket): Promise<RealtimeSession> {
        const header = socket.handshake.headers.authorization;
        const authToken = socket.handshake.auth?.token as string | undefined;
        const token =
            authToken ??
            (header?.startsWith("Bearer ")
                ? header.slice(7).trim()
                : undefined);
        if (!token) throw new Error("missing token");

        const payload = await this.jwt.verifyAsync<{
            sub?: string;
            typ?: string;
            rt?: boolean;
        }>(token, { secret: this.config.get<string>("JWT_ACCESS_SECRET") });

        const userType = payload.typ as RealtimeUserType;
        if (!payload.sub || payload.rt || !USER_TYPES.includes(userType)) {
            throw new Error("invalid token");
        }
        return { userType, userId: payload.sub };
    }

    private session(client: Socket): RealtimeSession | null {
        return (client.data as SocketData | undefined)?.session ?? null;
    }

    private roomOf(session: RealtimeSession): string {
        switch (session.userType) {
            case "passenger":
                return Rooms.passenger(session.userId);
            case "driver":
                return Rooms.driver(session.userId);
            default:
                return Rooms.admins;
        }
    }
}
