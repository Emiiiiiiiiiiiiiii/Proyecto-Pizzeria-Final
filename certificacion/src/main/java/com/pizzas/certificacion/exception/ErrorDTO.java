package com.pizzas.certificacion.exception;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ErrorDTO {
    private LocalDateTime timestamp;
    private int status;
    private String mensaje;
    private Map<String, String> errores;
    private String path;

}
