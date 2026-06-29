package com.pizzas.catalogo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// Configuración de Swagger/OpenAPI para catálogo
@Configuration
public class OpenApiConfig {
    // Define la información general que aparece en Swagger UI
    @Bean
    public OpenAPI catalogoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Catálogo - Pizzería Gordas")
                        .version("1.0")
                        .description("Microservicio encargado de gestionar el catálogo de pizzas."));
    }

}
