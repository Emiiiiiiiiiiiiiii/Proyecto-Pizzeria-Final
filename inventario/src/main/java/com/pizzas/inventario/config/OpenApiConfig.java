package com.pizzas.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// Configuración de Swagger/OpenAPI para inventario
@Configuration
public class OpenApiConfig {
    // Define la información general que aparece en Swagger UI
    @Bean
    public OpenAPI inventarioOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Inventario - Pizzería Gordas")
                        .version("1.0")
                        .description("Microservicio encargado de gestionar stock de productos del catálogo y descontar inventario al generar pedidos."));
    }

}
