package com.pago.service.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// Modelo que representa un pago realizado por un pedido
@Entity
@Table(name = "pagos")
@Data
public class Pagos {

    // ID único del pago
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Monto pagado
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    @Column(nullable = false)
    private Integer monto;

    // Estado del pago
    @NotBlank(message = "El estado del pago es obligatorio")
    @Pattern(
        regexp = "^(APROBADO|RECHAZADO|PENDIENTE)$",
        message = "El estado debe ser APROBADO, RECHAZADO o PENDIENTE"
    )
    @Column(nullable = false, length = 30)
    private String estado;

    // Fecha en que se registró el pago
    @NotNull(message = "La fecha de pago es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime fechaPago;

    // ID del pedido pagado
    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    @Column(nullable = false, unique = true)
    private Integer pedidoId;

    // ID del usuario que realiza el pago
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    @Column(nullable = false)
    private Integer usuarioId;
}
