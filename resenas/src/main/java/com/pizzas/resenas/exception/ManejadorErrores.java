package com.pizzas.resenas.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ManejadorErrores {

    // Manejo de tus errores de negocio personalizados
    @ExceptionHandler(ExcepcionPersonalizada.class)
    public ResponseEntity<ErrorDTO> handleExcepcionPersonalizada(ExcepcionPersonalizada ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            null,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Se activa cuando falla alguna validación como el @NotBlank de tu clase ResenaRequestDTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, Object> errores = new HashMap<>();
    
    // Recorremos todos los campos que fallaron y guardamos su mensaje
    ex.getBindingResult().getFieldErrors().forEach(error -> {
        errores.put(error.getField(), error.getDefaultMessage());
    });

    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put("timestamp", LocalDateTime.now());
    respuesta.put("status", HttpStatus.BAD_REQUEST.value());
    respuesta.put("mensaje", "Error de validación en los campos");
    respuesta.put("errores", errores); // Aquí verás qué campo falta

    return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
}

    // Manejo de errores inesperados del sistema
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
