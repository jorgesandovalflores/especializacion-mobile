import { seedPassengers } from "../database/seeders/passenger-seed";

async function main() {
    try {
        console.log("[seed] running pending seeds");
        await seedPassengers();
        console.log("[seed] done. Executed");
    } catch (err) {
        console.error("[seed] failed:", err);
        process.exit(1);
    }
}

main();
