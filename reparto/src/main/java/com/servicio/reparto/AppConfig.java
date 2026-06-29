package com.servicio.reparto;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Configuración general del microservicio reparto
@Configuration
public class AppConfig {
    // Crea RestTemplate para comunicarse con otros microservicios
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
