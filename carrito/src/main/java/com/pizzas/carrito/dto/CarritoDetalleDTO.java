package com.pizzas.carrito.dto;

import lombok.Data;

@Data

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
