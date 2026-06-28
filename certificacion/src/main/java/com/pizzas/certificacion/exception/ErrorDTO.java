package com.pizzas.certificacion.exception;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para devolver errores de forma ordenada
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

    // Lista de errores específicos, por ejemplo validaciones
    private List<String> errores;

    // Ruta donde ocurrió el error
    private String path;

}
