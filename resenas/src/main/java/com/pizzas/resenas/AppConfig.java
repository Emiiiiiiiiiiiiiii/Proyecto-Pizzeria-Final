package com.pizzas.resenas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Configuración general del microservicio reseñas
@Configuration

public class AppConfig {
    // Crea RestTemplate para comunicarse con pedido, auth y notificaciones
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
