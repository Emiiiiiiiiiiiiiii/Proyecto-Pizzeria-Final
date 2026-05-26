package com.pizzas.pedido.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer usuarioId;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;

    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String emailCliente;

    @NotNull(message = "El monto total es obligatorio")
    @Min(value = 0, message = "El monto no puede ser negativo")
    private Integer montoTotal;

    @NotNull(message = "La cantidad de items es obligatoria")
    @Min(value = 1, message = "Debe haber al menos 1 producto")
    private Integer cantidadTotalItems;

    @NotBlank(message = "El estado del pedido es obligatorio")
    private String estado;

    @NotBlank(message = "La fecha del pedido es obligatoria")
    private String fechaPedido;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

}
