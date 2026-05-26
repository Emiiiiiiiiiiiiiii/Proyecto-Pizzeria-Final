package com.pizzas.pedido.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice

public class ManejadorErrores {
    @ExceptionHandler(ExcepcionPersonalizada.class)
    public ResponseEntity<ErrorDTO> handleExcepcion(ExcepcionPersonalizada ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            null,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error Interno del Servidor",
            null,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
