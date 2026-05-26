package com.pizzas.certificacion.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class RepartoDTO {
    private Integer id;
    private Integer pedidoId;
    private String repartidor;
    private String horaEntrega;
    private String direccionEntrega;

}
