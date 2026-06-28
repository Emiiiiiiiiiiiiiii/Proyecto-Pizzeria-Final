package com.pizzas.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// DTO usado para crear un pedido
@Data
public class PedidoRequestDTO {
    // ID del usuario que realiza el pedido
    @NotNull(message = "El usuarioId es obligatorio")
    @Positive(message = "El usuarioId debe ser positivo")
    private Integer usuarioId;

    // Método de pago elegido por el usuario
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(
        regexp = "^(EFECTIVO|DEBITO|CREDITO|TRANSFERENCIA)$",
        message = "El método de pago debe ser EFECTIVO, DEBITO, CREDITO o TRANSFERENCIA"
    )
    private String metodoPago;

}
