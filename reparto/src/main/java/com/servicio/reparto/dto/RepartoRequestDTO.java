package com.servicio.reparto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

// DTO usado para generar un reparto
@Data
public class RepartoRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    private Integer pedidoId;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Integer usuarioId;

    @NotBlank(message = "La dirección de entrega no puede estar vacía")
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
    private String direccionEntrega;

    @NotBlank(message = "El nombre del repartidor no puede estar vacío")
    @Size(min = 2, max = 120, message = "El repartidor debe tener entre 2 y 120 caracteres")
    private String repartidor;
}
