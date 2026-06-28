package com.pizzas.pedido.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para devolver errores ordenados en JSON
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
        // Fecha y hora en que ocurrió el error
    private LocalDateTime timestamp;

    // Código HTTP del error
    private int status;

    // Mensaje principal del error
    private String mensaje;

    // Detalle de errores, por ejemplo validaciones fallidas
    private Map<String, String> errores;

    // Ruta donde ocurrió el error
    private String path;

}
