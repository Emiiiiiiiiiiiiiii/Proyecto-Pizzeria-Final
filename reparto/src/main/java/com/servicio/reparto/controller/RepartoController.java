package com.servicio.reparto.controller;

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

import com.servicio.reparto.dto.EstadoRepartoDTO;
import com.servicio.reparto.dto.RepartoRequestDTO;
import com.servicio.reparto.dto.RepartoResponseDTO;
import com.servicio.reparto.service.RepartoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controlador REST para gestionar repartos
@RestController
@RequestMapping("/reparto")
@Tag(name = "Reparto", description = "Endpoints para generar, consultar, actualizar y eliminar repartos")
public class RepartoController {

    private final RepartoService repartoService;

    // Constructor para inyectar service
    public RepartoController(RepartoService repartoService) {
        this.repartoService = repartoService;
    }

    // POST http://localhost:8087/reparto/generar
    @Operation(
            summary = "Generar reparto",
            description = "Genera un reparto para un pedido existente, asignando dirección de entrega, repartidor y estado inicial."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reparto generado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o pedido inconsistente"),
            @ApiResponse(responseCode = "404", description = "Pedido o usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El pedido ya tiene reparto asignado"),
            @ApiResponse(responseCode = "503", description = "Microservicio externo no disponible")
    })
    @PostMapping("/generar")
    public ResponseEntity<Map<String, Object>> generarReparto(@Valid @RequestBody RepartoRequestDTO dto) {
        RepartoResponseDTO reparto = repartoService.generarReparto(dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reparto generado correctamente");
        respuesta.put("reparto", reparto);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // GET http://localhost:8087/reparto/todos
    @Operation(
            summary = "Listar todos los repartos",
            description = "Lista todos los repartos en formato directo. Esta ruta es usada por el microservicio certificación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repartos obtenidos correctamente")
    })
    @GetMapping("/todos")
    public ResponseEntity<List<RepartoResponseDTO>> listarTodos() {
        List<RepartoResponseDTO> lista = repartoService.listarTodos();
        return ResponseEntity.ok(lista);
    }

    // GET http://localhost:8087/reparto
    @Operation(
            summary = "Listar repartos con respuesta detallada",
            description = "Obtiene todos los repartos con mensaje, total y lista de repartos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de repartos obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTodosRespuesta() {
        List<RepartoResponseDTO> lista = repartoService.listarTodos();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de repartos obtenido correctamente");
        respuesta.put("total", lista.size());
        respuesta.put("repartos", lista);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8087/reparto/{id}
    @Operation(
            summary = "Buscar reparto por ID",
            description = "Obtiene un reparto específico usando su ID interno."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reparto encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Reparto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(
            @Parameter(description = "ID del reparto", example = "1")
            @PathVariable Long id) {

        RepartoResponseDTO reparto = repartoService.buscarPorId(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reparto encontrado");
        respuesta.put("reparto", reparto);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8087/reparto/pedido/{pedidoId}
    @Operation(
            summary = "Buscar reparto por pedido",
            description = "Obtiene el reparto asociado a un pedido específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reparto encontrado para el pedido"),
            @ApiResponse(responseCode = "404", description = "No existe reparto para el pedido indicado")
    })
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Map<String, Object>> buscarPorPedido(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Integer pedidoId) {

        RepartoResponseDTO reparto = repartoService.buscarPorPedido(pedidoId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reparto encontrado para el pedido");
        respuesta.put("reparto", reparto);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8087/reparto/usuario/{usuarioId}
    @Operation(
            summary = "Buscar repartos por usuario",
            description = "Lista todos los repartos asociados a un usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repartos encontrados para el usuario"),
            @ApiResponse(responseCode = "404", description = "No existen repartos para ese usuario")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer usuarioId) {

        List<RepartoResponseDTO> repartos = repartoService.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Repartos encontrados para el usuario");
        respuesta.put("total", repartos.size());
        respuesta.put("repartos", repartos);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8087/reparto/estado/{estado}
    @Operation(
            summary = "Buscar repartos por estado",
            description = "Lista repartos filtrados por estado, por ejemplo PREPARANDO, EN_CAMINO, ENTREGADO, CANCELADO o RETRASADO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repartos encontrados por estado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "No existen repartos con ese estado")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> buscarPorEstado(
            @Parameter(description = "Estado del reparto", example = "EN_CAMINO")
            @PathVariable String estado) {

        List<RepartoResponseDTO> repartos = repartoService.buscarPorEstado(estado);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Repartos encontrados por estado");
        respuesta.put("total", repartos.size());
        respuesta.put("repartos", repartos);

        return ResponseEntity.ok(respuesta);
    }

    // PUT http://localhost:8087/reparto/{id}/estado
    @Operation(
            summary = "Actualizar estado del reparto",
            description = "Actualiza el estado de un reparto existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de reparto actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Reparto no encontrado")
    })
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> actualizarEstado(
            @Parameter(description = "ID del reparto", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EstadoRepartoDTO body) {

        RepartoResponseDTO reparto = repartoService.actualizarEstadoReparto(id, body.getEstadoReparto());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Estado de reparto actualizado correctamente");
        respuesta.put("reparto", reparto);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8087/reparto/{id}
    @Operation(
            summary = "Eliminar reparto",
            description = "Elimina un reparto según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reparto eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Reparto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(
            @Parameter(description = "ID del reparto", example = "1")
            @PathVariable Long id) {

        repartoService.eliminar(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reparto eliminado correctamente");
        respuesta.put("idEliminado", id);

        return ResponseEntity.ok(respuesta);
    }
}
