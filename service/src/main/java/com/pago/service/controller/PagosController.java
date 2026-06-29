package com.pago.service.controller;

import java.util.LinkedHashMap;
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

import com.pago.service.dto.PagosDTO;
import com.pago.service.model.Pagos;
import com.pago.service.pagosService.PagosService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controlador REST para gestionar pagos
@RestController
@RequestMapping("/pagos")
@Tag(name = "Pagos", description = "Endpoints para procesar, consultar y eliminar pagos")
public class PagosController {

    // Service encargado de la lógica de pagos
    private final PagosService pagosService;

    // Constructor para inyectar service
    public PagosController(PagosService pagosService) {
        this.pagosService = pagosService;
    }

    // POST http://localhost:8085/pagos/procesar
    @Operation(
            summary = "Procesar pago",
            description = "Procesa el pago de un pedido, valida el usuario y registra el pago como APROBADO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago procesado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El pedido ya tiene un pago registrado"),
            @ApiResponse(responseCode = "502", description = "Error al comunicarse con otro microservicio"),
            @ApiResponse(responseCode = "503", description = "Microservicio externo no disponible")
    })
    @PostMapping("/procesar")
    public ResponseEntity<Map<String, Object>> procesarPago(@Valid @RequestBody PagosDTO dto) {

        Map<String, Object> pago = pagosService.generarPago(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(pago);
    }

    // GET http://localhost:8085/pagos
    @Operation(
            summary = "Listar pagos",
            description = "Obtiene todos los pagos registrados en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos obtenidos correctamente")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarPagos() {

        List<Pagos> pagos = pagosService.listarTodos();

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pagos obtenidos correctamente");
        respuesta.put("total", pagos.size());
        respuesta.put("pagos", pagos);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8085/pagos/{id}
    @Operation(
            summary = "Buscar pago por ID",
            description = "Obtiene un pago específico según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(
            @Parameter(description = "ID del pago", example = "1")
            @PathVariable Integer id) {

        Pagos pago = pagosService.buscarPorId(id);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pago encontrado correctamente");
        respuesta.put("pago", pago);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8085/pagos/pedido/{pedidoId}
    @Operation(
            summary = "Buscar pago por pedido",
            description = "Obtiene el pago asociado a un pedido específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago encontrado para el pedido"),
            @ApiResponse(responseCode = "404", description = "No existe pago para ese pedido")
    })
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Map<String, Object>> buscarPorPedido(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Integer pedidoId) {

        Pagos pago = pagosService.buscarPorPedido(pedidoId);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pago encontrado para el pedido");
        respuesta.put("pago", pago);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8085/pagos/usuario/{usuarioId}
    @Operation(
            summary = "Buscar pagos por usuario",
            description = "Lista todos los pagos realizados por un usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos encontrados para el usuario"),
            @ApiResponse(responseCode = "404", description = "No existen pagos para ese usuario")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer usuarioId) {

        List<Pagos> pagos = pagosService.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pagos encontrados para el usuario");
        respuesta.put("usuarioId", usuarioId);
        respuesta.put("total", pagos.size());
        respuesta.put("pagos", pagos);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8085/pagos/estado/{estado}
    @Operation(
            summary = "Buscar pagos por estado",
            description = "Lista pagos filtrados por estado, por ejemplo APROBADO, RECHAZADO o PENDIENTE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos encontrados por estado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "No existen pagos con ese estado")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> buscarPorEstado(
            @Parameter(description = "Estado del pago", example = "APROBADO")
            @PathVariable String estado) {

        List<Pagos> pagos = pagosService.buscarPorEstado(estado);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pagos encontrados por estado");
        respuesta.put("estado", estado.toUpperCase());
        respuesta.put("total", pagos.size());
        respuesta.put("pagos", pagos);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8085/pagos/{id}
    @Operation(
            summary = "Eliminar pago",
            description = "Elimina un pago según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarPago(
            @Parameter(description = "ID del pago", example = "1")
            @PathVariable Integer id) {

        pagosService.eliminarPorId(id);

        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pago eliminado correctamente");

        return ResponseEntity.ok(respuesta);
    }

}