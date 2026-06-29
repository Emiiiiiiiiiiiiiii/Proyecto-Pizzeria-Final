package com.servicio.reparto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

// DTO usado para actualizar el estado de un reparto
@Data
public class EstadoRepartoDTO {
    
    @NotBlank(message = "El estado del reparto es obligatorio")
    @Pattern(
        regexp = "^(PREPARANDO|EN_CAMINO|ENTREGADO|CANCELADO|RETRASADO)$",
        message = "El estado debe ser PREPARANDO, EN_CAMINO, ENTREGADO, CANCELADO o RETRASADO"
    )
    private String estadoReparto;
}