import { NestFactory } from "@nestjs/core";
import { AppModule } from "./app.module";
import { Logger } from "nestjs-pino";
import { ValidationPipe } from "@nestjs/common";
import { DocumentBuilder, SwaggerModule } from "@nestjs/swagger";
import { I18nService, I18nValidationExceptionFilter } from "nestjs-i18n";
import { ExpressAdapter } from "@nestjs/platform-express";
import { HttpExceptionFilter } from "./core/http/filters/http-exception.filter";

async function bootstrap() {
    const app = await NestFactory.create(AppModule, {
        bufferLogs: true,
    });
    app.useLogger(app.get(Logger));

    app.useGlobalPipes(
        new ValidationPipe({
            whitelist: true,
            transform: true,
            forbidNonWhitelisted: true,
            transformOptions: {
                enableImplicitConversion: true,
            },
        }),
    );

    // Filtro de validación con manejo seguro de constraints
    app.useGlobalFilters(
        new I18nValidationExceptionFilter(),
        new HttpExceptionFilter(app.get(I18nService)),
    );

    // Configuración de Swagger...
    const config = new DocumentBuilder()
        .setTitle("AppTaxi - api service")
        .setDescription("Documentación de la API")
        .setVersion("0.0.1")
        .addBearerAuth()
        .build();

    const document = SwaggerModule.createDocument(app, config);
    SwaggerModule.setup("api/docs", app, document);

    const httpAdapter = app.getHttpAdapter();

    if (httpAdapter instanceof ExpressAdapter) {
        httpAdapter.getInstance().disable("x-powered-by");
    }
    await app.listen(Number(process.env.APPLICATION_PORT));
}
bootstrap();
