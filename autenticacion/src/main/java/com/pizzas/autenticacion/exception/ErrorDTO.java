package com.pizzas.autenticacion.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para responder errores de forma ordenada en JSON
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ErrorDTO {
    // Fecha y hora en que ocurrió el error
    private LocalDateTime timestamp;

    // Código HTTP del error
    private int status;

    // Mensaje general del error
    private String mensaje;

    // Lista de errores específicos, por ejemplo errores de validación
    private Map<String, String> errores;

    // Ruta donde ocurrió el error
    private String path;
}
