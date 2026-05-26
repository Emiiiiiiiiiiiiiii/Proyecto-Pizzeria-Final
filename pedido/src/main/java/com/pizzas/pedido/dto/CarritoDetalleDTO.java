package com.pizzas.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarritoDetalleDTO {
    private Integer id;
    private Integer catalogoId;
    private String nombreUsuario;
    private String nombrePizza;
    private String tamanio;
    private Integer cantidad;
    private Integer precioUnitario;
    private Integer precioTotal;

}
