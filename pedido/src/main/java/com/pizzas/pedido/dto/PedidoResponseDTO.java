package com.pizzas.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//Este es el objeto que devolveremos al usuario cuando el pedido se cree con éxito.
public class PedidoResponseDTO {
    private Integer id;
    private String nombreCliente;
    private Integer montoTotal;
    private String estado;
    private String fechaPedido;
    private String mensaje;

}
