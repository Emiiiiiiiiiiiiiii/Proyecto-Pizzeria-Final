package com.pizzas.certificacion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "certificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Certificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer pedidoId;

    @Column(nullable = false)
    private Integer usuarioId;
    
    @Column(nullable = false)
    private String fechaPedido; 
    
    @Column(nullable = false)
    private String nombreUsuario;
    
    @Column(nullable = false)
    private String emailUsuario;
    
    @Column(nullable = false)
    private String nombreRepartidor;
    
    @Column(nullable = false)
    private String horaEntrega; 
    
    @Column(nullable = false)
    private String estadoPuntualidad; 
    
    @Column(nullable = false)
    private String fechaEmision; 

}
