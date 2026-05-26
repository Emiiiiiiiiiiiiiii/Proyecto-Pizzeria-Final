package com.pizzas.autenticacion.exception;

import org.springframework.http.HttpStatus;

public class ExcepcionPersonalizada extends RuntimeException{
    private final HttpStatus status;

    public ExcepcionPersonalizada(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
