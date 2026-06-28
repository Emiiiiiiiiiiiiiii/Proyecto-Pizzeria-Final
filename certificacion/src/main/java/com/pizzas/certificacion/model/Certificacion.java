package com.pizzas.certificacion.model;

// Anotaciones para crear y configurar la tabla en la base de datos
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

// Validaciones para controlar los datos guardados en certificación
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Lombok genera getters, setters y constructores
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Indica que esta clase será una entidad en la base de datos
@Entity

// Define el nombre de la tabla certificaciones
@Table(name = "certificaciones")

// Genera getters, setters, toString, equals y hashCode
@Data

// Constructor vacío necesario para JPA
@NoArgsConstructor

// Constructor con todos los atributos
@AllArgsConstructor
public class Certificacion {
    // Clave primaria de la tabla certificaciones
    @Id

    // Genera el ID automáticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ID del pedido certificado
    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    @Column(nullable = false, unique = true)
    private Integer pedidoId;

    // ID del usuario que realizó el pedido
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    @Column(nullable = false)
    private Integer usuarioId;

    // Nombre del cliente que realizó la compra
    @NotBlank(message = "El nombre del usuario es obligatorio")
    @Size(max = 120, message = "El nombre del usuario no puede superar los 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombreUsuario;

    // Email del cliente que realizó la compra
    @NotBlank(message = "El email del usuario es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 120, message = "El email no puede superar los 120 caracteres")
    @Column(nullable = false, length = 120)
    private String emailUsuario;

    // Fecha en que se realizó el pedido
    @NotBlank(message = "La fecha del pedido es obligatoria")
    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
        message = "La fecha del pedido debe tener el formato yyyy-MM-dd HH:mm:ss"
    )
    @Column(nullable = false, length = 30)
    private String fechaPedido;

    // Resumen de pizzas compradas con cantidad, por ejemplo: 2x Pepperoni, 1x Napolitana
    @NotBlank(message = "El detalle de productos es obligatorio")
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalleProductos;

    // Cantidad total de productos comprados
    @NotNull(message = "La cantidad total de items es obligatoria")
    @Min(value = 1, message = "La cantidad total debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidadTotalItems;

    // Total pagado por el pedido
    @NotNull(message = "El monto total es obligatorio")
    @PositiveOrZero(message = "El monto total no puede ser negativo")
    @Column(nullable = false)
    private Integer montoTotal;

    // Método de pago usado en el pedido
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(
        regexp = "^(EFECTIVO|DEBITO|CREDITO|TRANSFERENCIA)$",
        message = "El método de pago debe ser EFECTIVO, DEBITO, CREDITO o TRANSFERENCIA"
    )
    @Column(nullable = false, length = 30)
    private String metodoPago;

    // Estado del pedido al momento de certificar
    @NotBlank(message = "El estado del pedido es obligatorio")
    @Pattern(
        regexp = "^(PENDIENTE|PAGADO|EN_PREPARACION|EN_REPARTO|ENTREGADO|CANCELADO|RECHAZADO)$",
        message = "El estado del pedido no es válido"
    )
    @Column(nullable = false, length = 30)
    private String estadoPedido;

    // Nombre del repartidor asociado al pedido
    @NotBlank(message = "El nombre del repartidor es obligatorio")
    @Size(max = 120, message = "El nombre del repartidor no puede superar los 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombreRepartidor;

    // Hora o fecha de entrega informada por reparto
    @NotBlank(message = "La hora de entrega es obligatoria")
    @Size(max = 40, message = "La hora de entrega no puede superar los 40 caracteres")
    @Column(nullable = false, length = 40)
    private String horaEntrega;

    // Indica si el pedido fue entregado a tiempo o con retraso
    @NotBlank(message = "El estado de puntualidad es obligatorio")
    @Pattern(
        regexp = "^(A TIEMPO|CON RETRASO|SIN INFORMACION)$",
        message = "El estado de puntualidad debe ser A TIEMPO, CON RETRASO o SIN INFORMACION"
    )
    @Column(nullable = false, length = 30)
    private String estadoPuntualidad;

    // Indica si el cliente dejó reseña
    @NotNull(message = "Debe indicarse si existe reseña")
    @Column(nullable = false)
    private Boolean tieneResena;

    // Comentario de la reseña, si existe
    @Size(max = 500, message = "El comentario de la reseña no puede superar los 500 caracteres")
    @Column(length = 500)
    private String comentarioResena;

    // Cantidad de estrellas de la reseña, si existe
    @Min(value = 0, message = "Las estrellas no pueden ser negativas")
    @Max(value = 5, message = "Las estrellas no pueden superar 5")
    @Column
    private Integer estrellasResena;

    // Fecha en que se generó la certificación
    @NotBlank(message = "La fecha de emisión es obligatoria")
    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
        message = "La fecha de emisión debe tener el formato yyyy-MM-dd HH:mm:ss"
    )
    @Column(nullable = false, length = 30)
    private String fechaEmision;
}
