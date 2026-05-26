package com.pizzas.certificacion.controller;

import com.pizzas.certificacion.dto.CertificacionRequestDTO;
import com.pizzas.certificacion.dto.CertificacionResponseDTO;
import com.pizzas.certificacion.model.Certificacion;
import com.pizzas.certificacion.service.CertificacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/certificaciones")

public class CertificacionController {
    @Autowired
    private CertificacionService service;

    // 1. GENERAR
    @PostMapping("/generar")
    public ResponseEntity<CertificacionResponseDTO> crear(@Valid @RequestBody CertificacionRequestDTO request) {
        return new ResponseEntity<>(service.generarCertificacion(request), HttpStatus.CREATED);
    }

    // 2. LISTAR
    @GetMapping
    public ResponseEntity<List<CertificacionResponseDTO>> listarTodo() {
        return ResponseEntity.ok(service.listarTodas());
    }

    // 3. BUSCAR ID
    @GetMapping("/{id}")
    public ResponseEntity<CertificacionResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // 4. BUSCAR USUARIO
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CertificacionResponseDTO>> buscarPorUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuarioId));
    }

    // 6. ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
