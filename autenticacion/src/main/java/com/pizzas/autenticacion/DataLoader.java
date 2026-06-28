package com.pizzas.autenticacion;

// Permite ejecutar código automáticamente cuando inicia el microservicio
import org.springframework.boot.CommandLineRunner;

// Permite encriptar las contraseñas antes de guardarlas
import org.springframework.security.crypto.password.PasswordEncoder;

// Marca esta clase como componente de Spring para que se ejecute automáticamente
import org.springframework.stereotype.Component;

// Permite usar logs en vez de System.out.println
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Importa el modelo Usuario
import com.pizzas.autenticacion.model.Usuario;

// Importa el repositorio para guardar y consultar usuarios
import com.pizzas.autenticacion.repository.UsuarioRepository;

@Component
public class DataLoader implements CommandLineRunner {
    // Logger para mostrar mensajes ordenados en consola
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    // Repositorio para acceder a la tabla usuarios
    private final UsuarioRepository usuarioRepository;

    // Encoder para guardar contraseñas encriptadas
    private final PasswordEncoder passwordEncoder;

    // Constructor para inyectar dependencias
    public DataLoader(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Este método se ejecuta automáticamente al iniciar el microservicio
    @Override
    public void run(String... args) throws Exception {

        // Crea el usuario administrador solo si no existe ese correo
        if (!usuarioRepository.existsByEmail("admin@gordaspizzas.com")) {
            Usuario admin = new Usuario();
            admin.setNombre("Matias");
            admin.setApellido("Lisandres");
            admin.setEmail("admin@gordaspizzas.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol("ADMIN");

            usuarioRepository.save(admin);

            logger.info("Usuario ADMIN de prueba creado correctamente");
        }

        // Crea el usuario cliente solo si no existe ese correo
        if (!usuarioRepository.existsByEmail("juan@gmail.com")) {
            Usuario cliente = new Usuario();
            cliente.setNombre("Juan");
            cliente.setApellido("Pérez");
            cliente.setEmail("juan@gmail.com");
            cliente.setPassword(passwordEncoder.encode("juan123"));
            cliente.setRol("CLIENTE");

            usuarioRepository.save(cliente);

            logger.info("Usuario CLIENTE de prueba creado correctamente");
        }

        logger.info("Carga inicial de usuarios finalizada correctamente");
    }


}
