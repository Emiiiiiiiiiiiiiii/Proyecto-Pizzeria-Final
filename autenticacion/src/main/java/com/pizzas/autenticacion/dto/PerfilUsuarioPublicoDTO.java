package com.pizzas.autenticacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PerfilUsuarioPublicoDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;

}
