package com.servicio.reparto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para responder datos de reparto
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepartoResponseDTO {

    private Long id;
    private Integer pedidoId;
    private Integer usuarioId;

    private String nombreCliente;
    private String emailCliente;

    private String direccionEntrega;
    private String estadoReparto;

    private String repartidor;
    private String horaEntrega;

}
