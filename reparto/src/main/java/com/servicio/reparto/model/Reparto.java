package com.servicio.reparto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "repartos")
public class Reparto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
