package com.pizzas.resenas.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Modelo que representa una reseña realizada por un cliente
@Entity
@Table(
    name = "resenas",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_resena_pedido",
        columnNames = "pedido_id"
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resena {

    // ID único de la reseña
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ID del pedido evaluado
    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    @Column(name = "pedido_id", nullable = false, unique = true)
    private Integer pedidoId;

    // ID del usuario que hizo la reseña
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    // Nombre del cliente
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 120, message = "El nombre del cliente no puede superar los 120 caracteres")
    @Column(name = "cliente_nombre", nullable = false, length = 120)
    private String clienteNombre;

    // Producto o detalle del pedido reseñado
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 300, message = "El nombre del producto no puede superar los 300 caracteres")
    @Column(name = "nombre_producto", nullable = false, length = 300)
    private String nombreProducto;

    // Comentario de la reseña
    @NotBlank(message = "El comentario es obligatorio")
    @Size(min = 5, max = 1000, message = "El comentario debe tener entre 5 y 1000 caracteres")
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comentario;

    // Calificación de 1 a 5 estrellas
    @NotNull(message = "Las estrellas son obligatorias")
    @Min(value = 1, message = "La reseña debe tener mínimo 1 estrella")
    @Max(value = 5, message = "La reseña debe tener máximo 5 estrellas")
    @Column(nullable = false)
    private Integer estrellas;

    // Fecha de creación de la reseña
    @NotNull(message = "La fecha de reseña es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "fecha_resena", nullable = false)
    private LocalDateTime fechaResena;
}