package com.pago.service.dto;



import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class PagosDTO {

@NotNull(message = "El ID del pedido es obligatorio")
    private Integer pedidoId;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer usuarioId;
}