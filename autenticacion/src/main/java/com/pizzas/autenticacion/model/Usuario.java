package com.pizzas.autenticacion.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Column(nullable = false, length = 100)
    private String apellido;

    @Email(message = "Debe ser un correo válido")
    @NotBlank(message = "El email no puede estar vacío")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(nullable = false)
    private String password;

    private String rol;

}
