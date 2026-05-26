package com.pizzas.carrito.controller;

import java.util.HashMap;
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

import com.pizzas.carrito.dto.CarritoDTO;
import com.pizzas.carrito.dto.CarritoDetalleDTO;
import com.pizzas.carrito.model.Carrito;
import com.pizzas.carrito.service.CarritoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/carrito")

public class CarritoController {
    private final CarritoService service;

    public CarritoController(CarritoService service) {
        this.service = service;
    }

    @PostMapping("/agregar")
    public ResponseEntity<Map<String, Object>> agregar(@Valid @RequestBody CarritoDTO dto) {
        Carrito guardado = service.agregarAlCarrito(dto);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Producto agregado al carrito con éxito");
        respuesta.put("item", guardado);
        
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    @PostMapping("/agregar-lista")
    public ResponseEntity<Map<String, String>> agregarLista(@Valid @RequestBody List<CarritoDTO> lista) {
        for (CarritoDTO dto : lista) {
            service.agregarAlCarrito(dto);
        }
        
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Todas las pizzas se procesaron correctamente.");
        
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CarritoDetalleDTO>> listar(@PathVariable Integer usuarioId) {
        List<CarritoDetalleDTO> items = service.listarDetalladoPorUsuario(usuarioId);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
        @PathVariable Integer id,
        @RequestBody CarritoDTO dto) {

        Carrito actualizado = service.actualizarCantidad(id, dto.getCantidad());
    
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Cantidad actualizada correctamente");
        respuesta.put("item", actualizado);
    
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Integer id) {
        service.eliminarDelCarrito(id);
        
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Ítem eliminado del carrito con éxito");
        
        return ResponseEntity.ok(respuesta);
    }

}
