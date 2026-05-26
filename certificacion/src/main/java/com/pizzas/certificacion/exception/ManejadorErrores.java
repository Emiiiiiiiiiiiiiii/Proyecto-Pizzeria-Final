package com.pizzas.certificacion.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;

@ControllerAdvice

public class ManejadorErrores {
    @ExceptionHandler(ExcepcionPersonalizada.class)
    public ResponseEntity<ErrorDTO> handleExcepcion(ExcepcionPersonalizada ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO(
            LocalDateTime.now(),
            ex.getStatus().value(),
            ex.getMessage(),
            null,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Ocurrió un error inesperado: " + ex.getMessage(),
            null,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
