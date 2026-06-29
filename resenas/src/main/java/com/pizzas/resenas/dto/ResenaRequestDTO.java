package com.pizzas.resenas.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data

public class ResenaRequestDTO {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer usuarioId;

    @NotNull(message = "El ID del pedido es obligatorio")
    private Integer pedidoId;

    @NotNull(message = "El ID del catálogo es obligatorio")
    private Integer catalogoId;

    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(max = 500, message = "El comentario no puede exceder los 500 caracteres")
    private String comentario;

    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    @NotNull(message = "La calificación es obligatoria")
    private Integer estrellas;


}
