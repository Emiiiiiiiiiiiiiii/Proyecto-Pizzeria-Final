package com.pizzas.pedido.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzas.pedido.dto.PedidoRequestDTO;
import com.pizzas.pedido.dto.PedidoResponseDTO;
import com.pizzas.pedido.exception.ExcepcionPersonalizada;
import com.pizzas.pedido.exception.ManejadorErrores;
import com.pizzas.pedido.model.Pedido;
import com.pizzas.pedido.service.PedidoService;

// Pruebas unitarias del controller de pedidos usando MockMvc
@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    void crear_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        PedidoResponseDTO pedidoCreado = new PedidoResponseDTO(
                1,
                "Juan Perez",
                25980,
                "PAGADO",
                "2026-06-29 10:00:00",
                "2x Pepperoni Familiar",
                "Pedido completado correctamente"
        );

        when(pedidoService.crearPedido(any(PedidoRequestDTO.class))).thenReturn(pedidoCreado);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("usuarioId", 1);
        body.put("metodoPago", "DEBITO");

        mockMvc.perform(post("/pedidos/guardar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Pedido completado correctamente"))
                .andExpect(jsonPath("$.pedido.id").value(1))
                .andExpect(jsonPath("$.pedido.estado").value("PAGADO"))
                .andExpect(jsonPath("$.pedido.montoTotal").value(25980));

        verify(pedidoService).crearPedido(any(PedidoRequestDTO.class));
    }

    @Test
    void crear_CuandoMetodoPagoInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("usuarioId", 1);
        body.put("metodoPago", "BITCOIN");

        mockMvc.perform(post("/pedidos/guardar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación"));

        verify(pedidoService, never()).crearPedido(any(PedidoRequestDTO.class));
    }

    @Test
    void listar_DebeResponderPedidos() throws Exception {
        Pedido pedido = crearPedido();

        when(pedidoService.listarPedidos()).thenReturn(List.of(pedido));

        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pedidos obtenidos correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pedidos[0].id").value(1))
                .andExpect(jsonPath("$.pedidos[0].estado").value("PAGADO"));

        verify(pedidoService).listarPedidos();
    }

    @Test
    void buscarPorId_CuandoExiste_DebeResponderPedido() throws Exception {
        Pedido pedido = crearPedido();

        when(pedidoService.buscarPorId(1)).thenReturn(pedido);

        mockMvc.perform(get("/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pedido encontrado correctamente"))
                .andExpect(jsonPath("$.pedido.id").value(1))
                .andExpect(jsonPath("$.pedido.nombreCliente").value("Juan Perez"));

        verify(pedidoService).buscarPorId(1);
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeResponderNotFound() throws Exception {
        when(pedidoService.buscarPorId(99))
                .thenThrow(new ExcepcionPersonalizada(
                        "Pedido no encontrado con ID: 99",
                        HttpStatus.NOT_FOUND
                ));

        mockMvc.perform(get("/pedidos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pedido no encontrado con ID: 99"));
    }

    @Test
    void buscarPorUsuario_DebeResponderPedidos() throws Exception {
        Pedido pedido = crearPedido();

        when(pedidoService.buscarPorUsuario(1)).thenReturn(List.of(pedido));

        mockMvc.perform(get("/pedidos/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pedidos del usuario obtenidos correctamente"))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.total").value(1));

        verify(pedidoService).buscarPorUsuario(1);
    }

    @Test
    void buscarPorEstado_DebeResponderPedidos() throws Exception {
        Pedido pedido = crearPedido();

        when(pedidoService.buscarPorEstado("PAGADO")).thenReturn(List.of(pedido));

        mockMvc.perform(get("/pedidos/estado/PAGADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pedidos filtrados por estado correctamente"))
                .andExpect(jsonPath("$.estado").value("PAGADO"))
                .andExpect(jsonPath("$.total").value(1));

        verify(pedidoService).buscarPorEstado("PAGADO");
    }

    @Test
    void actualizarEstado_CuandoDatosValidos_DebeResponderOk() throws Exception {
        Pedido pedido = crearPedido();
        pedido.setEstado("ENTREGADO");

        when(pedidoService.actualizarPedido(eq(1), eq("ENTREGADO"))).thenReturn(pedido);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estado", "ENTREGADO");

        mockMvc.perform(put("/pedidos/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Estado del pedido actualizado correctamente"))
                .andExpect(jsonPath("$.pedido.estado").value("ENTREGADO"));

        verify(pedidoService).actualizarPedido(1, "ENTREGADO");
    }

    @Test
    void actualizarEstado_CuandoEstadoInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estado", "MALO");

        mockMvc.perform(put("/pedidos/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación"));

        verify(pedidoService, never()).actualizarPedido(eq(1), any(String.class));
    }

    @Test
    void eliminar_CuandoExiste_DebeResponderOk() throws Exception {
        doNothing().when(pedidoService).eliminarPedido(1);

        mockMvc.perform(delete("/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pedido eliminado correctamente"));

        verify(pedidoService).eliminarPedido(1);
    }

    private Pedido crearPedido() {
        Pedido pedido = new Pedido();
        pedido.setId(1);
        pedido.setUsuarioId(1);
        pedido.setNombreCliente("Juan Perez");
        pedido.setEmailCliente("juan@mail.com");
        pedido.setMontoTotal(25980);
        pedido.setCantidadTotalItems(2);
        pedido.setDetalleProductos("2x Pepperoni Familiar");
        pedido.setEstado("PAGADO");
        pedido.setFechaPedido("2026-06-29 10:00:00");
        pedido.setMetodoPago("DEBITO");
        return pedido;
    }

}
