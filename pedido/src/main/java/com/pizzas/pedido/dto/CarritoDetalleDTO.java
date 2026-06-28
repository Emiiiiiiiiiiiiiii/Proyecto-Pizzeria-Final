package com.pizzas.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para recibir productos del carrito
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarritoDetalleDTO {
    // ID del item del carrito
    private Integer id;

    // ID de la pizza/producto
    private Integer catalogoId;

    // Nombre del usuario enviado desde carrito
    private String nombreUsuario;

    // Nombre de la pizza
    private String nombrePizza;

    // Tamaño de la pizza
    private String tamanio;

    // Cantidad agregada
    private Integer cantidad;

    // Precio unitario del producto
    private Integer precioUnitario;

    // Precio total del item
    private Integer precioTotal;

}
