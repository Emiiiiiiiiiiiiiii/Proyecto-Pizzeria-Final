package com.pizzas.autenticacion.model;

// Evita que la contraseña se muestre cuando se responde en JSON
import com.fasterxml.jackson.annotation.JsonProperty;

// Anotaciones para trabajar con JPA y crear la tabla en la base de datos
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Anotaciones para validar los datos que llegan al microservicio
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Lombok crea automáticamente getters, setters, constructores y otros métodos
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Indica que esta clase será una entidad/tabla en la base de datos
@Entity

// Define el nombre de la tabla en la base de datos
@Table(name = "usuarios")

// Genera getters, setters, toString, equals y hashCode automáticamente
@Data

// Constructor vacío necesario para JPA
@NoArgsConstructor

// Constructor con todos los atributos
@AllArgsConstructor
public class Usuario {

    // Clave primaria de la tabla usuarios
    @Id

    // Hace que el ID se genere automáticamente en la base de datos
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Valida que el nombre no venga vacío
    @NotBlank(message = "El nombre no puede estar vacío")

    // Limita el tamaño del nombre
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")

    // Permite solo letras y espacios
    @Pattern(
        regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$",
        message = "El nombre solo puede contener letras y espacios"
    )

    // Define la columna en la BD como obligatoria y con máximo 50 caracteres
    @Column(nullable = false, length = 50)
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

    // Define la columna en la BD como obligatoria y con máximo 50 caracteres
    @Column(nullable = false, length = 50)
    private String apellido;

    // Valida que el email no venga vacío
    @NotBlank(message = "El email no puede estar vacío")

    // Valida que tenga formato de correo electrónico
    @Email(message = "Debe ser un correo válido")

    // Limita el largo máximo del email
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")

    // Define la columna como obligatoria, única y con máximo 100 caracteres
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Hace que la contraseña solo pueda recibirse, pero no mostrarse en respuestas JSON
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)

    // Valida que la contraseña no venga vacía
    @NotBlank(message = "La contraseña no puede estar vacía")

    // Se deja máximo 100 porque aquí se guarda la contraseña encriptada con BCrypt
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")

    // Define la columna password como obligatoria y con espacio suficiente para el hash
    @Column(nullable = false, length = 100)
    private String password;

    // Valida que el rol solo pueda ser CLIENTE o ADMIN
    @Pattern(
        regexp = "CLIENTE|ADMIN",
        message = "El rol debe ser CLIENTE o ADMIN"
    )

    // Define la columna rol como obligatoria
    @Column(nullable = false, length = 20)
    private String rol;
}