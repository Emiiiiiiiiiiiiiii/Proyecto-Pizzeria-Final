package com.pizzas.carrito.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class CarritoDTO {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer usuarioId;

    @NotNull(message = "El ID del producto es obligatorio")
    private Integer catalogoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    private Integer cantidad;

    private Integer precioUnitario;
    private Integer precioTotal;

}
