package com.pizzas.pedido.controller;

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

import com.pizzas.pedido.dto.EstadoPedidoDTO;
import com.pizzas.pedido.dto.PedidoRequestDTO;
import com.pizzas.pedido.dto.PedidoResponseDTO;
import com.pizzas.pedido.model.Pedido;
import com.pizzas.pedido.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Controlador REST para gestionar pedidos
@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para crear, consultar, actualizar y eliminar pedidos")
public class PedidoController {
    // Service encargado de la lógica del pedido
    private final PedidoService pedidoService;

    // Constructor para inyectar el service
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // POST http://localhost:8083/pedidos/guardar
    @Operation(
            summary = "Crear pedido",
            description = "Crea un pedido usando el carrito del usuario, descuenta inventario, procesa pago y registra notificación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o carrito vacío"),
            @ApiResponse(responseCode = "404", description = "Usuario o recurso relacionado no encontrado"),
            @ApiResponse(responseCode = "502", description = "Error al comunicarse con otro microservicio"),
            @ApiResponse(responseCode = "503", description = "Microservicio externo no disponible")
    })
    @PostMapping("/guardar")
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody PedidoRequestDTO request) {

        PedidoResponseDTO pedidoCreado = pedidoService.crearPedido(request);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", pedidoCreado.getMensaje());
        respuesta.put("pedido", pedidoCreado);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // GET http://localhost:8083/pedidos
    @Operation(
            summary = "Listar pedidos",
            description = "Obtiene todos los pedidos registrados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos obtenidos correctamente")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {

        List<Pedido> pedidos = pedidoService.listarPedidos();

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedidos obtenidos correctamente");
        respuesta.put("total", pedidos.size());
        respuesta.put("pedidos", pedidos);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8083/pedidos/{id}
    @Operation(
            summary = "Buscar pedido por ID",
            description = "Obtiene un pedido específico según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Integer id) {

        Pedido pedido = pedidoService.buscarPorId(id);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedido encontrado correctamente");
        respuesta.put("pedido", pedido);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8083/pedidos/usuario/{usuarioId}
    @Operation(
            summary = "Buscar pedidos por usuario",
            description = "Lista todos los pedidos realizados por un usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos del usuario obtenidos correctamente"),
            @ApiResponse(responseCode = "404", description = "No existen pedidos para el usuario indicado")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer usuarioId) {

        List<Pedido> pedidos = pedidoService.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedidos del usuario obtenidos correctamente");
        respuesta.put("usuarioId", usuarioId);
        respuesta.put("total", pedidos.size());
        respuesta.put("pedidos", pedidos);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8083/pedidos/estado/{estado}
    @Operation(
            summary = "Buscar pedidos por estado",
            description = "Lista pedidos filtrados por estado, por ejemplo PAGADO, ENTREGADO o CANCELADO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos filtrados por estado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "No existen pedidos con el estado indicado")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> buscarPorEstado(
            @Parameter(description = "Estado del pedido", example = "PAGADO")
            @PathVariable String estado) {

        List<Pedido> pedidos = pedidoService.buscarPorEstado(estado);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedidos filtrados por estado correctamente");
        respuesta.put("estado", estado.toUpperCase());
        respuesta.put("total", pedidos.size());
        respuesta.put("pedidos", pedidos);

        return ResponseEntity.ok(respuesta);
    }

    // PUT http://localhost:8083/pedidos/{id}/estado
    @Operation(
            summary = "Actualizar estado del pedido",
            description = "Actualiza el estado de un pedido existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del pedido actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> actualizarEstado(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody EstadoPedidoDTO dto) {

        Pedido pedidoActualizado = pedidoService.actualizarPedido(id, dto.getEstado());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Estado del pedido actualizado correctamente");
        respuesta.put("pedido", pedidoActualizado);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8083/pedidos/{id}
    @Operation(
            summary = "Eliminar pedido",
            description = "Elimina un pedido según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Integer id) {

        pedidoService.eliminarPedido(id);

        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedido eliminado correctamente");

        return ResponseEntity.ok(respuesta);
    }

}
