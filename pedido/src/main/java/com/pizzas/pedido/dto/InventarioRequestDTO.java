package com.pizzas.pedido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para solicitar descuento de stock al microservicio inventario
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventarioRequestDTO {
    // ID del producto que se descontará del inventario
    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Integer catalogoId;

    // Cantidad que se descontará del inventario
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    private Integer cantidad;

}
