package com.pizzas.resenas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO usado para enviar notificaciones desde reseñas
@Data
public class NotiDTO {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer usuarioId;

    @NotNull(message = "El ID del pedido es obligatorio")
    private Integer pedidoId;

    @NotBlank(message = "El tipo de notificación es obligatorio")
    private String tipo;

    @NotBlank(message = "El destinatario es obligatorio")
    @Email(message = "El destinatario debe ser un email válido")
    private String destinatario;

    private String mensaje;
    private String fecha;

}
