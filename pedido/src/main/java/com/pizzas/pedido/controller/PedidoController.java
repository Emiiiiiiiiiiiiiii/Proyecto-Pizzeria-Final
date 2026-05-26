package com.pizzas.pedido.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pizzas.pedido.dto.PedidoRequestDTO;
import com.pizzas.pedido.dto.PedidoResponseDTO;
import com.pizzas.pedido.model.Pedido;
import com.pizzas.pedido.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")

public class PedidoController {
    @Autowired
    private PedidoService pedidoService;

    // 1. GUARDAR / CREAR
    @PostMapping("/guardar")
    public ResponseEntity<PedidoResponseDTO> crear(@Valid @RequestBody PedidoRequestDTO request) {
        return new ResponseEntity<>(pedidoService.crearPedido(request), HttpStatus.CREATED);
    }

    // 2. LISTAR TODOS
    @GetMapping
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(pedidoService.listarPedidos());
    }

    // 3. BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    // 4. BUSCAR POR USUARIO
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Pedido>> buscarPorUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(pedidoService.buscarPorUsuario(usuarioId));
    }

    // 5. ACTUALIZAR ESTADO
    @PutMapping("/{id}/estado")
    public ResponseEntity<String> actualizar(@PathVariable Integer id, @RequestParam String nuevoEstado) {
        pedidoService.actualizarPedido(id, nuevoEstado);
        return ResponseEntity.ok("Estado del pedido " + id + " actualizado a: " + nuevoEstado);
    }

    // 6. BORRAR
    @DeleteMapping("/{id}")
public ResponseEntity<String> eliminar(@PathVariable Integer id) {
    pedidoService.eliminarPedido(id);
    
    return ResponseEntity.ok("El pedido con ID " + id + " ha sido eliminado exitosamente.");
}

}
