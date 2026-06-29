package com.pizzas.resenas.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzas.resenas.dto.ResenaRequestDTO;
import com.pizzas.resenas.dto.ResenaResponseDTO;
import com.pizzas.resenas.dto.ResenaUpdateDTO;
import com.pizzas.resenas.service.ResenaService;

// Controlador REST para gestionar reseñas
@RestController
@RequestMapping("/resenas")
public class ResenaController {

    private final ResenaService service;

    // Constructor para inyectar service
    public ResenaController(ResenaService service) {
        this.service = service;
    }

    // Crea una reseña
    @PostMapping("/guardar")
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody ResenaRequestDTO dto) {
        ResenaResponseDTO resena = service.crearResena(dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña registrada correctamente");
        respuesta.put("resena", resena);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Ruta alternativa para crear reseña
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearAlternativo(@Valid @RequestBody ResenaRequestDTO dto) {
        ResenaResponseDTO resena = service.crearResena(dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña registrada correctamente");
        respuesta.put("resena", resena);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Lista todas las reseñas
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTodas() {
        List<ResenaResponseDTO> resenas = service.listarTodas();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de reseñas obtenido correctamente");
        respuesta.put("total", resenas.size());
        respuesta.put("resenas", resenas);

        return ResponseEntity.ok(respuesta);
    }

    // Busca reseña por ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Integer id) {
        ResenaResponseDTO resena = service.buscarPorId(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña encontrada");
        respuesta.put("resena", resena);

        return ResponseEntity.ok(respuesta);
    }

    // Busca reseñas por pedido, esta ruta la usa certificación
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<ResenaResponseDTO>> buscarPorPedido(@PathVariable Integer pedidoId) {
        List<ResenaResponseDTO> resenas = service.buscarPorPedido(pedidoId);
        return ResponseEntity.ok(resenas);
    }

    // Busca reseñas por pedido con respuesta detallada para Postman
    @GetMapping("/pedido/{pedidoId}/respuesta")
    public ResponseEntity<Map<String, Object>> buscarPorPedidoRespuesta(@PathVariable Integer pedidoId) {
        List<ResenaResponseDTO> resenas = service.buscarPorPedido(pedidoId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseñas encontradas para el pedido");
        respuesta.put("total", resenas.size());
        respuesta.put("resenas", resenas);

        return ResponseEntity.ok(respuesta);
    }

    // Busca reseñas por usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(@PathVariable Integer usuarioId) {
        List<ResenaResponseDTO> resenas = service.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseñas encontradas para el usuario");
        respuesta.put("total", resenas.size());
        respuesta.put("resenas", resenas);

        return ResponseEntity.ok(respuesta);
    }

    // Busca reseñas por estrellas
    @GetMapping("/estrellas/{estrellas}")
    public ResponseEntity<Map<String, Object>> buscarPorEstrellas(@PathVariable Integer estrellas) {
        List<ResenaResponseDTO> resenas = service.buscarPorEstrellas(estrellas);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseñas encontradas por estrellas");
        respuesta.put("total", resenas.size());
        respuesta.put("resenas", resenas);

        return ResponseEntity.ok(respuesta);
    }

    // Actualiza reseña por ID
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ResenaUpdateDTO dto) {

        ResenaResponseDTO resena = service.actualizar(id, dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña actualizada correctamente");
        respuesta.put("resena", resena);

        return ResponseEntity.ok(respuesta);
    }

    // Elimina reseña por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {
        service.eliminar(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña eliminada correctamente");
        respuesta.put("idEliminado", id);

        return ResponseEntity.ok(respuesta);
    }
}