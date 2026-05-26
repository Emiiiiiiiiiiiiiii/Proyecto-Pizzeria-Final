package com.servicio.notificaciones.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNoti;

    @NotNull
    private Integer pedidoId;
    
    @NotBlank
    private String tipo;

    @NotBlank
    @Email
    private String destinatario;
    
    
    private String fecha;

    private String mensaje;

    private Integer usuarioId;

}
