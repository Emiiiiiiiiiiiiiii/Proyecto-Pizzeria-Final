package com.pago.service;

import org.springframework.http.HttpStatus;

// Excepción propia del microservicio pagos

public class ExcepcionPersonalizada extends RuntimeException{
     private static final long serialVersionUID = 1L;

    // Estado HTTP asociado al error
    private final HttpStatus status;

    // Constructor con BAD_REQUEST por defecto
    public ExcepcionPersonalizada(String mensaje) {
        super(mensaje);
        this.status = HttpStatus.BAD_REQUEST;
    }

    // Constructor con estado específico
    public ExcepcionPersonalizada(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    // Retorna el estado HTTP
    public HttpStatus getStatus() {
        return status;
    }

}
