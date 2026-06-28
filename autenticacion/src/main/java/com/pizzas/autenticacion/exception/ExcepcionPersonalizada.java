package com.pizzas.autenticacion.exception;

import org.springframework.http.HttpStatus;

// Excepción propia para enviar mensajes y códigos HTTP personalizados
public class ExcepcionPersonalizada extends RuntimeException{
    private static final long serialVersionUID = 1L; //Se usa por buena práctica al crear una excepción personalizada, ya que RuntimeException es serializable.

    // Código HTTP que se devolverá al cliente
    private final HttpStatus status;

    public ExcepcionPersonalizada(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
