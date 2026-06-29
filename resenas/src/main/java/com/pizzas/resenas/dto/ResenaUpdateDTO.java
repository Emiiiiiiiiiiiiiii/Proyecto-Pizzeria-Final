package com.pizzas.resenas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// DTO usado para actualizar una reseña
@Data
public class ResenaUpdateDTO {
    @NotBlank(message = "El comentario es obligatorio")
    @Size(min = 5, max = 1000, message = "El comentario debe tener entre 5 y 1000 caracteres")
    private String comentario;

    @NotNull(message = "Las estrellas son obligatorias")
    @Min(value = 1, message = "La reseña debe tener mínimo 1 estrella")
    @Max(value = 5, message = "La reseña debe tener máximo 5 estrellas")
    private Integer estrellas;

}
