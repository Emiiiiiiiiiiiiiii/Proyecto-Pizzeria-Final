package com.servicio.notificaciones.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Modelo que representa una notificación enviada al usuario
@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    // ID único de la notificación
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ID del usuario que recibe la notificación
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    // ID del pedido relacionado, puede ser nulo si la notificación no depende de un pedido
    @Positive(message = "El ID del pedido debe ser positivo")
    @Column(name = "pedido_id")
    private Integer pedidoId;

    // Tipo de notificación
    @NotBlank(message = "El tipo de notificación es obligatorio")
    @Size(max = 50, message = "El tipo no puede superar los 50 caracteres")
    @Column(nullable = false, length = 50)
    private String tipo;

    // Email o destinatario de la notificación
    @NotBlank(message = "El destinatario es obligatorio")
    @Email(message = "El destinatario debe ser un email válido")
    @Size(max = 120, message = "El destinatario no puede superar los 120 caracteres")
    @Column(nullable = false, length = 120)
    private String destinatario;

    // Mensaje enviado al usuario
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres")
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    // Estado de la notificación
    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 30, message = "El estado no puede superar los 30 caracteres")
    @Column(nullable = false, length = 30)
    private String estado;

    // Fecha en que se registró/envió la notificación
    @NotNull(message = "La fecha de envío es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

}
