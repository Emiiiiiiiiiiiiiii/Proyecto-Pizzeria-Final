package com.pizzas.resenas.controller;

import com.pizzas.resenas.dto.ResenaRequestDTO;
import com.pizzas.resenas.dto.ResenaResponseDTO;
import com.pizzas.resenas.dto.ResenaUpdateDTO;
import com.pizzas.resenas.service.ResenaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resenas")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    // 1. GUARDAR / CREAR
    @PostMapping("/agregar")
    public ResponseEntity<ResenaResponseDTO> agregar(@Valid @RequestBody ResenaRequestDTO request) {
        // Devolvemos 201 Created porque se creó exitosamente un nuevo recurso
        return new ResponseEntity<>(resenaService.guardarResena(request), HttpStatus.CREATED);
    }

    // 2. LISTAR TODAS
    @GetMapping
    public ResponseEntity<List<ResenaResponseDTO>> listar() {
        return ResponseEntity.ok(resenaService.listarTodas());
    }

    // 3. BUSCAR POR USUARIO
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ResenaResponseDTO>> buscarPorUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(resenaService.buscarPorUsuario(usuarioId));
    }

    // 4. BUSCAR POR PRODUCTO (CATÁLOGO)
    @GetMapping("/producto/{catalogoId}")
    public ResponseEntity<List<ResenaResponseDTO>> buscarPorCatalogo(@PathVariable Integer catalogoId) {
        return ResponseEntity.ok(resenaService.buscarPorCatalogo(catalogoId));
    }

    // 5. BUSCAR POR PEDIDO
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<ResenaResponseDTO>> buscarPorPedido(@PathVariable Integer pedidoId) {
        return ResponseEntity.ok(resenaService.buscarPorPedido(pedidoId));
    }

    // 6. ACTUALIZAR
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ResenaResponseDTO> actualizar(
            @PathVariable Integer id, 
            @Valid @RequestBody ResenaUpdateDTO request) { // <--- CAMBIO AQUÍ: usamos el nuevo DTO
        
        return ResponseEntity.ok(resenaService.actualizarResena(id, request));
    }

    // 7. BORRAR
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Integer id) {
        resenaService.eliminarResena(id);
        return ResponseEntity.ok("La reseña con ID " + id + " ha sido eliminada exitosamente.");
    }
}