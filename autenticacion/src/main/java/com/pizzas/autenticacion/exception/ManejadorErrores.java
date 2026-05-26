package com.pizzas.autenticacion.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice

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
                "Error de validación",
                errores,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorDTO);
    }

    @ExceptionHandler(ExcepcionPersonalizada.class)
    public ResponseEntity<ErrorDTO> manejarExcepcionPersonalizada(ExcepcionPersonalizada ex, HttpServletRequest request) {
        
        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getMessage(),
                new HashMap<>(),
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDTO> manejarDuplicadosBaseDatos(DataIntegrityViolationException ex, HttpServletRequest request) {
        
        Map<String, String> errores = new HashMap<>();
        errores.put("email", "El correo electrónico ya se encuentra registrado.");

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                400,
                "Error de duplicidad en base de datos",
                errores,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorDTO);
    }

}
