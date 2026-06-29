package com.pago.service.controller;


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

import com.pago.service.dto.PagosDTO;
import com.pago.service.model.Pagos;
import com.pago.service.pagosService.PagosService;

// Controlador REST del microservicio pagos
@RestController
@RequestMapping("/pagos")
public class PagosController {

    // Service con la lógica de pagos
    private final PagosService pagosService;

    // Constructor para inyectar service
    public PagosController(PagosService pagosService) {
        this.pagosService = pagosService;
    }

    // Procesa un pago desde pedido
    @PostMapping("/procesar")
    public ResponseEntity<Map<String, Object>> procesar(@Valid @RequestBody PagosDTO pagosDTO) {
        Map<String, Object> resultado = pagosService.generarPago(pagosDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    // Lista todos los pagos
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<Pagos> pagos = pagosService.listarTodos();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de pagos obtenido correctamente");
        respuesta.put("total", pagos.size());
        respuesta.put("pagos", pagos);

        return ResponseEntity.ok(respuesta);
    }

    // Busca pago por ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Integer id) {
        Pagos pago = pagosService.buscarPorId(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pago encontrado");
        respuesta.put("pago", pago);

        return ResponseEntity.ok(respuesta);
    }

    // Busca pago por pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Map<String, Object>> buscarPorPedido(@PathVariable Integer pedidoId) {
        Pagos pago = pagosService.buscarPorPedido(pedidoId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pago encontrado para el pedido");
        respuesta.put("pago", pago);

        return ResponseEntity.ok(respuesta);
    }

    // Busca pagos por usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(@PathVariable Integer usuarioId) {
        List<Pagos> pagos = pagosService.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pagos encontrados para el usuario");
        respuesta.put("total", pagos.size());
        respuesta.put("pagos", pagos);

        return ResponseEntity.ok(respuesta);
    }

    // Busca pagos por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> buscarPorEstado(@PathVariable String estado) {
        List<Pagos> pagos = pagosService.buscarPorEstado(estado);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pagos encontrados por estado");
        respuesta.put("total", pagos.size());
        respuesta.put("pagos", pagos);

        return ResponseEntity.ok(respuesta);
    }

    // Elimina un pago por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {
        pagosService.eliminarPorId(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Registro de pago eliminado correctamente");
        respuesta.put("idEliminado", id);

        return ResponseEntity.ok(respuesta);
    }

}