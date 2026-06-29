package com.pago.service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para responder información del pago
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagosResponseDTO {
    
    private Integer id;
    private Integer monto;
    private String estado;
    private Integer pedidoId;
    private Integer usuarioId;
    private String nombreUsuario;
    private LocalDateTime fechaPago;
    private String mensaje;

}
