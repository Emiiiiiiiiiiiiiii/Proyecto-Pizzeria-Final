package com.pizzas.carrito.dto;

// Lombok genera getters, setters y constructores
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para recibir datos del usuario desde autenticación
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    // ID del usuario
    private Integer id;

    // Nombre del usuario
    private String nombre;

    // Apellido del usuario
    private String apellido;

    // Correo del usuario
    private String email;

    // Rol del usuario
    private String rol;


}
