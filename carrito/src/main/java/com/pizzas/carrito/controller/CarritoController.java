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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Controlador REST para gestionar el carrito
@RestController
@RequestMapping("/carrito")
@Tag(name = "Carrito", description = "Endpoints para agregar, listar, actualizar y eliminar productos del carrito")
public class CarritoController {
    // Service encargado de la lógica del carrito
    private final CarritoService service;

    // Constructor para inyectar el service
    public CarritoController(CarritoService service) {
        this.service = service;
    }

    // POST http://localhost:8082/carrito/agregar
    @Operation(
            summary = "Agregar producto al carrito",
            description = "Agrega un producto al carrito de un usuario. Si el producto ya existe en el carrito, aumenta la cantidad."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto agregado al carrito correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o cantidad incorrecta"),
            @ApiResponse(responseCode = "404", description = "Usuario o producto no encontrado"),
            @ApiResponse(responseCode = "503", description = "Microservicio externo no disponible")
    })
    @PostMapping("/agregar")
    public ResponseEntity<Map<String, Object>> agregar(@Valid @RequestBody CarritoDTO dto) {

        Carrito guardado = service.agregarAlCarrito(dto);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Producto agregado al carrito correctamente");
        respuesta.put("item", guardado);

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    // POST http://localhost:8082/carrito/agregar-lista
    @Operation(
            summary = "Agregar lista de productos al carrito",
            description = "Agrega varios productos al carrito en una sola petición."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Productos agregados correctamente"),
            @ApiResponse(responseCode = "400", description = "Lista vacía o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario o producto no encontrado")
    })
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

    // GET http://localhost:8082/carrito/usuario/{usuarioId}
    @Operation(
            summary = "Listar carrito de usuario",
            description = "Lista el carrito detallado de un usuario. Esta ruta devuelve una lista directa porque la consume el microservicio pedido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario inexistente o carrito vacío")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CarritoDetalleDTO>> listarPorUsuario(
            @Parameter(description = "ID del usuario dueño del carrito", example = "1")
            @PathVariable Integer usuarioId) {

        List<CarritoDetalleDTO> items = service.listarDetalladoPorUsuario(usuarioId);

        return ResponseEntity.ok(items);
    }

    // GET http://localhost:8082/carrito/usuario/{usuarioId}/respuesta
    @Operation(
            summary = "Listar carrito de usuario con respuesta detallada",
            description = "Lista el carrito de un usuario con mensaje, total e items. Ruta pensada para pruebas en Postman o Swagger."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario inexistente o carrito vacío")
    })
    @GetMapping("/usuario/{usuarioId}/respuesta")
    public ResponseEntity<Map<String, Object>> listarPorUsuarioConMensaje(
            @Parameter(description = "ID del usuario dueño del carrito", example = "1")
            @PathVariable Integer usuarioId) {

        List<CarritoDetalleDTO> items = service.listarDetalladoPorUsuario(usuarioId);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Carrito obtenido correctamente");
        respuesta.put("total", items.size());
        respuesta.put("items", items);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8082/carrito/items
    @Operation(
            summary = "Listar todos los items del carrito",
            description = "Obtiene todos los registros existentes en la tabla carrito."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Items del carrito obtenidos correctamente")
    })
    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> listarTodos() {

        List<Carrito> carritos = service.listarTodos();

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Items del carrito obtenidos correctamente");
        respuesta.put("total", carritos.size());
        respuesta.put("items", carritos);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8082/carrito/item/{id}
    @Operation(
            summary = "Buscar item del carrito por ID",
            description = "Obtiene un item específico del carrito según su ID interno."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Item de carrito no encontrado")
    })
    @GetMapping("/item/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(
            @Parameter(description = "ID interno del item del carrito", example = "1")
            @PathVariable Integer id) {

        Carrito item = service.buscarPorId(id);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Item del carrito encontrado correctamente");
        respuesta.put("item", item);

        return ResponseEntity.ok(respuesta);
    }

    // PUT http://localhost:8082/carrito/item/{id}
    @Operation(
            summary = "Actualizar cantidad de un item",
            description = "Actualiza la cantidad de un producto existente dentro del carrito."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida"),
            @ApiResponse(responseCode = "404", description = "Item de carrito no encontrado")
    })
    @PutMapping("/item/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @Parameter(description = "ID interno del item del carrito", example = "1")
            @PathVariable Integer id,
            @RequestBody CarritoDTO dto) {

        Carrito actualizado = service.actualizarCantidad(id, dto.getCantidad());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Cantidad actualizada correctamente");
        respuesta.put("item", actualizado);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8082/carrito/item/{id}
    @Operation(
            summary = "Eliminar item del carrito",
            description = "Elimina un producto específico del carrito según su ID interno."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Item de carrito no encontrado")
    })
    @DeleteMapping("/item/{id}")
    public ResponseEntity<Map<String, String>> eliminarItem(
            @Parameter(description = "ID interno del item del carrito", example = "1")
            @PathVariable Integer id) {

        service.eliminarDelCarrito(id);

        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Item eliminado del carrito correctamente");

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8082/carrito/usuario/{usuarioId}/vaciar
    @Operation(
            summary = "Vaciar carrito de usuario",
            description = "Elimina todos los productos del carrito asociados a un usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito vaciado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario inexistente o carrito vacío")
    })
    @DeleteMapping("/usuario/{usuarioId}/vaciar")
    public ResponseEntity<Map<String, String>> vaciarCarrito(
            @Parameter(description = "ID del usuario dueño del carrito", example = "1")
            @PathVariable Integer usuarioId) {

        service.vaciarCarritoPorUsuario(usuarioId);

        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Carrito vaciado correctamente");

        return ResponseEntity.ok(respuesta);
    }

}
