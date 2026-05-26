package com.pizzas.resenas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resenas") // Usamos "resenas" (sin ñ) por seguridad técnica
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private Integer usuarioId;
    
    @Column(nullable = false)
    private Integer pedidoId;
    
    @Column(nullable = false)
    private Integer catalogoId;
    
    @Column(nullable = false, length = 500)
    private String comentario;
    
    @Column(nullable = false)
    private Integer estrellas;
}