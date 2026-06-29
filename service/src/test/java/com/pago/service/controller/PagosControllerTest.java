package com.pago.service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import com.pago.service.ExcepcionPersonalizada;
import com.pago.service.ManejadorError;
import com.pago.service.dto.PagosDTO;
import com.pago.service.model.Pagos;
import com.pago.service.pagosService.PagosService;

// Pruebas unitarias del controller de pagos usando MockMvc
@WebMvcTest(PagosController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorError.class)
public class PagosControllerTest {
     @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PagosService pagosService;

    @Test
    void procesarPago_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        Map<String, Object> respuestaService = new LinkedHashMap<>();
        respuestaService.put("status", "APROBADO");
        respuestaService.put("mensaje", "Pago aprobado correctamente");
        respuestaService.put("idPago", 1);
        respuestaService.put("montoAprobado", 25980);
        respuestaService.put("pedidoId", 1);
        respuestaService.put("usuarioId", 1);
        respuestaService.put("nombreUsuario", "Juan");

        when(pagosService.generarPago(any(PagosDTO.class))).thenReturn(respuestaService);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("monto", 25980);
        body.put("pedidoId", 1);
        body.put("usuarioId", 1);

        mockMvc.perform(post("/pagos/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APROBADO"))
                .andExpect(jsonPath("$.mensaje").value("Pago aprobado correctamente"))
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.montoAprobado").value(25980));

        verify(pagosService).generarPago(any(PagosDTO.class));
    }

    @Test
    void procesarPago_CuandoMontoInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("monto", 0);
        body.put("pedidoId", 1);
        body.put("usuarioId", 1);

        mockMvc.perform(post("/pagos/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación en los datos del pago"));

        verify(pagosService, never()).generarPago(any(PagosDTO.class));
    }

    @Test
    void procesarPago_CuandoPedidoYaPagado_DebeResponderConflict() throws Exception {
        when(pagosService.generarPago(any(PagosDTO.class)))
                .thenThrow(new ExcepcionPersonalizada(
                        "El pedido con ID 1 ya tiene un pago registrado.",
                        HttpStatus.CONFLICT
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("monto", 25980);
        body.put("pedidoId", 1);
        body.put("usuarioId", 1);

        mockMvc.perform(post("/pagos/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El pedido con ID 1 ya tiene un pago registrado."));
    }

    @Test
    void listarPagos_DebeResponderPagos() throws Exception {
        Pagos pago = crearPago();

        when(pagosService.listarTodos()).thenReturn(List.of(pago));

        mockMvc.perform(get("/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pagos obtenidos correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pagos[0].id").value(1))
                .andExpect(jsonPath("$.pagos[0].estado").value("APROBADO"));

        verify(pagosService).listarTodos();
    }

    @Test
    void buscarPorId_CuandoExiste_DebeResponderPago() throws Exception {
        Pagos pago = crearPago();

        when(pagosService.buscarPorId(1)).thenReturn(pago);

        mockMvc.perform(get("/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pago encontrado correctamente"))
                .andExpect(jsonPath("$.pago.id").value(1))
                .andExpect(jsonPath("$.pago.monto").value(25980));

        verify(pagosService).buscarPorId(1);
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeResponderNotFound() throws Exception {
        when(pagosService.buscarPorId(99))
                .thenThrow(new ExcepcionPersonalizada(
                        "Pago no encontrado con ID: 99",
                        HttpStatus.NOT_FOUND
                ));

        mockMvc.perform(get("/pagos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pago no encontrado con ID: 99"));
    }

    @Test
    void buscarPorPedido_DebeResponderPago() throws Exception {
        Pagos pago = crearPago();

        when(pagosService.buscarPorPedido(1)).thenReturn(pago);

        mockMvc.perform(get("/pagos/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pago encontrado para el pedido"))
                .andExpect(jsonPath("$.pago.pedidoId").value(1));

        verify(pagosService).buscarPorPedido(1);
    }

    @Test
    void buscarPorUsuario_DebeResponderPagos() throws Exception {
        Pagos pago = crearPago();

        when(pagosService.buscarPorUsuario(1)).thenReturn(List.of(pago));

        mockMvc.perform(get("/pagos/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pagos encontrados para el usuario"))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.total").value(1));

        verify(pagosService).buscarPorUsuario(1);
    }

    @Test
    void buscarPorEstado_DebeResponderPagos() throws Exception {
        Pagos pago = crearPago();

        when(pagosService.buscarPorEstado("APROBADO")).thenReturn(List.of(pago));

        mockMvc.perform(get("/pagos/estado/APROBADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pagos encontrados por estado"))
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.total").value(1));

        verify(pagosService).buscarPorEstado("APROBADO");
    }

    @Test
    void eliminarPago_CuandoExiste_DebeResponderOk() throws Exception {
        doNothing().when(pagosService).eliminarPorId(1);

        mockMvc.perform(delete("/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pago eliminado correctamente"));

        verify(pagosService).eliminarPorId(1);
    }

    private Pagos crearPago() {
        Pagos pago = new Pagos();
        pago.setId(1);
        pago.setMonto(25980);
        pago.setEstado("APROBADO");
        pago.setPedidoId(1);
        pago.setUsuarioId(1);
        pago.setFechaPago(LocalDateTime.now());
        return pago;
    }

}
