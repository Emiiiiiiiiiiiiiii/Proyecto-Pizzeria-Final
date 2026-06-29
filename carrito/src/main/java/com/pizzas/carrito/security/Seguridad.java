package com.pizzas.carrito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// Configuración para desactivar la seguridad automática de Spring Security
@Configuration

public class Seguridad {
    // Permite todos los endpoints porque este MS no usa login propio
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactiva CSRF porque el microservicio funciona como API REST
            .csrf(csrf -> csrf.disable())

            // Evita crear sesiones en el servidor
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Desactiva el formulario de login automático de Spring
            .formLogin(form -> form.disable())

            // Desactiva la autenticación básica del navegador
            .httpBasic(basic -> basic.disable())

            // Permite todas las rutas del microservicio carrito
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
