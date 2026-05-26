package com.pizzas.certificacion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class CertificacionRequestDTO {
    @NotNull(message = "El ID del pedido es obligatorio")
    private Integer pedidoId;

}
