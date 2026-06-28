package com.pizzas.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para responder cuando un pedido se crea o consulta correctamente
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoResponseDTO {
    // ID del pedido creado
    private Integer id;

    // Nombre del cliente
    private String nombreCliente;

    // Monto total del pedido
    private Integer montoTotal;

    // Estado actual del pedido
    private String estado;

    // Fecha de creación del pedido
    private String fechaPedido;

    // Mensaje de respuesta
    private String mensaje;

}
