package com.pizzas.certificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CertificacionResponseDTO {
    private Integer id;
    private Integer pedidoId;
    private Integer usuarioId;
    private String nombreUsuario;
    private String emailUsuario;
    private String nombreRepartidor;
    private String fechaPedido;
    private String horaEntrega;
    private String estadoPuntualidad;
    private String fechaEmision;

}
