package com.servicio.notificaciones;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Configuración general del microservicio notificaciones
@Configuration

public class AppConfig {
    // Crea RestTemplate por si notificaciones necesita comunicarse con otros MS
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
