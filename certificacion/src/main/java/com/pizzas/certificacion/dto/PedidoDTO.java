package com.pizzas.certificacion.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PedidoDTO {
    private Integer id;
    private Integer usuarioId;
    private String nombreCliente;
    private String emailCliente;
    private String fechaPedido;

}
