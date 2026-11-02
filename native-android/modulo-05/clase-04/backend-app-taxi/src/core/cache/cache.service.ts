import { Injectable, OnModuleInit, OnModuleDestroy } from "@nestjs/common";
import { createClient, RedisClientType } from "redis";

@Injectable()
export class CacheService implements OnModuleInit, OnModuleDestroy {
    private client: RedisClientType;

    async onModuleInit() {
        this.client = createClient({
            url: `redis://${process.env.REDIS_HOST || "localhost"}:${process.env.REDIS_PORT || 6379}`,
            disableOfflineQueue: true,
            socket: { connectTimeout: 5_000 },
        });

        this.client.on("error", (err) =>
            console.error("Redis Client Error", err),
        );
        this.client.on("connect", () => console.log("Redis connecting..."));
        this.client.on("ready", () => console.log("Redis ready"));

        await this.client.connect();
    }

    async onModuleDestroy() {
        await this.client.quit();
    }

    // Métodos básicos
    async set(key: string, value: any, ttlSeconds?: number): Promise<void> {
        const serialized = JSON.stringify(value);
        if (ttlSeconds) {
            await this.client.setEx(key, ttlSeconds, serialized);
        } else {
            await this.client.set(key, serialized);
        }
    }

    async get<T>(key: string): Promise<T | null> {
        const value = await this.client.get(key);
        return value ? JSON.parse(value) : null;
    }

    async del(key: string): Promise<void> {
        await this.client.del(key);
    }

    // Métodos avanzados
    async keys(pattern: string): Promise<string[]> {
        return this.client.keys(pattern);
    }

    async flushAll(): Promise<void> {
        await this.client.flushAll();
    }

    async hSet(key: string, field: string, value: unknown): Promise<void> {
        await this.client.hSet(key, field, JSON.stringify(value));
    }

    async hGet<T>(key: string, field: string): Promise<T | null> {
        const value = await this.client.hGet(key, field);
        return value ? (JSON.parse(value) as T) : null;
    }

    async hGetAll<T>(key: string): Promise<Record<string, T>> {
        const entries = await this.client.hGetAll(key);
        return Object.fromEntries(
            Object.entries(entries).map(([field, value]) => [
                field,
                JSON.parse(value) as T,
            ]),
        );
    }

    async hDel(key: string, field: string): Promise<void> {
        await this.client.hDel(key, field);
    }

    async sAdd(key: string, ...members: string[]): Promise<void> {
        if (members.length) await this.client.sAdd(key, members);
    }

    async sRem(key: string, member: string): Promise<void> {
        await this.client.sRem(key, member);
    }

    async sMembers(key: string): Promise<string[]> {
        return this.client.sMembers(key);
    }

    async sIsMember(key: string, member: string): Promise<boolean> {
        return Boolean(await this.client.sIsMember(key, member));
    }

    async rPush(key: string, value: unknown): Promise<void> {
        await this.client.rPush(key, JSON.stringify(value));
    }

    async lRange<T>(key: string, start = 0, stop = -1): Promise<T[]> {
        const values = await this.client.lRange(key, start, stop);
        return values.map((value) => JSON.parse(value) as T);
    }

    async expire(key: string, ttlSeconds: number): Promise<void> {
        await this.client.expire(key, ttlSeconds);
    }
}
