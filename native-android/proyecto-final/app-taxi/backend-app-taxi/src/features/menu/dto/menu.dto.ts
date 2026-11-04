import { ApiProperty } from "@nestjs/swagger";

/* -------------------------------------------------------
   DTO de lectura de una opción de menú
   - Solo expone lo que la app necesita dibujar y guardar
-------------------------------------------------------- */
export class MenuDto {
    @ApiProperty({
        description: "Stable key of the menu item",
        example: "passenger_profile",
        maxLength: 24,
    })
    key!: string;

    @ApiProperty({ description: "Display text", example: "Mi perfil" })
    text!: string;

    @ApiProperty({
        description: "Logical icon name resolved by the app",
        example: "profile",
    })
    icon!: string;

    @ApiProperty({
        description: "Deep link opened by the menu item",
        example: "app-taxi://passenger/profile",
    })
    deeplink!: string;

    @ApiProperty({ description: "Ascending display order", example: 2 })
    order!: number;
}
