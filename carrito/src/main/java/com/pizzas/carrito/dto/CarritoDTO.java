package com.pizzas.carrito.dto;

// Validaciones para controlar los datos que llegan al carrito
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Lombok genera getters, setters y constructores
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para agregar o actualizar productos en el carrito
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CarritoDTO {
    // ID del usuario dueño del carrito
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Integer usuarioId;

    // ID de la pizza/producto que viene desde catálogo
    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Integer catalogoId;

    // Cantidad de unidades que se desean agregar
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    @Max(value = 50, message = "No puedes agregar más de 50 unidades del mismo producto")
    private Integer cantidad;

}
