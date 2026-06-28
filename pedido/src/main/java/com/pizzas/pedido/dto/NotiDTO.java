package com.pizzas.pedido.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para enviar datos al microservicio notificaciones
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotiDTO {
    // ID del pedido relacionado con la notificación
    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    private Integer pedidoId;

    // Tipo de notificación, por ejemplo PAGO
    @NotBlank(message = "El tipo de notificación es obligatorio")
    private String tipo;

    // Correo destinatario de la notificación
    @NotBlank(message = "El destinatario es obligatorio")
    @Email(message = "El destinatario debe ser un correo válido")
    private String destinatario;

    // Fecha asociada a la notificación
    @NotBlank(message = "La fecha es obligatoria")
    private String fecha;

    // ID del usuario que recibirá la notificación
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Integer usuarioId;

}
