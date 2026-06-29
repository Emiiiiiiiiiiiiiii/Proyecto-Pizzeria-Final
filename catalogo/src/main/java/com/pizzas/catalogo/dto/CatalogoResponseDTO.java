package com.pizzas.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para responder datos de pizzas
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoResponseDTO {
    
    private Integer id;
    private String nombre;
    private String tipo;
    private String tamanio;
    private Integer precio;

}
