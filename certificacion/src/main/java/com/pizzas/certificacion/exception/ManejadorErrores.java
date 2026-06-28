package com.pizzas.certificacion.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

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

// Maneja errores de forma centralizada para todo el microservicio certificación
@RestControllerAdvice
public class ManejadorErrores {
    // Logger para registrar errores del microservicio
    private static final Logger logger = LoggerFactory.getLogger(ManejadorErrores.class);

    // Maneja errores de validación de DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> manejarValidaciones(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                errores,
                request.getRequestURI()
        );

        logger.warn("Error de validación en {}: {}", request.getRequestURI(), errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    // Maneja errores personalizados del negocio
    @ExceptionHandler(ExcepcionPersonalizada.class)
    public ResponseEntity<ErrorDTO> manejarExcepcionPersonalizada(
            ExcepcionPersonalizada ex,
            HttpServletRequest request) {

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getMessage(),
                List.of(ex.getMessage()),
                request.getRequestURI()
        );

        logger.warn("Error controlado en {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(errorDTO);
    }

    // Maneja errores de integridad de base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDTO> manejarIntegridadDatos(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Error de integridad en la base de datos",
                List.of("No se pudo guardar la información porque existen datos repetidos o inválidos."),
                request.getRequestURI()
        );

        logger.error("Error de integridad en {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDTO);
    }

    // Maneja JSON mal escrito o con tipos incorrectos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> manejarJsonInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "JSON inválido",
                List.of("Revisa que el body esté bien escrito y que los tipos de datos sean correctos."),
                request.getRequestURI()
        );

        logger.warn("JSON inválido en {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    // Maneja errores cuando un PathVariable tiene tipo incorrecto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> manejarTipoIncorrecto(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Tipo de dato incorrecto",
                List.of("El parámetro '" + ex.getName() + "' tiene un valor inválido."),
                request.getRequestURI()
        );

        logger.warn("Tipo incorrecto en {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    // Maneja cualquier error no controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {

        ErrorDTO errorDTO = new ErrorDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                List.of("Ocurrió un error inesperado en certificación."),
                request.getRequestURI()
        );

        logger.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
    }

}
