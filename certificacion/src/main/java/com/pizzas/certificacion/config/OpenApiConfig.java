package com.pizzas.certificacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// Configuración de Swagger/OpenAPI para certificación
@Configuration
public class OpenApiConfig {

    // Define la información general que aparece en Swagger UI
    @Bean
    public OpenAPI certificacionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Certificación - Pizzería Gordas")
                        .version("1.0")
                        .description("Microservicio encargado de generar certificaciones de pedidos usando datos de pedido, reparto y reseñas."));
    }

}
