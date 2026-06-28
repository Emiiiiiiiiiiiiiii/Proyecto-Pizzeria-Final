package com.pizzas.certificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para recibir datos desde el microservicio pedido
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

   private Integer id;
    private Integer usuarioId;

    private String nombreCliente;
    private String emailCliente;

    private Integer montoTotal;
    private Integer cantidadTotalItems;
    private String detalleProductos;

    private String estado;
    private String fechaPedido;
    private String metodoPago;
}
