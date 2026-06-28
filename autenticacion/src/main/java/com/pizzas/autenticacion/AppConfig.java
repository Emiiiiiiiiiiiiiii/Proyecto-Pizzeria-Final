package com.pizzas.autenticacion;

// Permite crear objetos que Spring puede inyectar en otras clases
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Cliente usado para comunicarse con otros microservicios
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    // Crea un RestTemplate para consumir endpoints de otros microservicios
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
