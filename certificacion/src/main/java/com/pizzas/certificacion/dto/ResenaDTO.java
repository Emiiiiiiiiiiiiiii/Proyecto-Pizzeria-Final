package com.pizzas.certificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para recibir datos desde el microservicio reseñas
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResenaDTO {

     private Integer id;

    private Integer pedidoId;
    private Integer usuarioId;

    private String clienteNombre;
    private String nombreProducto;

    private String comentario;
    private Integer estrellas;

}
