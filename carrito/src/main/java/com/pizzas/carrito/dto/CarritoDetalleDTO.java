package com.pizzas.carrito.dto;

// Lombok genera getters, setters y constructores
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para mostrar el carrito con datos de usuario y catálogo
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoDetalleDTO {
    // ID del item del carrito
    private Integer id;

    // ID de la pizza/producto en catálogo
    private Integer catalogoId;

    // Nombre del usuario obtenido desde autenticación
    private String nombreUsuario;

    // Nombre de la pizza obtenido desde catálogo
    private String nombrePizza;

    // Tamaño de la pizza obtenido desde catálogo
    private String tamanio;

    // Cantidad agregada al carrito
    private Integer cantidad;

    // Precio unitario de la pizza
    private Integer precioUnitario;

    // Total calculado según cantidad y precio unitario
    private Integer precioTotal;
}
