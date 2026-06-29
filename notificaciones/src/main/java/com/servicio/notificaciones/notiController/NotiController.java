package com.servicio.notificaciones.notiController;

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

import com.servicio.notificaciones.dto.NotiDTO;
import com.servicio.notificaciones.dto.NotiListadoDTO;
import com.servicio.notificaciones.dto.NotiResponseDTO;
import com.servicio.notificaciones.notiService.NotiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controlador REST para gestionar notificaciones
@RestController
@RequestMapping("/notificaciones")
@Tag(name = "Notificaciones", description = "Endpoints para enviar, listar, buscar y eliminar notificaciones")
public class NotiController {

    private final NotiService service;

    // Constructor para inyectar service
    public NotiController(NotiService service) {
        this.service = service;
    }

    // POST http://localhost:8086/notificaciones/enviar
    @Operation(
            summary = "Enviar notificación",
            description = "Registra una notificación como enviada. Puede ser de tipo REGISTRO, PEDIDO, PAGO, REPARTO, CAMBIO_ESTADO_REPARTO, CERTIFICACION o RESENA."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notificación enviada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviar(@Valid @RequestBody NotiDTO dto) {
        NotiResponseDTO notificacion = service.enviar(dto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Notificación enviada correctamente");
        respuesta.put("notificacion", notificacion);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // GET http://localhost:8086/notificaciones
    @Operation(
            summary = "Listar notificaciones",
            description = "Obtiene todas las notificaciones registradas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de notificaciones obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTodas() {
        List<NotiListadoDTO> notificaciones = service.listarTodas();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de notificaciones obtenido correctamente");
        respuesta.put("total", notificaciones.size());
        respuesta.put("notificaciones", notificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8086/notificaciones/todas
    @Operation(
            summary = "Listar todas las notificaciones",
            description = "Ruta alternativa para obtener todas las notificaciones registradas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de notificaciones obtenido correctamente")
    })
    @GetMapping("/todas")
    public ResponseEntity<Map<String, Object>> listarTodasAlternativo() {
        List<NotiListadoDTO> notificaciones = service.listarTodas();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de notificaciones obtenido correctamente");
        respuesta.put("total", notificaciones.size());
        respuesta.put("notificaciones", notificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8086/notificaciones/{id}
    @Operation(
            summary = "Buscar notificación por ID",
            description = "Obtiene una notificación específica usando su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación encontrada"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(
            @Parameter(description = "ID de la notificación", example = "1")
            @PathVariable Integer id) {

        NotiResponseDTO notificacion = service.buscarPorId(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Notificación encontrada");
        respuesta.put("notificacion", notificacion);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8086/notificaciones/usuario/{usuarioId}
    @Operation(
            summary = "Buscar notificaciones por usuario",
            description = "Lista las notificaciones asociadas a un usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificaciones encontradas para el usuario"),
            @ApiResponse(responseCode = "404", description = "No existen notificaciones para ese usuario")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer usuarioId) {

        List<NotiListadoDTO> notificaciones = service.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Notificaciones encontradas para el usuario");
        respuesta.put("total", notificaciones.size());
        respuesta.put("notificaciones", notificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8086/notificaciones/pedido/{pedidoId}
    @Operation(
            summary = "Buscar notificaciones por pedido",
            description = "Lista las notificaciones asociadas a un pedido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificaciones encontradas para el pedido"),
            @ApiResponse(responseCode = "404", description = "No existen notificaciones para ese pedido")
    })
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Map<String, Object>> buscarPorPedido(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Integer pedidoId) {

        List<NotiListadoDTO> notificaciones = service.buscarPorPedido(pedidoId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Notificaciones encontradas para el pedido");
        respuesta.put("total", notificaciones.size());
        respuesta.put("notificaciones", notificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8086/notificaciones/tipo/{tipo}
    @Operation(
            summary = "Buscar notificaciones por tipo",
            description = "Lista notificaciones filtradas por tipo. Ejemplos: REGISTRO, PEDIDO, PAGO, REPARTO, CERTIFICACION o RESENA."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificaciones encontradas por tipo"),
            @ApiResponse(responseCode = "400", description = "Tipo inválido"),
            @ApiResponse(responseCode = "404", description = "No existen notificaciones de ese tipo")
    })
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<Map<String, Object>> buscarPorTipo(
            @Parameter(description = "Tipo de notificación", example = "PAGO")
            @PathVariable String tipo) {

        List<NotiListadoDTO> notificaciones = service.buscarPorTipo(tipo);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Notificaciones encontradas por tipo");
        respuesta.put("total", notificaciones.size());
        respuesta.put("notificaciones", notificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8086/notificaciones/destinatario/{destinatario}
    @Operation(
            summary = "Buscar notificaciones por destinatario",
            description = "Lista notificaciones enviadas a un destinatario específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificaciones encontradas por destinatario"),
            @ApiResponse(responseCode = "400", description = "Destinatario inválido"),
            @ApiResponse(responseCode = "404", description = "No existen notificaciones para ese destinatario")
    })
    @GetMapping("/destinatario/{destinatario}")
    public ResponseEntity<Map<String, Object>> buscarPorDestinatario(
            @Parameter(description = "Correo destinatario", example = "cliente@mail.com")
            @PathVariable String destinatario) {

        List<NotiListadoDTO> notificaciones = service.buscarPorDestinatario(destinatario);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Notificaciones encontradas por destinatario");
        respuesta.put("total", notificaciones.size());
        respuesta.put("notificaciones", notificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8086/notificaciones/estado/{estado}
    @Operation(
            summary = "Buscar notificaciones por estado",
            description = "Lista notificaciones filtradas por estado. En este MS normalmente se registran como ENVIADA."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificaciones encontradas por estado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "No existen notificaciones con ese estado")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> buscarPorEstado(
            @Parameter(description = "Estado de la notificación", example = "ENVIADA")
            @PathVariable String estado) {

        List<NotiListadoDTO> notificaciones = service.buscarPorEstado(estado);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Notificaciones encontradas por estado");
        respuesta.put("total", notificaciones.size());
        respuesta.put("notificaciones", notificaciones);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8086/notificaciones/{id}
    @Operation(
            summary = "Eliminar notificación",
            description = "Elimina una notificación usando su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(
            @Parameter(description = "ID de la notificación", example = "1")
            @PathVariable Integer id) {

        service.eliminar(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Notificación eliminada correctamente");
        respuesta.put("idEliminado", id);

        return ResponseEntity.ok(respuesta);
    }
}