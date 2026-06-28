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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    // Service encargado de la lógica del pedido
    private final PedidoService pedidoService;

    // Constructor para inyectar el service
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Crea un pedido usando usuario, carrito, pagos, inventario y notificaciones
    @PostMapping("/guardar")
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody PedidoRequestDTO request) {

        PedidoResponseDTO pedidoCreado = pedidoService.crearPedido(request);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", pedidoCreado.getMensaje());
        respuesta.put("pedido", pedidoCreado);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Lista todos los pedidos registrados
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {

        List<Pedido> pedidos = pedidoService.listarPedidos();

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedidos obtenidos correctamente");
        respuesta.put("total", pedidos.size());
        respuesta.put("pedidos", pedidos);

        return ResponseEntity.ok(respuesta);
    }

    // Busca un pedido específico por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Integer id) {

        Pedido pedido = pedidoService.buscarPorId(id);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedido encontrado correctamente");
        respuesta.put("pedido", pedido);

        return ResponseEntity.ok(respuesta);
    }

    // Busca todos los pedidos realizados por un usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> buscarPorUsuario(@PathVariable Integer usuarioId) {

        List<Pedido> pedidos = pedidoService.buscarPorUsuario(usuarioId);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedidos del usuario obtenidos correctamente");
        respuesta.put("usuarioId", usuarioId);
        respuesta.put("total", pedidos.size());
        respuesta.put("pedidos", pedidos);

        return ResponseEntity.ok(respuesta);
    }

    // Busca pedidos por estado, por ejemplo PAGADO o ENTREGADO
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> buscarPorEstado(@PathVariable String estado) {

        List<Pedido> pedidos = pedidoService.buscarPorEstado(estado);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedidos filtrados por estado correctamente");
        respuesta.put("estado", estado.toUpperCase());
        respuesta.put("total", pedidos.size());
        respuesta.put("pedidos", pedidos);

        return ResponseEntity.ok(respuesta);
    }

    // Actualiza el estado de un pedido
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> actualizarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody EstadoPedidoDTO dto) {

        Pedido pedidoActualizado = pedidoService.actualizarPedido(id, dto.getEstado());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Estado del pedido actualizado correctamente");
        respuesta.put("pedido", pedidoActualizado);

        return ResponseEntity.ok(respuesta);
    }

    // Elimina un pedido por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Integer id) {

        pedidoService.eliminarPedido(id);

        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Pedido eliminado correctamente");

        return ResponseEntity.ok(respuesta);
    }

}
