package com.servicio.notificaciones.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

// DTO recibido para crear una notificación
@Data
public class NotiDTO {


    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Integer usuarioId;

    // Es opcional porque registro de usuario no tiene pedido
    @Positive(message = "El ID del pedido debe ser positivo")
    private Integer pedidoId;

    @NotBlank(message = "El tipo de notificación es obligatorio")
    @Size(max = 50, message = "El tipo no puede superar los 50 caracteres")
    private String tipo;

    @NotBlank(message = "El destinatario es obligatorio")
    @Email(message = "El destinatario debe ser un email válido")
    @Size(max = 120, message = "El destinatario no puede superar los 120 caracteres")
    private String destinatario;

    // Es opcional porque algunos MS solo envían tipo y fecha
    @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres")
    private String mensaje;

    // Es opcional porque el service puede generar la fecha automáticamente
    private String fecha;
}
