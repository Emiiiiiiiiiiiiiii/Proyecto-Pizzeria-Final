package com.pizzas.inventario.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice

public class ManejadorErorres {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> manejarErroresValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                400,
                "Error de validación en inventario",
                errores,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorDTO);
    }

    @ExceptionHandler(ExcepcionPersonalizada.class)
    public ResponseEntity<ErrorDTO> manejarExcepcionPersonalizada(ExcepcionPersonalizada ex, HttpServletRequest request) {
    
        int status;
        String mensajeFinal;

        
        if (ex.getMessage().contains("No puedes modificar el catalogoId")) {
            status = 400;
            mensajeFinal = ex.getMessage();
        }

        if (ex.getMessage().contains("existe un registro")) {
            status = 409;
            mensajeFinal = "Error: Este producto ya tiene inventario. Por favor, usa PUT para actualizar la cantidad.";
        } else if (ex.getMessage().contains("no existe en el catálogo")) {
            status = 404;
            mensajeFinal = ex.getMessage();
        } else {
            status = 400;
            mensajeFinal = ex.getMessage();
        }

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                status,
                mensajeFinal,
                new HashMap<>(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(errorDTO);
    }

}
