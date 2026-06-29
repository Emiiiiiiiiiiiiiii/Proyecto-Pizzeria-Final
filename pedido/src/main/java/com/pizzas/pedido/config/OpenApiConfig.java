package com.pizzas.pedido.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// Configuración de Swagger/OpenAPI para pedidos
@Configuration
public class OpenApiConfig {

    // Define la información general que aparece en Swagger UI
    @Bean
    public OpenAPI pedidoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Pedido - Pizzería Gordas")
                        .version("1.0")
                        .description("Microservicio encargado de crear pedidos y coordinar carrito, inventario, pagos y notificaciones."));
    }

}
