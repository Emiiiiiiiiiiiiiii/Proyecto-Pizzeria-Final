package com.pizzas.carrito.dto;

// Lombok genera getters, setters y constructores
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para recibir datos de una pizza desde el microservicio catálogo
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    // ID de la pizza/producto
    private Integer id;

    // Nombre de la pizza
    private String nombre;

    // Precio unitario de la pizza
    private Integer precio;

    // Tamaño de la pizza
    private String tamanio;
}
