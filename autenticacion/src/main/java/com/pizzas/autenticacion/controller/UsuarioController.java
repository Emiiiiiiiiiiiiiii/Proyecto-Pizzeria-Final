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
import org.springframework.web.client.RestTemplate;

import com.pizzas.autenticacion.dto.FormularioIngresoDTO;
import com.pizzas.autenticacion.dto.PerfilUsuarioPublicoDTO;
import com.pizzas.autenticacion.model.Usuario;
import com.pizzas.autenticacion.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final RestTemplate restTemplate;

    public UsuarioController(UsuarioService usuarioService, RestTemplate restTemplate) {
    this.usuarioService = usuarioService;
    this.restTemplate = restTemplate;

    }

    //POST http://localhost:8080/auth/registrar
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrar(@Valid @RequestBody Usuario usuario) {
        Map<String, Object> datosRegistro = usuarioService.registrar(usuario);
    
        return new ResponseEntity<>(datosRegistro, HttpStatus.CREATED);

    }

    //POST http://localhost:8080/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody FormularioIngresoDTO formulario) {
        var datosUsuario = usuarioService.iniciarSesion(formulario);
        
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Login exitoso");
        respuesta.put("usuario", datosUsuario);
        
        return ResponseEntity.ok(respuesta);
    }

    //GET http://localhost:8080/auth/usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<List<PerfilUsuarioPublicoDTO>> listar() {
        List<PerfilUsuarioPublicoDTO> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    //GET http://localhost:8080/auth/usuarios/{id}
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<PerfilUsuarioPublicoDTO> buscarPorId(@PathVariable Integer id) {

        PerfilUsuarioPublicoDTO usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    //PUT http://localhost:8080/auth/usuarios/{id}
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(@PathVariable Integer id, @RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioService.actualizar(id, usuario);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario actualizado correctamente");
        respuesta.put("usuario", usuarioActualizado);
        
        return ResponseEntity.ok(respuesta);
    }

    //DELETE http://localhost:8080/auth/usuarios/{id}
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario eliminado con éxito");
        
        return ResponseEntity.ok(respuesta);
    }

}
