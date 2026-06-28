package com.pizzas.certificacion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Configuración general del microservicio certificación
@Configuration
public class AppConfig {
    // Crea el RestTemplate para comunicarse con otros microservicios
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
