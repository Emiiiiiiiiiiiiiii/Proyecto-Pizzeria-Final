package com.servicio.reparto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepartoDTO {
    @NotNull(message = "El ID del pedido es obligatorio")
    private Integer pedidoId;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer usuarioId;

    @NotBlank(message = "La dirección de entrega no puede estar vacía")
    private String direccionEntrega;

    @NotBlank(message = "El nombre del repartidor no puede estar vacío")
    private String repartidor;

    private String horaEntrega;
}