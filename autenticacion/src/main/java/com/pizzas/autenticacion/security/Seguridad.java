package com.pizzas.autenticacion.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Configuración de seguridad del microservicio
@Configuration
public class Seguridad {
     // Bean usado para encriptar y validar contraseñas con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura los permisos de los endpoints
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desactiva CSRF porque usamos API REST
            .csrf(csrf -> csrf.disable())

            // Evita sesiones porque el microservicio trabaja como API REST
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Desactiva el login por formulario de Spring Security
            .formLogin(form -> form.disable())

            // Desactiva autenticación básica del navegador
            .httpBasic(basic -> basic.disable())

            // Permite todas las rutas porque no usaremos JWT en esta entrega
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
