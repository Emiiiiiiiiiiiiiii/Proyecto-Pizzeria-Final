package com.pizzas.certificacion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// Datos necesarios para generar una certificación
@Data
public class CertificacionRequestDTO {
    
    // ID del pedido que se quiere certificar
    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    private Integer pedidoId;

}
