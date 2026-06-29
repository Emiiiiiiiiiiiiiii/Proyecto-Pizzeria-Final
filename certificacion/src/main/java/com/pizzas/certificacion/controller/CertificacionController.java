package com.pizzas.certificacion.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.pizzas.certificacion.dto.CertificacionRequestDTO;
import com.pizzas.certificacion.dto.CertificacionResponseDTO;
import com.pizzas.certificacion.service.CertificacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controlador REST para gestionar certificaciones de pedidos
@RestController
@RequestMapping("/certificaciones")
@Tag(name = "Certificaciones", description = "Endpoints para generar, consultar, filtrar y eliminar certificaciones de pedidos")
public class CertificacionController {

     // Service con la lógica de certificación
    private final CertificacionService service;

    // Constructor para inyectar el service
    public CertificacionController(CertificacionService service) {
        this.service = service;
    }

    // POST http://localhost:8089/certificaciones/generar
    @Operation(
            summary = "Generar certificación",
            description = "Genera una certificación a partir de un pedido. Usa datos del microservicio pedidos, reparto y reseñas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Certificación generada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o pedido no certificable"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe una certificación para ese pedido"),
            @ApiResponse(responseCode = "502", description = "Error al comunicarse con otro microservicio"),
            @ApiResponse(responseCode = "503", description = "Microservicio externo no disponible")
    })
    @PostMapping("/generar")
    public ResponseEntity<Map<String, Object>> generarCertificacion(
            @Valid @RequestBody CertificacionRequestDTO request) {

        CertificacionResponseDTO certificacion = service.generarCertificacion(request);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificación generada correctamente");
        respuesta.put("certificacion", certificacion);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // GET http://localhost:8089/certificaciones
    @Operation(
            summary = "Listar certificaciones",
            description = "Obtiene todas las certificaciones registradas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de certificaciones obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTodas() {

        List<CertificacionResponseDTO> certificaciones = service.listarTodas();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de certificaciones obtenido correctamente");
        respuesta.put("total", certificaciones.size());
        respuesta.put("certificaciones", certificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8089/certificaciones/{id}
    @Operation(
            summary = "Buscar certificación por ID",
            description = "Obtiene una certificación específica usando su ID interno."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificación encontrada"),
            @ApiResponse(responseCode = "404", description = "Certificación no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(
            @Parameter(description = "ID de la certificación", example = "1")
            @PathVariable Integer id) {

        CertificacionResponseDTO certificacion = service.buscarPorId(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificación encontrada");
        respuesta.put("certificacion", certificacion);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8089/certificaciones/pedido/{pedidoId}
    @Operation(
            summary = "Buscar certificación por pedido",
            description = "Obtiene la certificación asociada a un pedido específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificación encontrada para el pedido"),
            @ApiResponse(responseCode = "404", description = "No existe certificación para ese pedido")
    })
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Map<String, Object>> buscarPorPedido(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Integer pedidoId) {

        CertificacionResponseDTO certificacion = service.buscarPorPedido(pedidoId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificación encontrada para el pedido");
        respuesta.put("certificacion", certificacion);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8089/certificaciones/usuario/{usuarioId}
    @Operation(
            summary = "Buscar certificaciones por usuario",
            description = "Lista todas las certificaciones asociadas a un usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificaciones encontradas para el usuario"),
            @ApiResponse(responseCode = "404", description = "No existen certificaciones para ese usuario")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer usuarioId) {

        List<CertificacionResponseDTO> certificaciones = service.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificaciones encontradas para el usuario");
        respuesta.put("total", certificaciones.size());
        respuesta.put("certificaciones", certificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8089/certificaciones/estado/{estadoPedido}
    @Operation(
            summary = "Buscar certificaciones por estado del pedido",
            description = "Lista certificaciones filtradas por el estado del pedido, por ejemplo PAGADO, ENTREGADO o CANCELADO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificaciones encontradas por estado de pedido"),
            @ApiResponse(responseCode = "400", description = "Estado de pedido inválido"),
            @ApiResponse(responseCode = "404", description = "No existen certificaciones con ese estado")
    })
    @GetMapping("/estado/{estadoPedido}")
    public ResponseEntity<Map<String, Object>> buscarPorEstadoPedido(
            @Parameter(description = "Estado del pedido", example = "ENTREGADO")
            @PathVariable String estadoPedido) {

        List<CertificacionResponseDTO> certificaciones = service.buscarPorEstadoPedido(estadoPedido);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificaciones encontradas por estado de pedido");
        respuesta.put("total", certificaciones.size());
        respuesta.put("certificaciones", certificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8089/certificaciones/resena/{tieneResena}
    @Operation(
            summary = "Buscar certificaciones por existencia de reseña",
            description = "Lista certificaciones filtrando si el pedido tiene o no tiene reseña registrada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificaciones filtradas por reseña"),
            @ApiResponse(responseCode = "404", description = "No existen certificaciones con ese filtro")
    })
    @GetMapping("/resena/{tieneResena}")
    public ResponseEntity<Map<String, Object>> buscarPorTieneResena(
            @Parameter(description = "Indica si la certificación tiene reseña", example = "true")
            @PathVariable Boolean tieneResena) {

        List<CertificacionResponseDTO> certificaciones = service.buscarPorTieneResena(tieneResena);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificaciones filtradas por reseña");
        respuesta.put("total", certificaciones.size());
        respuesta.put("certificaciones", certificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8089/certificaciones/{id}
    @Operation(
            summary = "Eliminar certificación",
            description = "Elimina una certificación usando su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificación eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Certificación no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(
            @Parameter(description = "ID de la certificación", example = "1")
            @PathVariable Integer id) {

        service.eliminar(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Certificación eliminada correctamente");
        respuesta.put("idEliminado", id);

        return ResponseEntity.ok(respuesta);
    }

}
