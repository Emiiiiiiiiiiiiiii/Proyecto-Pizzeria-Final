package com.pizzas.inventario.controller;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzas.inventario.dto.DescuentoInventarioDTO;
import com.pizzas.inventario.dto.InventarioRequestDTO;
import com.pizzas.inventario.dto.InventarioResponseDTO;
import com.pizzas.inventario.service.InventarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controlador REST para gestionar inventario
@RestController
@RequestMapping("/inventario")
@Tag(name = "Inventario", description = "Endpoints para consultar, agregar, actualizar, eliminar y descontar stock")

public class InventarioController {
    private final InventarioService service;

    // Constructor para inyectar service
    public InventarioController(InventarioService service) {
        this.service = service;
    }

    // GET http://localhost:8084/inventario
    @Operation(
            summary = "Listar inventario",
            description = "Obtiene todos los registros de inventario con su estado de stock."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de inventario obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<InventarioResponseDTO> inventario = service.listar();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de inventario obtenido correctamente");
        respuesta.put("total", inventario.size());
        respuesta.put("inventario", inventario);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8084/inventario/{catalogoId}
    @Operation(
            summary = "Buscar inventario por producto",
            description = "Obtiene el stock de un producto usando el ID del catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe inventario para el producto indicado")
    })
    @GetMapping("/{catalogoId}")
    public ResponseEntity<InventarioResponseDTO> buscar(
            @Parameter(description = "ID del producto en el catálogo", example = "1")
            @PathVariable Integer catalogoId) {

        InventarioResponseDTO inventario = service.buscarPorCatalogoId(catalogoId);

        return ResponseEntity.ok(inventario);
    }

    // GET http://localhost:8084/inventario/stock-bajo/{limite}
    @Operation(
            summary = "Listar productos con stock bajo",
            description = "Lista los productos cuyo stock sea menor al límite indicado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Productos con stock bajo obtenidos correctamente"),
            @ApiResponse(responseCode = "400", description = "Límite inválido")
    })
    @GetMapping("/stock-bajo/{limite}")
    public ResponseEntity<Map<String, Object>> listarStockBajo(
            @Parameter(description = "Cantidad límite para considerar stock bajo", example = "20")
            @PathVariable Integer limite) {

        List<InventarioResponseDTO> inventario = service.listarStockBajo(limite);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de productos con stock bajo obtenido correctamente");
        respuesta.put("limite", limite);
        respuesta.put("total", inventario.size());
        respuesta.put("inventario", inventario);

        return ResponseEntity.ok(respuesta);
    }

    // POST http://localhost:8084/inventario/agregar
    @Operation(
            summary = "Agregar inventario",
            description = "Agrega stock para un producto existente del catálogo. El producto debe existir previamente en el microservicio catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inventario agregado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en catálogo"),
            @ApiResponse(responseCode = "409", description = "Ya existe inventario para ese producto"),
            @ApiResponse(responseCode = "503", description = "Microservicio catálogo no disponible")
    })
    @PostMapping("/agregar")
    public ResponseEntity<Map<String, Object>> guardar(@Valid @RequestBody InventarioRequestDTO dto) {
        InventarioResponseDTO guardado = service.guardar(dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Inventario agregado correctamente");
        respuesta.put("inventario", guardado);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // PUT http://localhost:8084/inventario/{catalogoId}
    @Operation(
            summary = "Actualizar inventario",
            description = "Actualiza la cantidad disponible de un producto usando su ID de catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o catalogoId inconsistente"),
            @ApiResponse(responseCode = "404", description = "Inventario o producto no encontrado"),
            @ApiResponse(responseCode = "503", description = "Microservicio catálogo no disponible")
    })
    @PutMapping("/{catalogoId}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @Parameter(description = "ID del producto en el catálogo", example = "1")
            @PathVariable Integer catalogoId,
            @Valid @RequestBody InventarioRequestDTO dto) {

        InventarioResponseDTO actualizado = service.actualizar(catalogoId, dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Inventario actualizado correctamente");
        respuesta.put("inventario", actualizado);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8084/inventario/{catalogoId}
    @Operation(
            summary = "Eliminar inventario",
            description = "Elimina el inventario asociado a un producto usando su ID de catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    @DeleteMapping("/{catalogoId}")
    public ResponseEntity<Map<String, Object>> eliminar(
            @Parameter(description = "ID del producto en el catálogo", example = "1")
            @PathVariable Integer catalogoId) {

        service.eliminar(catalogoId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Inventario eliminado correctamente");
        respuesta.put("catalogoIdEliminado", catalogoId);

        return ResponseEntity.ok(respuesta);
    }

    // POST http://localhost:8084/inventario/descontar
    @Operation(
            summary = "Descontar stock",
            description = "Descuenta stock de uno o varios productos. Esta ruta es usada por el microservicio pedido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock descontado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Lista vacía, datos inválidos o stock insuficiente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en inventario")
    })
    @PostMapping("/descontar")
    public ResponseEntity<Map<String, Object>> descontar(
            @Valid @RequestBody List<DescuentoInventarioDTO> items) {

        service.descontarStock(items);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Stock descontado exitosamente");
        respuesta.put("totalItemsDescontados", items.size());

        return ResponseEntity.ok(respuesta);
    }
}
