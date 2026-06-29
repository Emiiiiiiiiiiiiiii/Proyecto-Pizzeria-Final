package com.pizzas.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

// DTO usado para recibir datos al crear o actualizar pizzas
@Data

public class CatalogoRequestDTO {
     @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(
        regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$",
        message = "El nombre solo puede contener letras y espacios"
    )
    private String nombre;

    @NotBlank(message = "El tipo no puede estar vacío")
    @Size(min = 2, max = 50, message = "El tipo debe tener entre 2 y 50 caracteres")
    @Pattern(
        regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$",
        message = "El tipo solo puede contener letras y espacios"
    )
    private String tipo;

    @NotBlank(message = "El tamaño no puede estar vacío")
    @Size(min = 2, max = 50, message = "El tamaño debe tener entre 2 y 50 caracteres")
    @Pattern(
        regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$",
        message = "El tamaño solo puede contener letras y espacios"
    )
    private String tamanio;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private Integer precio;

}
