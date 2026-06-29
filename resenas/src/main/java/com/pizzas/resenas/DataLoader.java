package com.pizzas.resenas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// DataLoader del microservicio
@Configuration
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner init() {
        return args -> {
            logger.info("Microservicio iniciado correctamente. No se cargan datos de prueba para evitar registros falsos.");
        };
    }

}
