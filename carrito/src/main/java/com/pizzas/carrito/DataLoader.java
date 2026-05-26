package com.pizzas.carrito;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pizzas.carrito.model.Carrito;
import com.pizzas.carrito.repository.CarritoRepository;

@Configuration
public class DataLoader {
    @Bean
CommandLineRunner init(CarritoRepository repository) {
    return args -> {
        if (repository.count() == 0) {
            
            repository.save(new Carrito(null, 2, 1, 2, 8000, 16000));
            repository.save(new Carrito(null, 2, 2, 1, 7500, 7500));

            System.out.println("Base de datos de carrito inicializada con datos de prueba!");
        }
    };
}

}
