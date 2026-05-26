package com.pizzas.autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class FormularioIngresoDTO {
    @Email(message = "Debe ser un correo válido")
    @NotBlank(message = "El email es obligatorio para iniciar sesión")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria para iniciar sesión")
    private String password;

}
