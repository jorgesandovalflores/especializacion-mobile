import { MenuDto } from "../dto/menu.dto";
import { MenuEntity } from "../entities/menu.entity";

export const toMenuDto = (entity: MenuEntity): MenuDto => {
    return {
        key: entity.key,
        text: entity.text,
        icon: entity.icon,
        deeplink: entity.deeplink,
        order: entity.order,
    };
};
