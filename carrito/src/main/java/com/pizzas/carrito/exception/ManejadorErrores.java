package com.pizzas.carrito.exception;


import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ManejadorErrores {
     // Logger para registrar errores del microservicio carrito
    private static final Logger logger = LoggerFactory.getLogger(ManejadorErrores.class);

    // Maneja errores de validación producidos por @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> manejarErroresValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errores = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        logger.warn("Error de validación en {}: {}", request.getRequestURI(), errores);

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                errores,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorDTO);
    }

    // Maneja errores personalizados lanzados desde el service
    @ExceptionHandler(ExcepcionPersonalizada.class)
    public ResponseEntity<ErrorDTO> manejarExcepcionPersonalizada(
            ExcepcionPersonalizada ex,
            HttpServletRequest request) {

        logger.warn("Error controlado en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getMessage(),
                new LinkedHashMap<>(),
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(errorDTO);
    }

    // Maneja errores de duplicidad, como repetir usuario + producto en carrito
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDTO> manejarErrorIntegridad(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        Map<String, String> errores = new LinkedHashMap<>();
        errores.put("carrito", "El producto ya existe en el carrito de este usuario.");

        logger.warn("Error de integridad en base de datos en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de integridad en base de datos",
                errores,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorDTO);
    }

    // Maneja errores cuando el JSON viene mal escrito
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> manejarJsonInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        Map<String, String> errores = new LinkedHashMap<>();
        errores.put("body", "El cuerpo de la solicitud no tiene un formato JSON válido.");

        logger.warn("JSON inválido en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud inválida",
                errores,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorDTO);
    }

    // Maneja errores cuando un path variable tiene tipo incorrecto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> manejarTipoDatoIncorrecto(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        Map<String, String> errores = new LinkedHashMap<>();
        errores.put(ex.getName(), "El valor ingresado no tiene el tipo de dato correcto.");

        logger.warn("Tipo de dato incorrecto en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Parámetro inválido",
                errores,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorDTO);
    }

    // Maneja cualquier error inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> manejarErroresGenerales(
            Exception ex,
            HttpServletRequest request) {

        logger.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error interno en el microservicio de carrito.",
                new LinkedHashMap<>(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
    }

}
