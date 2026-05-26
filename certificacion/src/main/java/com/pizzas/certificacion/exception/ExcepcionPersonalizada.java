package com.pizzas.certificacion.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter

public class ExcepcionPersonalizada extends RuntimeException{
    private final HttpStatus status;

    public ExcepcionPersonalizada(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

}
