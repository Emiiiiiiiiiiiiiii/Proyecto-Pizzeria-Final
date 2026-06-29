package com.pizzas.autenticacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// Configuración de Swagger/OpenAPI para autenticación
@Configuration
public class OpenApiConfig {
    // Define la información general que aparece en Swagger UI
    @Bean
    public OpenAPI autenticacionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Autenticación - Pizzería Gordas")
                        .version("1.0")
                        .description("Microservicio encargado del registro, login y gestión de usuarios."));
    }

}
