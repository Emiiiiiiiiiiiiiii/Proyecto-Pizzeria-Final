package com.servicio.notificaciones.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para responder datos de una notificación
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotiResponseDTO {


    private Integer id;
    private Integer usuarioId;
    private Integer pedidoId;
    private String tipo;
    private String destinatario;
    private String mensaje;
    private String estado;
    private LocalDateTime fechaEnvio;

    
}
