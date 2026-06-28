package com.pizzas.carrito.model;

// Anotaciones para crear y configurar la tabla en la base de datos
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Validaciones para controlar los datos del carrito
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

// Lombok genera getters, setters y constructores
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Indica que esta clase será una entidad en la base de datos
@Entity

// Define la tabla y evita que un mismo usuario tenga dos veces el mismo producto en el carrito
@Table(
    name = "carritos",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_producto_carrito", columnNames = {"usuario_id", "catalogo_id"})
    }
)

// Genera getters, setters, toString, equals y hashCode
@Data

// Constructor vacío necesario para JPA
@NoArgsConstructor

// Constructor con todos los atributos
@AllArgsConstructor
public class Carrito {
    // Clave primaria de la tabla carritos
    @Id

    // Genera el ID automáticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ID del usuario dueño del carrito
    @NotNull(message = "El ID del usuario no puede ser nulo")
    @Positive(message = "El ID del usuario debe ser positivo")
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    // ID del producto/pizza que viene desde el microservicio catálogo
    @NotNull(message = "El ID del producto no puede ser nulo")
    @Positive(message = "El ID del producto debe ser positivo")
    @Column(name = "catalogo_id", nullable = false)
    private Integer catalogoId;

    // Cantidad de productos agregados al carrito
    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    @Max(value = 50, message = "No puedes agregar más de 50 unidades del mismo producto")
    @Column(nullable = false)
    private Integer cantidad;

    // Precio unitario del producto al momento de agregarlo al carrito
    @NotNull(message = "El precio unitario no puede ser nulo")
    @PositiveOrZero(message = "El precio unitario no puede ser negativo")
    @Column(nullable = false)
    private Integer precioUnitario;

    // Precio total calculado según cantidad * precioUnitario
    @NotNull(message = "El precio total no puede ser nulo")
    @PositiveOrZero(message = "El precio total no puede ser negativo")
    @Column(nullable = false)
    private Integer precioTotal;

}
