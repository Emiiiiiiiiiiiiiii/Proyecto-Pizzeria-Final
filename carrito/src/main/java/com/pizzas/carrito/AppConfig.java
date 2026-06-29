package com.pizzas.carrito;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    // Crea el RestTemplate para consumir otros microservicios
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
