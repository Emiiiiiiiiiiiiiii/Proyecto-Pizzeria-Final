package com.pizzas.pedido;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Configuración general del microservicio pedido
@Configuration
public class AppConfig {
    // Crea el RestTemplate para comunicarse con otros microservicios
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
