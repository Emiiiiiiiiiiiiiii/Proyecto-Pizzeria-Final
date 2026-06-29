package com.pizzas.resenas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// Configuración de Swagger/OpenAPI para reseñas
@Configuration
public class OpenApiConfig {

    // Define la información general que aparece en Swagger UI
    @Bean
    public OpenAPI resenasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Reseñas - Pizzería Gordas")
                        .version("1.0")
                        .description("Microservicio encargado de registrar, consultar, actualizar y eliminar reseñas de pedidos."));
    }

}
