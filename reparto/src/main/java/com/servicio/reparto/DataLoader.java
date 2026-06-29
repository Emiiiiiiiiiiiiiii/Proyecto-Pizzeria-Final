package com.servicio.reparto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// DataLoader del microservicio reparto
@Configuration
public class DataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner init() {
        return args -> {
            logger.info("Microservicio reparto iniciado. No se cargan repartos de prueba para evitar pedidos falsos.");
        };
    }

}
