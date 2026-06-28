package com.pizzas.carrito;

// Permite ejecutar código inicial al arrancar el microservicio
import org.springframework.boot.CommandLineRunner;

// Permite crear beans de configuración
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Permite usar logs en vez de System.out.println
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Importa el modelo Carrito
import com.pizzas.carrito.model.Carrito;

// Importa el repository para guardar datos de prueba
import com.pizzas.carrito.repository.CarritoRepository;

@Configuration
public class DataLoader {
    // Logger para mostrar mensajes ordenados en consola
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    // Ejecuta datos iniciales al iniciar el microservicio
    @Bean
    CommandLineRunner init(CarritoRepository repository) {
        return args -> {

            // Agrega pizza 1 al carrito del usuario 2 solo si no existe
            if (!repository.existsByUsuarioIdAndCatalogoId(2, 1)) {
                repository.save(new Carrito(null, 2, 1, 2, 8000, 16000));
                logger.info("Producto 1 agregado al carrito de prueba del usuario 2");
            }

            // Agrega pizza 2 al carrito del usuario 2 solo si no existe
            if (!repository.existsByUsuarioIdAndCatalogoId(2, 2)) {
                repository.save(new Carrito(null, 2, 2, 1, 7500, 7500));
                logger.info("Producto 2 agregado al carrito de prueba del usuario 2");
            }

            logger.info("Base de datos de carrito inicializada correctamente con datos de prueba");
        };
    }

}
