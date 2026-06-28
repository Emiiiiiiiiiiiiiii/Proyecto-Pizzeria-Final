package com.pizzas.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

// DTO usado para actualizar el estado de un pedido
@Data
public class EstadoPedidoDTO {
    // Nuevo estado del pedido
    @NotBlank(message = "El estado del pedido es obligatorio")
    @Pattern(
        regexp = "^(PENDIENTE|PAGADO|EN_PREPARACION|EN_REPARTO|ENTREGADO|CANCELADO|RECHAZADO)$",
        message = "El estado debe ser PENDIENTE, PAGADO, EN_PREPARACION, EN_REPARTO, ENTREGADO, CANCELADO o RECHAZADO"
    )
    private String estado;

}
