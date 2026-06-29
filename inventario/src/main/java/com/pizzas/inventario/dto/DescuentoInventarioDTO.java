package com.pizzas.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// DTO usado para descontar stock desde pedido
@Data
public class DescuentoInventarioDTO {
    @NotNull(message = "El ID del catálogo es obligatorio")
    @Positive(message = "El ID del catálogo debe ser positivo")
    private Integer catalogoId;

    @NotNull(message = "La cantidad a descontar es obligatoria")
    @Min(value = 1, message = "La cantidad a descontar debe ser al menos 1")
    private Integer cantidad;

}
