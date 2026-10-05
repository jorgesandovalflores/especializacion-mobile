import { toMenuDto } from "./menu.mapper";
import { MenuEntity } from "../entities/menu.entity";
import { MenuApplication } from "../enum/menu-application.enum";
import { MenuStatus } from "../enum/menu-status.enum";

describe("toMenuDto", () => {
    it("maps only the fields the app needs", () => {
        const entity = new MenuEntity();
        entity.id = "0b0f5b0e-6a55-4f1c-9a49-0d6f6c1f9f10";
        entity.key = "passenger_profile";
        entity.text = "Mi perfil";
        entity.icon = "profile";
        entity.deeplink = "app-taxi://passenger/profile";
        entity.order = 2;
        entity.application = MenuApplication.PASSENGER;
        entity.status = MenuStatus.ACTIVE;
        entity.createdAt = new Date();
        entity.updatedAt = new Date();
        entity.deletedAt = null;

        expect(toMenuDto(entity)).toEqual({
            key: "passenger_profile",
            text: "Mi perfil",
            icon: "profile",
            deeplink: "app-taxi://passenger/profile",
            order: 2,
        });
    });
});
