package com.pizzas.carrito.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carritos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El ID del cliente no puede ser nulo")
    private Integer usuarioId;

    @NotNull(message = "El ID del producto de catálogo no puede ser nulo")
    private Integer catalogoId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    private Integer cantidad;

    @NotNull(message = "El precio unitario no puede ser nulo")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Integer precioUnitario;
    @NotNull(message = "El precio total no puede ser nulo")
    private Integer precioTotal;

}
