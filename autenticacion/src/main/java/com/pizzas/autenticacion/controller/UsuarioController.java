package com.pizzas.autenticacion.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// DTOs usados para login, registro y respuesta pública
import com.pizzas.autenticacion.dto.FormularioIngresoDTO;
import com.pizzas.autenticacion.dto.PerfilUsuarioPublicoDTO;
import com.pizzas.autenticacion.dto.UsuarioRegistroDTO;

// Modelo usado para actualizar datos del usuario
import com.pizzas.autenticacion.model.Usuario;

// Service donde está la lógica de negocio
import com.pizzas.autenticacion.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro, login y gestión de usuarios")
public class UsuarioController {
    // Service de autenticación
    private final UsuarioService usuarioService;

    // Constructor para inyectar el service
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // POST http://localhost:8080/auth/registrar
    @Operation(
            summary = "Registrar usuario",
            description = "Registra un nuevo usuario cliente en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en el formulario"),
            @ApiResponse(responseCode = "409", description = "El email ya se encuentra registrado")
    })
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrar(@Valid @RequestBody UsuarioRegistroDTO usuario) {

        Map<String, Object> datosRegistro = usuarioService.registrar(usuario);

        return new ResponseEntity<>(datosRegistro, HttpStatus.CREATED);
    }

    // POST http://localhost:8080/auth/login
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida email y contraseña para iniciar sesión en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en el formulario"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody FormularioIngresoDTO formulario) {

        PerfilUsuarioPublicoDTO datosUsuario = usuarioService.iniciarSesion(formulario);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Login exitoso");
        respuesta.put("usuario", datosUsuario);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8080/auth/usuarios
    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene el listado completo de usuarios registrados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente")
    })
    @GetMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> listar() {

        List<PerfilUsuarioPublicoDTO> usuarios = usuarioService.listarUsuarios();

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Usuarios obtenidos correctamente");
        respuesta.put("total", usuarios.size());
        respuesta.put("usuarios", usuarios);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8080/auth/usuarios/{id}
    @Operation(
            summary = "Buscar usuario por ID",
            description = "Obtiene la información pública de un usuario según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Integer id) {

        PerfilUsuarioPublicoDTO usuario = usuarioService.buscarPorId(id);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Usuario encontrado correctamente");
        respuesta.put("usuario", usuario);

        return ResponseEntity.ok(respuesta);
    }

    // PUT http://localhost:8080/auth/usuarios/{id}
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza los datos de un usuario existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Email ya utilizado por otro usuario")
    })
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @RequestBody Usuario usuario) {

        PerfilUsuarioPublicoDTO usuarioActualizado = usuarioService.actualizar(id, usuario);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario actualizado correctamente");
        respuesta.put("usuario", usuarioActualizado);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8080/auth/usuarios/{id}
    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario del sistema según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Integer id) {

        usuarioService.eliminar(id);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario eliminado correctamente");

        return ResponseEntity.ok(respuesta);
    }

}
