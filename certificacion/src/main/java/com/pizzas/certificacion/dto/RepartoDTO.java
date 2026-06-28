package com.pizzas.certificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para recibir datos desde el microservicio reparto
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepartoDTO {

    private Long id;

    private Integer pedidoId;
    private Integer usuarioId;

    private String nombreCliente;
    private String emailCliente;

    private String direccionEntrega;
    private String estadoReparto;

    private String repartidor;
    private String nombreRepartidor;

    private String horaEntrega;
    private String fechaEntrega;

}
