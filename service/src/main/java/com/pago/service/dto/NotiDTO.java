package com.pago.service.dto;

import lombok.Data;

@Data
public class NotiDTO {

    private Integer usuarioId;
    private Integer pedidoId;
    private String tipo;
    private String mensaje;
    private String destinatario;
    private String fecha;

}
