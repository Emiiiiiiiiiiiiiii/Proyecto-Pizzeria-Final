package com.pizzas.inventario;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pizzas.inventario.model.Inventario;
import com.pizzas.inventario.repository.InventarioRepository;

@Configuration

public class DataLoader {
    @Bean
    CommandLineRunner init(InventarioRepository repository){
        return args -> {
            if(repository.count() == 0){
                repository.save(new Inventario(null, 1, 300));
                repository.save(new Inventario(null, 2, 300));
                repository.save(new Inventario(null, 3, 200));
                repository.save(new Inventario(null, 4, 150));
                repository.save(new Inventario(null, 5, 250));

                System.out.println("Base de datos de inventario inicializada correctamente con IDs de catálogo!");
            }
        };
    }

}
