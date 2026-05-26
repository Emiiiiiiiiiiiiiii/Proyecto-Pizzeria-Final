package com.servicio.reparto.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.reparto.dto.RepartoDTO;
import com.servicio.reparto.model.Reparto;
import com.servicio.reparto.service.RepartoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/reparto")
@RequiredArgsConstructor
public class RepartoController {

    private final RepartoService repartoService;

    
    @PostMapping("/generar")
    public ResponseEntity<Reparto> generarReparto(@RequestBody @Valid RepartoDTO dto) {
        Reparto creado = repartoService.generarReparto(dto);
        if (creado == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(201).body(creado);
    }

    
    @GetMapping("/todos")
    public ResponseEntity<List<Reparto>> listarTodos() {
        List<Reparto> lista = repartoService.buscarTodosLosDespachos();
        return ResponseEntity.ok(lista);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Reparto> buscarPorId(@PathVariable Long id) {
        return repartoService.buscarDespachoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Reparto> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String nuevoEstado = body.get("estadoReparto");
        
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Reparto actualizado = repartoService.actualizarEstadoReparto(id, nuevoEstado);
        
        return ResponseEntity.ok(actualizado);
    }
}
