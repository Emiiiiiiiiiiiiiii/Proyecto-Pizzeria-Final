package com.pizzas.certificacion.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.pizzas.certificacion.dto.CertificacionRequestDTO;
import com.pizzas.certificacion.dto.CertificacionResponseDTO;
import com.pizzas.certificacion.service.CertificacionService;

// Controlador REST para gestionar certificaciones de pedidos
@RestController
@RequestMapping("/certificaciones")
public class CertificacionController {

    // Service con la lógica de certificación
    private final CertificacionService service;

    // Constructor para inyectar el service
    public CertificacionController(CertificacionService service) {
        this.service = service;
    }

    // Genera una certificación a partir de un pedido
    @PostMapping("/generar")
    public ResponseEntity<Map<String, Object>> generarCertificacion(
            @Valid @RequestBody CertificacionRequestDTO request) {

        CertificacionResponseDTO certificacion = service.generarCertificacion(request);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificación generada correctamente");
        respuesta.put("certificacion", certificacion);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Lista todas las certificaciones
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTodas() {

        List<CertificacionResponseDTO> certificaciones = service.listarTodas();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de certificaciones obtenido correctamente");
        respuesta.put("total", certificaciones.size());
        respuesta.put("certificaciones", certificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // Busca una certificación por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Integer id) {

        CertificacionResponseDTO certificacion = service.buscarPorId(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificación encontrada");
        respuesta.put("certificacion", certificacion);

        return ResponseEntity.ok(respuesta);
    }

    // Busca una certificación usando el ID del pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Map<String, Object>> buscarPorPedido(@PathVariable Integer pedidoId) {

        CertificacionResponseDTO certificacion = service.buscarPorPedido(pedidoId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificación encontrada para el pedido");
        respuesta.put("certificacion", certificacion);

        return ResponseEntity.ok(respuesta);
    }

    // Busca certificaciones por usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(@PathVariable Integer usuarioId) {

        List<CertificacionResponseDTO> certificaciones = service.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificaciones encontradas para el usuario");
        respuesta.put("total", certificaciones.size());
        respuesta.put("certificaciones", certificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // Busca certificaciones por estado del pedido
    @GetMapping("/estado/{estadoPedido}")
    public ResponseEntity<Map<String, Object>> buscarPorEstadoPedido(@PathVariable String estadoPedido) {

        List<CertificacionResponseDTO> certificaciones = service.buscarPorEstadoPedido(estadoPedido);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificaciones encontradas por estado de pedido");
        respuesta.put("total", certificaciones.size());
        respuesta.put("certificaciones", certificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // Busca certificaciones filtrando si tienen o no reseña
    @GetMapping("/resena/{tieneResena}")
    public ResponseEntity<Map<String, Object>> buscarPorTieneResena(@PathVariable Boolean tieneResena) {

        List<CertificacionResponseDTO> certificaciones = service.buscarPorTieneResena(tieneResena);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificaciones filtradas por reseña");
        respuesta.put("total", certificaciones.size());
        respuesta.put("certificaciones", certificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // Elimina una certificación por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {

        service.eliminar(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificación eliminada correctamente");
        respuesta.put("idEliminado", id);

        return ResponseEntity.ok(respuesta);
    }

}
