package com.servicio.notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class NotiDTO {


    @NotNull(message = "El ID del pedido es obligatorio")
    private Integer pedidoId;

    @NotBlank(message = "El tipo de notificacion es obligatorio")
    private String tipo;

    @NotBlank(message = "El destinatario es obligatorio")
    private String destinatario;

    
    private String fecha;

    private Integer usuarioId;
}
