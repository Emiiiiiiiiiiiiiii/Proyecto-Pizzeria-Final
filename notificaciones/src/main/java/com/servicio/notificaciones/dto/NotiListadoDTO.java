package com.servicio.notificaciones.dto;

import lombok.Data;

@Data
public class NotiListadoDTO {

    private Integer pedidoId;
    private String tipoNoti;
    private String destinatario;
    private String mensaje;
}
