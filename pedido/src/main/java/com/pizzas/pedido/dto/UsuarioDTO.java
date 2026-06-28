package com.pizzas.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para manejar datos recibidos desde autenticación
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
    // ID del usuario
    private Integer id;

    // Nombre del usuario
    private String nombre;

    // Apellido del usuario
    private String apellido;

    // Email del usuario
    private String email;

    // Rol del usuario
    private String rol;

}
