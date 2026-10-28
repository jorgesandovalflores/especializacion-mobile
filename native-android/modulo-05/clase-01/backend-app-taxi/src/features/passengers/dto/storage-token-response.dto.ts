import { ApiProperty } from "@nestjs/swagger";

/* -------------------------------------------------------
   Respuesta de POST /passenger/storage-token
   - token: custom token de Firebase Auth (uid = pasajero)
   - bucket y photoPath: dónde debe subir la app la foto
-------------------------------------------------------- */
export class StorageTokenResponseDto {
    @ApiProperty({ example: "eyJhbGciOiJSUzI1NiIs..." })
    token!: string;

    @ApiProperty({ example: "app-taxi-xxxxx.firebasestorage.app" })
    bucket!: string;

    @ApiProperty({
        example: "passengers/93facaba-726d-4dc7-9b42-e50bc1499e1b/avatar.jpg",
    })
    photoPath!: string;
}
