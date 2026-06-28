package com.pizzas.certificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta completa de una certificación generada
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificacionResponseDTO {
    
    private Integer id;
    private Integer pedidoId;
    private Integer usuarioId;

    private String nombreUsuario;
    private String emailUsuario;

    private String fechaPedido;
    private String detalleProductos;
    private Integer cantidadTotalItems;

    private Integer montoTotal;
    private String metodoPago;
    private String estadoPedido;

    private String nombreRepartidor;
    private String horaEntrega;
    private String estadoPuntualidad;

    private Boolean tieneResena;
    private String comentarioResena;
    private Integer estrellasResena;

    private String fechaEmision;
    private String mensaje;

}
