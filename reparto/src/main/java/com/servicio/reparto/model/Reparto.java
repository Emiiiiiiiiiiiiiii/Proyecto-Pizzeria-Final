package com.servicio.reparto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Modelo que representa el reparto de un pedido
@Entity
@Table(
    name = "repartos",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_reparto_pedido",
        columnNames = "pedido_id"
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reparto {
    
    // ID único del reparto
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID del pedido asociado al reparto
    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    @Column(name = "pedido_id", nullable = false, unique = true)
    private Integer pedidoId;

    // ID del usuario que realizó el pedido
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    // Nombre del cliente que recibirá el pedido
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 120, message = "El nombre del cliente no puede superar los 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombreCliente;

    // Email del cliente para notificaciones
    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 120, message = "El email no puede superar los 120 caracteres")
    @Column(nullable = false, length = 120)
    private String emailCliente;

    // Dirección donde se entregará el pedido
    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
    @Column(nullable = false, length = 200)
    private String direccionEntrega;

    // Estado actual del reparto
    @NotBlank(message = "El estado del reparto es obligatorio")
    @Pattern(
        regexp = "^(PREPARANDO|EN_CAMINO|ENTREGADO|CANCELADO|RETRASADO)$",
        message = "El estado debe ser PREPARANDO, EN_CAMINO, ENTREGADO, CANCELADO o RETRASADO"
    )
    @Column(nullable = false, length = 30)
    private String estadoReparto;

    // Nombre del repartidor asignado
    @NotBlank(message = "El repartidor es obligatorio")
    @Size(min = 2, max = 120, message = "El repartidor debe tener entre 2 y 120 caracteres")
    @Column(nullable = false, length = 120)
    private String repartidor;

    // Hora estimada o real de entrega
    @NotBlank(message = "La hora de entrega es obligatoria")
    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
        message = "La hora de entrega debe tener el formato yyyy-MM-dd HH:mm:ss"
    )
    @Column(nullable = false, length = 30)
    private String horaEntrega;
}
