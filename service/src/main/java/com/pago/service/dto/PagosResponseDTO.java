package com.pago.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PagosResponseDTO {
    private Integer id;
    private Integer monto;
    private String estado;
    private String nombreUsuario;
    private Integer pedidoId;
    private String mensaje;

}
