package com.pizzas.resenas.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para responder datos de reseñas
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ResenaResponseDTO {
    private Integer id;
    private Integer pedidoId;
    private Integer usuarioId;
    private String clienteNombre;
    private String nombreProducto;
    private String comentario;
    private Integer estrellas;
    private LocalDateTime fechaResena;

}
