package com.servicio.reparto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// Configuración de Swagger/OpenAPI para reparto
@Configuration
public class OpenApiConfig {

    // Define la información general que aparece en Swagger UI
    @Bean
    public OpenAPI repartoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Reparto - Pizzería Gordas")
                        .version("1.0")
                        .description("Microservicio encargado de generar repartos, consultar entregas y actualizar estados de despacho."));
    }

}
