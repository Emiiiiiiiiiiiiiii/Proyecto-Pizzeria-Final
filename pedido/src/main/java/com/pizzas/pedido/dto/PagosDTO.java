package com.pizzas.pedido.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para enviar datos al microservicio de pagos
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagosDTO {
    // Monto total que se debe pagar
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Integer monto;

    // ID del pedido que se está pagando
    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    private Integer pedidoId;

    // ID del usuario que realiza el pago
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Integer usuarioId;
}
