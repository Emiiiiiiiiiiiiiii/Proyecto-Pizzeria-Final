package com.pizzas.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Modelo que representa el stock de un producto del catálogo
@Entity
@Table(
    name = "inventario",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_inventario_catalogo",
        columnNames = "catalogo_id"
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Inventario {
    // ID único del registro de inventario
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ID del producto en el microservicio catálogo
    @NotNull(message = "El ID del catálogo es obligatorio")
    @Positive(message = "El ID del catálogo debe ser positivo")
    @Column(name = "catalogo_id", nullable = false, unique = true)
    private Integer catalogoId;

    // Cantidad disponible en stock
    @NotNull(message = "La cantidad no puede estar vacía")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer cantidad;

}
