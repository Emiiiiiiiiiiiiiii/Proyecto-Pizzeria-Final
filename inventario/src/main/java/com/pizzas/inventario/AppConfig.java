package com.pizzas.inventario;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Configuración general del microservicio inventario
@Configuration

public class AppConfig {
    // Crea RestTemplate para comunicarse con catálogo
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
