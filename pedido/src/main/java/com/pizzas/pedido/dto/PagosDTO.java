package com.pizzas.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagosDTO {
    private Integer monto;
    private Integer pedidoId;
    private Integer usuarioId;

}
