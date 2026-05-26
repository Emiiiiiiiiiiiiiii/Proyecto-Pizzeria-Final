package com.pizzas.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CatalogoDTO {
    private Integer id;
    private String nombre;
    private String tipo;
    private String tamanio;
    private Integer precio;

}
