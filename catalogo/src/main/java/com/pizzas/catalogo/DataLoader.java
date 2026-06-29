package com.pizzas.catalogo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pizzas.catalogo.model.Catalogo;
import com.pizzas.catalogo.repository.CatalogoRepository;

// Carga datos iniciales del catálogo
@Configuration
public class DataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner init(CatalogoRepository repository) {
        return args -> {
            crearPizzaSiNoExiste(repository, "Pepperoni", "Clasica", "Familiar", 8000);
            crearPizzaSiNoExiste(repository, "Margarita", "Vegetariana", "Familiar", 7500);
            crearPizzaSiNoExiste(repository, "Hawaiana", "Especial", "Familiar", 8500);
            crearPizzaSiNoExiste(repository, "Cuatro Quesos", "Premium", "Familiar", 9000);
            crearPizzaSiNoExiste(repository, "Vegetal", "Saludable", "Familiar", 7000);

            logger.info("Carga inicial de catálogo finalizada correctamente");
        };
    }

    // Crea una pizza solo si no existe otra con el mismo nombre y tamaño
    private void crearPizzaSiNoExiste(
            CatalogoRepository repository,
            String nombre,
            String tipo,
            String tamanio,
            Integer precio) {

        if (!repository.existsByNombreIgnoreCaseAndTamanioIgnoreCase(nombre, tamanio)) {
            Catalogo pizza = new Catalogo();
            pizza.setNombre(nombre);
            pizza.setTipo(tipo);
            pizza.setTamanio(tamanio);
            pizza.setPrecio(precio);

            repository.save(pizza);

            logger.info("Pizza cargada en catálogo: {} {}", nombre, tamanio);
        }
    }

}
