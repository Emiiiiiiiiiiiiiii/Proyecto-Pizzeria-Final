package com.pizzas.autenticacion.dto;

// Lombok genera getters, setters y constructores
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Genera getters, setters, toString, equals y hashCode automáticamente
@Data

// Constructor vacío
@NoArgsConstructor

// Constructor con todos los atributos
@AllArgsConstructor

public class PerfilUsuarioPublicoDTO {
    // ID público del usuario
    private Integer id;

    // Nombre del usuario
    private String nombre;

    // Apellido del usuario
    private String apellido;

    // Correo electrónico del usuario
    private String email;

    // Rol del usuario, por ejemplo CLIENTE o ADMIN
    private String rol;

}
