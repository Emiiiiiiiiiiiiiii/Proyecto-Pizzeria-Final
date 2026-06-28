package com.pizzas.pedido.exception;

import org.springframework.http.HttpStatus;

// Excepción propia para manejar errores del microservicio pedido
public class ExcepcionPersonalizada extends RuntimeException{
    private static final long serialVersionUID = 1L;

    // Código HTTP que se devolverá al cliente
    private final HttpStatus status;

    // Constructor con estado por defecto BAD_REQUEST
    public ExcepcionPersonalizada(String mensaje) {
        super(mensaje);
        this.status = HttpStatus.BAD_REQUEST;
    }

    // Constructor para indicar un estado HTTP específico
    public ExcepcionPersonalizada(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    // Devuelve el código HTTP asociado al error
    public HttpStatus getStatus() {
        return status;
    }

}
