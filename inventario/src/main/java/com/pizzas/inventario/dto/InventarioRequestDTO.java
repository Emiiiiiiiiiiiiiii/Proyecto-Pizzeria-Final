package com.pizzas.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class InventarioRequestDTO {
    @NotNull(message = "El ID del catálogo es obligatorio")
    private Integer catalogoId;

    @NotNull(message = "La cantidad no puede estar vacía")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer cantidad;

}
