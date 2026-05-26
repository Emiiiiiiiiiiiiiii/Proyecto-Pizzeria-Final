package com.pago.service.controller;


import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pago.service.dto.PagosDTO;
import com.pago.service.model.Pagos;
import com.pago.service.pagosService.PagosService;

import jakarta.validation.Valid;





@RestController
@RequestMapping("/pagos")
public class PagosController {

    private final PagosService pagosService;

    public PagosController(PagosService pagosService) {
        this.pagosService = pagosService;
    }

    @GetMapping
    public ResponseEntity<List<Pagos>> listar() {
        return ResponseEntity.ok(pagosService.listarTodos());
    }

    @PostMapping("/procesar")
    public ResponseEntity<Map<String, Object>> procesar(@Valid @RequestBody PagosDTO pagosDTO) {
        Map<String, Object> resultado = pagosService.generarPago(pagosDTO);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pagos> buscarPorId(@PathVariable Integer id) {
        Pagos pago = pagosService.buscarPorId(id);
        return ResponseEntity.ok(pago);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Integer id) {
        pagosService.eliminarPorId(id);
        Map<String, String> respuesta = new java.util.HashMap<>();
        respuesta.put("mensaje", "Registro de pago con ID " + id + " eliminado correctamente.");
        return ResponseEntity.ok(respuesta);
    }

}