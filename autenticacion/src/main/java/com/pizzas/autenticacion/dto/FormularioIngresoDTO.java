package com.pizzas.autenticacion.dto;

// Evita que la contraseña se muestre si por error este DTO se responde en JSON
import com.fasterxml.jackson.annotation.JsonProperty;

// Validaciones para los datos que llegan en el login
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok genera getters, setters y constructores
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Genera getters, setters, toString, equals y hashCode automáticamente
@Data

// Constructor vacío
@NoArgsConstructor

// Constructor con todos los atributos
@AllArgsConstructor

public class FormularioIngresoDTO {
    // Valida que el email no venga vacío
    @NotBlank(message = "El email es obligatorio para iniciar sesión")

    // Valida que tenga formato de correo electrónico
    @Email(message = "Debe ser un correo válido")

    // Limita el largo máximo del email
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    // Hace que la contraseña solo pueda recibirse, pero no mostrarse en respuestas JSON
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)

    // Valida que la contraseña no venga vacía
    @NotBlank(message = "La contraseña es obligatoria para iniciar sesión")

    // Aquí sí dejamos máximo 50 porque es la contraseña escrita por el usuario, no la encriptada
    @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres")
    private String password;

}
