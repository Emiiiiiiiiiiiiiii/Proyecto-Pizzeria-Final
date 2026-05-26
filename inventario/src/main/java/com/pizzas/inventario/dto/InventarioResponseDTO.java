package com.pizzas.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class InventarioResponseDTO {
    private Integer id;
    private Integer catalogoId;
    private Integer cantidad;

}
