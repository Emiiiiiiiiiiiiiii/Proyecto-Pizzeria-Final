package com.pizzas.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class PedidoRequestDTO {
    @NotNull(message = "El usuarioId es obligatorio")
    private Integer usuarioId;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

}
