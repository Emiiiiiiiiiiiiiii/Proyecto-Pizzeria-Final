package com.pago.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO usado para responder errores de forma ordenada
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ErrorDTO {


    private LocalDateTime timestamp;
    private int status;
    private String mensaje;
    private List<String> errores;
    private String path;
    
}
