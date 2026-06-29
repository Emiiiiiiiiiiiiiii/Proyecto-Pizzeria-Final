package com.pizzas.inventario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pizzas.inventario.model.Inventario;
import com.pizzas.inventario.repository.InventarioRepository;

// Carga datos iniciales del inventario
@Configuration

public class DataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner init(InventarioRepository repository) {
        return args -> {
            crearInventarioSiNoExiste(repository, 1, 300);
            crearInventarioSiNoExiste(repository, 2, 300);
            crearInventarioSiNoExiste(repository, 3, 200);
            crearInventarioSiNoExiste(repository, 4, 150);
            crearInventarioSiNoExiste(repository, 5, 250);

            logger.info("Carga inicial de inventario finalizada correctamente");
        };
    }

    // Crea inventario solo si no existe para ese producto del catálogo
    private void crearInventarioSiNoExiste(
            InventarioRepository repository,
            Integer catalogoId,
            Integer cantidad) {

        if (!repository.existsByCatalogoId(catalogoId)) {
            Inventario inventario = new Inventario();
            inventario.setCatalogoId(catalogoId);
            inventario.setCantidad(cantidad);

            repository.save(inventario);

            logger.info("Inventario inicial creado para catalogoId {} con stock {}", catalogoId, cantidad);
        }
    }

}
