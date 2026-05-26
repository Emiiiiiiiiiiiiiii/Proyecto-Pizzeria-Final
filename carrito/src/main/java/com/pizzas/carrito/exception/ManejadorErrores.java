package com.pizzas.carrito.exception;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice

public class ManejadorErrores {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> manejarErroresValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                400,
                "Error de validación en carrito",
                errores,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorDTO);
    }

    @ExceptionHandler(ExcepcionPersonalizada.class)
    public ResponseEntity<ErrorDTO> manejarExcepcionPersonalizada(ExcepcionPersonalizada ex, HttpServletRequest request) {
        int status = 400;

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                status,
                ex.getMessage(),
                new HashMap<>(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorDTO);
    }

}
