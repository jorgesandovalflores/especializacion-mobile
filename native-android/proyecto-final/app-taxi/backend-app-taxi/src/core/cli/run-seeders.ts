import { seedMenus } from "../database/seeders/menu-seed";
import { seedPassengers } from "../database/seeders/passenger-seed";
import { seedBackoffice } from "../database/seeders/backoffice-seed";

async function main() {
    try {
        console.log("[seed] running pending seeds");
        await seedPassengers();
        await seedMenus();
        await seedBackoffice();
        console.log("[seed] done. Executed");
    } catch (err) {
        console.error("[seed] failed:", err);
        process.exit(1);
    }
}

main();
