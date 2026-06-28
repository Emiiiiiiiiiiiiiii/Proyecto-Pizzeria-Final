package com.pizzas.certificacion.exception;

import org.springframework.http.HttpStatus;

// Excepción propia para manejar errores del microservicio
public class ExcepcionPersonalizada extends RuntimeException{
    private static final long serialVersionUID = 1L;

    // Estado HTTP que se devolverá al cliente
    private final HttpStatus status;

    // Constructor con estado BAD_REQUEST por defecto
    public ExcepcionPersonalizada(String mensaje) {
        super(mensaje);
        this.status = HttpStatus.BAD_REQUEST;
    }

    // Constructor con estado HTTP específico
    public ExcepcionPersonalizada(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    // Retorna el estado HTTP asociado a la excepción
    public HttpStatus getStatus() {
        return status;
    }

}
