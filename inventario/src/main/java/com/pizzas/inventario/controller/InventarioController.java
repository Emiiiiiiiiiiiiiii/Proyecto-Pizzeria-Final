package com.pizzas.inventario.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.pizzas.inventario.dto.InventarioRequestDTO;
import com.pizzas.inventario.dto.InventarioResponseDTO;
import com.pizzas.inventario.service.InventarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/inventario")

public class InventarioController {
    @Autowired
    private InventarioService service;

    @GetMapping
    public ResponseEntity<List<InventarioResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{catalogoId}")
    public ResponseEntity<InventarioResponseDTO> buscar(@PathVariable Integer catalogoId) {
        return ResponseEntity.ok(service.buscarPorCatalogoId(catalogoId));
    }

    @PostMapping("/agregar")
    public ResponseEntity<InventarioResponseDTO> guardar(@Valid @RequestBody InventarioRequestDTO dto) {
        InventarioResponseDTO guardado = service.guardar(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{Id}")
    public ResponseEntity<InventarioResponseDTO> actualizar(@PathVariable Integer Id, @Valid @RequestBody InventarioRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(Id, dto));
    }

    @DeleteMapping("/{catalogoId}")
    public ResponseEntity<String> eliminar(@PathVariable Integer catalogoId) {
        service.eliminar(catalogoId);
        return ResponseEntity.ok("Producto con ID " + catalogoId + " eliminado con éxito del inventario.");
    }
    @PostMapping("/descontar")
    public ResponseEntity<String> descontar(@RequestBody List<InventarioRequestDTO> items) {
        service.descontarStock(items);
        return ResponseEntity.ok("Stock descontado exitosamente");
    }
}
