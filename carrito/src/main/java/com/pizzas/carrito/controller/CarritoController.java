package com.pizzas.carrito.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

// DTOs usados para recibir datos y mostrar el carrito detallado
import com.pizzas.carrito.dto.CarritoDTO;
import com.pizzas.carrito.dto.CarritoDetalleDTO;

// Modelo Carrito
import com.pizzas.carrito.model.Carrito;

// Service donde está la lógica de negocio
import com.pizzas.carrito.service.CarritoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/carrito")
public class CarritoController {
    // Service encargado de la lógica del carrito
    private final CarritoService service;

    // Constructor para inyectar el service
    public CarritoController(CarritoService service) {
        this.service = service;
    }

    // Agrega un producto al carrito validando usuario y producto
    @PostMapping("/agregar")
    public ResponseEntity<Map<String, Object>> agregar(@Valid @RequestBody CarritoDTO dto) {

        Carrito guardado = service.agregarAlCarrito(dto);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Producto agregado al carrito correctamente");
        respuesta.put("item", guardado);

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    // Agrega varios productos al carrito en una sola petición
    @PostMapping("/agregar-lista")
    public ResponseEntity<Map<String, Object>> agregarLista(@RequestBody List<@Valid CarritoDTO> lista) {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        if (lista == null || lista.isEmpty()) {
            respuesta.put("mensaje", "La lista de productos no puede estar vacía");
            return ResponseEntity.badRequest().body(respuesta);
        }

        List<Carrito> productosProcesados = new ArrayList<>();

        for (CarritoDTO dto : lista) {
            productosProcesados.add(service.agregarAlCarrito(dto));
        }

        respuesta.put("mensaje", "Productos agregados al carrito correctamente");
        respuesta.put("totalProcesados", productosProcesados.size());
        respuesta.put("items", productosProcesados);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Lista el carrito de un usuario como lista directa para no romper pedido
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CarritoDetalleDTO>> listarPorUsuario(@PathVariable Integer usuarioId) {

        List<CarritoDetalleDTO> items = service.listarDetalladoPorUsuario(usuarioId);

        return ResponseEntity.ok(items);
    }

    // Lista el carrito de un usuario con mensaje para pruebas en Postman
    @GetMapping("/usuario/{usuarioId}/respuesta")
    public ResponseEntity<Map<String, Object>> listarPorUsuarioConMensaje(@PathVariable Integer usuarioId) {

        List<CarritoDetalleDTO> items = service.listarDetalladoPorUsuario(usuarioId);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Carrito obtenido correctamente");
        respuesta.put("total", items.size());
        respuesta.put("items", items);

        return ResponseEntity.ok(respuesta);
    }

    // Lista todos los items existentes en la tabla carrito
    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> listarTodos() {

        List<Carrito> carritos = service.listarTodos();

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Items del carrito obtenidos correctamente");
        respuesta.put("total", carritos.size());
        respuesta.put("items", carritos);

        return ResponseEntity.ok(respuesta);
    }

    // Busca un item específico del carrito por su ID
    @GetMapping("/item/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Integer id) {

        Carrito item = service.buscarPorId(id);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Item del carrito encontrado correctamente");
        respuesta.put("item", item);

        return ResponseEntity.ok(respuesta);
    }

    // Actualiza solo la cantidad de un item del carrito
    @PutMapping("/item/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @RequestBody CarritoDTO dto) {

        Carrito actualizado = service.actualizarCantidad(id, dto.getCantidad());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Cantidad actualizada correctamente");
        respuesta.put("item", actualizado);

        return ResponseEntity.ok(respuesta);
    }

    // Elimina una fila/item específico del carrito
    @DeleteMapping("/item/{id}")
    public ResponseEntity<Map<String, String>> eliminarItem(@PathVariable Integer id) {

        service.eliminarDelCarrito(id);

        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Item eliminado del carrito correctamente");

        return ResponseEntity.ok(respuesta);
    }

    // Vacía completamente el carrito de un usuario
    @DeleteMapping("/usuario/{usuarioId}/vaciar")
    public ResponseEntity<Map<String, String>> vaciarCarrito(@PathVariable Integer usuarioId) {

        service.vaciarCarritoPorUsuario(usuarioId);

        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Carrito vaciado correctamente");

        return ResponseEntity.ok(respuesta);
    }

}
