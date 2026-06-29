package com.pizzas.resenas.controller;

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

import com.pizzas.resenas.dto.ResenaRequestDTO;
import com.pizzas.resenas.dto.ResenaResponseDTO;
import com.pizzas.resenas.dto.ResenaUpdateDTO;
import com.pizzas.resenas.service.ResenaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controlador REST para gestionar reseñas
@RestController
@RequestMapping("/resenas")
@Tag(name = "Reseñas", description = "Endpoints para crear, consultar, actualizar y eliminar reseñas")
public class ResenaController {

    private final ResenaService service;

    // Constructor para inyectar service
    public ResenaController(ResenaService service) {
        this.service = service;
    }

    // POST http://localhost:8088/resenas/guardar
    @Operation(
            summary = "Crear reseña",
            description = "Registra una reseña para un pedido existente. Valida que el pedido exista y que no tenga una reseña previa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reseña registrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Pedido o usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El pedido ya tiene una reseña registrada"),
            @ApiResponse(responseCode = "503", description = "Microservicio externo no disponible")
    })
    @PostMapping("/guardar")
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody ResenaRequestDTO dto) {
        ResenaResponseDTO resena = service.crearResena(dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña registrada correctamente");
        respuesta.put("resena", resena);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // POST http://localhost:8088/resenas/crear
    @Operation(
            summary = "Crear reseña alternativa",
            description = "Ruta alternativa para registrar una reseña. Cumple la misma función que /resenas/guardar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reseña registrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Pedido o usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El pedido ya tiene una reseña registrada"),
            @ApiResponse(responseCode = "503", description = "Microservicio externo no disponible")
    })
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearAlternativo(@Valid @RequestBody ResenaRequestDTO dto) {
        ResenaResponseDTO resena = service.crearResena(dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña registrada correctamente");
        respuesta.put("resena", resena);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // GET http://localhost:8088/resenas
    @Operation(
            summary = "Listar reseñas",
            description = "Obtiene todas las reseñas registradas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de reseñas obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTodas() {
        List<ResenaResponseDTO> resenas = service.listarTodas();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de reseñas obtenido correctamente");
        respuesta.put("total", resenas.size());
        respuesta.put("resenas", resenas);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8088/resenas/{id}
    @Operation(
            summary = "Buscar reseña por ID",
            description = "Obtiene una reseña específica usando su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña encontrada correctamente"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(
            @Parameter(description = "ID de la reseña", example = "1")
            @PathVariable Integer id) {

        ResenaResponseDTO resena = service.buscarPorId(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña encontrada");
        respuesta.put("resena", resena);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8088/resenas/pedido/{pedidoId}
    @Operation(
            summary = "Buscar reseñas por pedido",
            description = "Obtiene las reseñas asociadas a un pedido. Esta ruta es usada por el microservicio certificación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseñas obtenidas correctamente")
    })
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<ResenaResponseDTO>> buscarPorPedido(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Integer pedidoId) {

        List<ResenaResponseDTO> resenas = service.buscarPorPedido(pedidoId);

        return ResponseEntity.ok(resenas);
    }

    // GET http://localhost:8088/resenas/pedido/{pedidoId}/respuesta
    @Operation(
            summary = "Buscar reseñas por pedido con respuesta detallada",
            description = "Obtiene reseñas por pedido con mensaje, total y lista de reseñas. Ruta pensada para pruebas en Swagger o Postman."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseñas encontradas para el pedido")
    })
    @GetMapping("/pedido/{pedidoId}/respuesta")
    public ResponseEntity<Map<String, Object>> buscarPorPedidoRespuesta(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Integer pedidoId) {

        List<ResenaResponseDTO> resenas = service.buscarPorPedido(pedidoId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseñas encontradas para el pedido");
        respuesta.put("total", resenas.size());
        respuesta.put("resenas", resenas);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8088/resenas/usuario/{usuarioId}
    @Operation(
            summary = "Buscar reseñas por usuario",
            description = "Lista todas las reseñas realizadas por un usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseñas encontradas para el usuario"),
            @ApiResponse(responseCode = "404", description = "No existen reseñas para ese usuario")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer usuarioId) {

        List<ResenaResponseDTO> resenas = service.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseñas encontradas para el usuario");
        respuesta.put("total", resenas.size());
        respuesta.put("resenas", resenas);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8088/resenas/estrellas/{estrellas}
    @Operation(
            summary = "Buscar reseñas por estrellas",
            description = "Lista reseñas filtradas por cantidad de estrellas, desde 1 hasta 5."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseñas encontradas por estrellas"),
            @ApiResponse(responseCode = "400", description = "Cantidad de estrellas inválida"),
            @ApiResponse(responseCode = "404", description = "No existen reseñas con esa cantidad de estrellas")
    })
    @GetMapping("/estrellas/{estrellas}")
    public ResponseEntity<Map<String, Object>> buscarPorEstrellas(
            @Parameter(description = "Cantidad de estrellas", example = "5")
            @PathVariable Integer estrellas) {

        List<ResenaResponseDTO> resenas = service.buscarPorEstrellas(estrellas);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseñas encontradas por estrellas");
        respuesta.put("total", resenas.size());
        respuesta.put("resenas", resenas);

        return ResponseEntity.ok(respuesta);
    }

    // PUT http://localhost:8088/resenas/{id}
    @Operation(
            summary = "Actualizar reseña",
            description = "Actualiza el comentario y la cantidad de estrellas de una reseña existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @Parameter(description = "ID de la reseña", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody ResenaUpdateDTO dto) {

        ResenaResponseDTO resena = service.actualizar(id, dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña actualizada correctamente");
        respuesta.put("resena", resena);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8088/resenas/{id}
    @Operation(
            summary = "Eliminar reseña",
            description = "Elimina una reseña según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(
            @Parameter(description = "ID de la reseña", example = "1")
            @PathVariable Integer id) {

        service.eliminar(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reseña eliminada correctamente");
        respuesta.put("idEliminado", id);

        return ResponseEntity.ok(respuesta);
    }
}