package com.pizzas.autenticacion.dto;

// Validaciones para los datos que llegan al registrar un usuario
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class UsuarioRegistroDTO {
    // Valida que el nombre no venga vacío
    @NotBlank(message = "El nombre no puede estar vacío")

    // Limita el tamaño del nombre
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")

    // Permite solo letras y espacios
    @Pattern(
        regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$",
        message = "El nombre solo puede contener letras y espacios"
    )
    private String nombre;

    // Valida que el apellido no venga vacío
    @NotBlank(message = "El apellido no puede estar vacío")

    // Limita el tamaño del apellido
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")

    // Permite solo letras y espacios
    @Pattern(
        regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$",
        message = "El apellido solo puede contener letras y espacios"
    )
    private String apellido;

    // Valida que el email no venga vacío
    @NotBlank(message = "El email no puede estar vacío")

    // Valida que tenga formato de correo electrónico
    @Email(message = "Debe ser un correo válido")

    // Limita el largo máximo del email
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    // Valida que la contraseña no venga vacía
    @NotBlank(message = "La contraseña no puede estar vacía")

    // Aquí sí máximo 50 porque es la contraseña real escrita por el usuario
    @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres")
    private String password;

}
