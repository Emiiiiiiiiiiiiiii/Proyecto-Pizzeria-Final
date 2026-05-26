package com.pizzas.catalogo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pizzas.catalogo.model.Catalogo;
import com.pizzas.catalogo.repository.CatalogoRepository;

@Configuration

public class DataLoader {
    @Bean
    CommandLineRunner init(CatalogoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                
                repository.save(new Catalogo(null, "Pepperoni",
                "Clásica", "Familiar", 8000));
                repository.save(new Catalogo(null, "Margarita",
                "Vegetariana", "Familiar", 7500));
                repository.save(new Catalogo(null, "Hawaiana",
                "Especial", "Familiar", 8500));
                repository.save(new Catalogo(null, "Cuatro Quesos",
                "Premium", "Familiar", 9000));
                repository.save(new Catalogo(null, "Vegetal",
                "Saludable", "Familiar", 7000));

                System.out.println("Base de datos de catálogo inicializada!.");
            }
        };
    }

}
