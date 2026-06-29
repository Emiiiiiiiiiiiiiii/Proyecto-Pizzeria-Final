package com.pizzas.pedido.model;

// Anotaciones para crear y configurar la tabla en la base de datos
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Validaciones para controlar los datos del pedido
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.persistence.Lob;


// Lombok genera getters, setters y constructores
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Indica que esta clase será una entidad en la base de datos
@Entity

// Define el nombre de la tabla pedidos
@Table(name = "pedidos")

// Genera getters, setters, toString, equals y hashCode
@Data

// Constructor vacío necesario para JPA
@NoArgsConstructor

// Constructor con todos los atributos
@AllArgsConstructor

public class Pedido {
    // Clave primaria de la tabla pedidos
    @Id

    // Genera el ID automáticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ID del usuario que realizó el pedido
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    @Column(nullable = false)
    private Integer usuarioId;

    // Nombre del cliente obtenido desde autenticación
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre del cliente debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombreCliente;

    // Email del cliente obtenido desde autenticación
    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String emailCliente;

    // Monto total calculado desde los productos del carrito
    @NotNull(message = "El monto total es obligatorio")
    @PositiveOrZero(message = "El monto total no puede ser negativo")
    @Column(nullable = false)
    private Integer montoTotal;

    // Cantidad total de productos del pedido
    @NotNull(message = "La cantidad de items es obligatoria")
    @Min(value = 1, message = "Debe haber al menos 1 producto en el pedido")
    @Column(nullable = false)
    private Integer cantidadTotalItems;

    // Detalle de productos comprados en el pedido
    @NotBlank(message = "El detalle de productos es obligatorio")
    @Size(max = 2000, message = "El detalle de productos no puede superar los 2000 caracteres")
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalleProductos;

    // Estado actual del pedido
    @NotBlank(message = "El estado del pedido es obligatorio")
    @Pattern(
        regexp = "^(PENDIENTE|PAGADO|EN_PREPARACION|EN_REPARTO|ENTREGADO|CANCELADO|RECHAZADO)$",
        message = "El estado debe ser PENDIENTE, PAGADO, EN_PREPARACION, EN_REPARTO, ENTREGADO, CANCELADO o RECHAZADO"
    )
    @Column(nullable = false, length = 30)
    private String estado;

    // Fecha en que se creó el pedido
    @NotBlank(message = "La fecha del pedido es obligatoria")
    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
        message = "La fecha debe tener el formato yyyy-MM-dd HH:mm:ss"
    )
    @Column(nullable = false, length = 30)
    private String fechaPedido;

    // Método de pago seleccionado para el pedido
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(
        regexp = "^(EFECTIVO|DEBITO|CREDITO|TRANSFERENCIA)$",
        message = "El método de pago debe ser EFECTIVO, DEBITO, CREDITO o TRANSFERENCIA"
    )
    @Column(nullable = false, length = 30)
    private String metodoPago;

}
