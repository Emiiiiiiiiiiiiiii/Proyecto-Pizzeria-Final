package com.pizzas.autenticacion;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.pizzas.autenticacion.model.Usuario;
import com.pizzas.autenticacion.repository.UsuarioRepository;

@Component
public class DataLoader implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            
            Usuario admin = new Usuario();
            admin.setNombre("Matias");
            admin.setApellido("Lisandres");
            admin.setEmail("admin@gordaspizzas.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            usuarioRepository.save(admin);

            Usuario cliente = new Usuario();
            cliente.setNombre("Juan");
            cliente.setApellido("Pérez");
            cliente.setEmail("juan@gmail.com");
            cliente.setPassword(passwordEncoder.encode("juan123"));
            cliente.setRol("CLIENTE");
            usuarioRepository.save(cliente);

            System.out.println("¡Usuarios de prueba cargados con éxito en Laragon!");
        }
    }


}
