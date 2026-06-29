package com.pizzas.resenas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ResenaResponseDTO {
    private String clienteNombre;
    private String nombreProducto;
    private String comentario;
    private Integer estrellas;

}
