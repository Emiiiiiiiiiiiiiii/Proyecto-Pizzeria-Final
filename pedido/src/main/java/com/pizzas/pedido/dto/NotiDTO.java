package com.pizzas.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotiDTO {
    private Integer pedidoId;
    private String tipo;
    private String destinatario;
    private String fecha;
    private Integer usuarioId;

}
