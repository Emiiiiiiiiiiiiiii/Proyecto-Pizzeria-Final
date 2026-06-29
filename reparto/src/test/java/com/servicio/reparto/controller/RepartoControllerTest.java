package com.servicio.reparto.controller;

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
import com.servicio.reparto.dto.RepartoRequestDTO;
import com.servicio.reparto.dto.RepartoResponseDTO;
import com.servicio.reparto.exception.ExcepcionPersonalizada;
import com.servicio.reparto.exception.ManejadorErrores;
import com.servicio.reparto.service.RepartoService;

// Pruebas unitarias del controller de reparto usando MockMvc
@WebMvcTest(RepartoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
public class RepartoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RepartoService repartoService;

    @Test
    void generarReparto_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        RepartoResponseDTO reparto = crearResponse("PREPARANDO");

        when(repartoService.generarReparto(any(RepartoRequestDTO.class))).thenReturn(reparto);

        Map<String, Object> body = bodyReparto();

        mockMvc.perform(post("/reparto/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Reparto generado correctamente"))
                .andExpect(jsonPath("$.reparto.id").value(1))
                .andExpect(jsonPath("$.reparto.pedidoId").value(1))
                .andExpect(jsonPath("$.reparto.estadoReparto").value("PREPARANDO"));

        verify(repartoService).generarReparto(any(RepartoRequestDTO.class));
    }

    @Test
    void generarReparto_CuandoDireccionInvalida_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = bodyReparto();
        body.put("direccionEntrega", "abc");

        mockMvc.perform(post("/reparto/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación en reparto"));

        verify(repartoService, never()).generarReparto(any(RepartoRequestDTO.class));
    }

    @Test
    void generarReparto_CuandoYaExiste_DebeResponderConflict() throws Exception {
        when(repartoService.generarReparto(any(RepartoRequestDTO.class)))
                .thenThrow(new ExcepcionPersonalizada(
                        "El pedido con ID 1 ya tiene reparto asignado.",
                        HttpStatus.CONFLICT
                ));

        mockMvc.perform(post("/reparto/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyReparto())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El pedido con ID 1 ya tiene reparto asignado."));
    }

    @Test
    void listarTodos_DebeResponderListaDirecta() throws Exception {
        RepartoResponseDTO reparto = crearResponse("EN_CAMINO");

        when(repartoService.listarTodos()).thenReturn(List.of(reparto));

        mockMvc.perform(get("/reparto/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].pedidoId").value(1))
                .andExpect(jsonPath("$[0].estadoReparto").value("EN_CAMINO"));

        verify(repartoService).listarTodos();
    }

    @Test
    void listarTodosRespuesta_DebeResponderRepartos() throws Exception {
        RepartoResponseDTO reparto = crearResponse("EN_CAMINO");

        when(repartoService.listarTodos()).thenReturn(List.of(reparto));

        mockMvc.perform(get("/reparto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Listado de repartos obtenido correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.repartos[0].id").value(1))
                .andExpect(jsonPath("$.repartos[0].estadoReparto").value("EN_CAMINO"));

        verify(repartoService).listarTodos();
    }

    @Test
    void buscarPorId_CuandoExiste_DebeResponderReparto() throws Exception {
        RepartoResponseDTO reparto = crearResponse("EN_CAMINO");

        when(repartoService.buscarPorId(1L)).thenReturn(reparto);

        mockMvc.perform(get("/reparto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reparto encontrado"))
                .andExpect(jsonPath("$.reparto.id").value(1))
                .andExpect(jsonPath("$.reparto.pedidoId").value(1));

        verify(repartoService).buscarPorId(1L);
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeResponderNotFound() throws Exception {
        when(repartoService.buscarPorId(99L))
                .thenThrow(new ExcepcionPersonalizada(
                        "Reparto no encontrado con ID: 99",
                        HttpStatus.NOT_FOUND
                ));

        mockMvc.perform(get("/reparto/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Reparto no encontrado con ID: 99"));
    }

    @Test
    void buscarPorPedido_DebeResponderReparto() throws Exception {
        RepartoResponseDTO reparto = crearResponse("EN_CAMINO");

        when(repartoService.buscarPorPedido(1)).thenReturn(reparto);

        mockMvc.perform(get("/reparto/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reparto encontrado para el pedido"))
                .andExpect(jsonPath("$.reparto.pedidoId").value(1));

        verify(repartoService).buscarPorPedido(1);
    }

    @Test
    void buscarPorUsuario_DebeResponderRepartos() throws Exception {
        RepartoResponseDTO reparto = crearResponse("EN_CAMINO");

        when(repartoService.buscarPorUsuario(1)).thenReturn(List.of(reparto));

        mockMvc.perform(get("/reparto/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Repartos encontrados para el usuario"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.repartos[0].usuarioId").value(1));

        verify(repartoService).buscarPorUsuario(1);
    }

    @Test
    void buscarPorEstado_DebeResponderRepartos() throws Exception {
        RepartoResponseDTO reparto = crearResponse("EN_CAMINO");

        when(repartoService.buscarPorEstado("EN_CAMINO")).thenReturn(List.of(reparto));

        mockMvc.perform(get("/reparto/estado/EN_CAMINO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Repartos encontrados por estado"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.repartos[0].estadoReparto").value("EN_CAMINO"));

        verify(repartoService).buscarPorEstado("EN_CAMINO");
    }

    @Test
    void actualizarEstado_CuandoDatosValidos_DebeResponderOk() throws Exception {
        RepartoResponseDTO reparto = crearResponse("ENTREGADO");

        when(repartoService.actualizarEstadoReparto(eq(1L), eq("ENTREGADO"))).thenReturn(reparto);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estadoReparto", "ENTREGADO");

        mockMvc.perform(put("/reparto/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Estado de reparto actualizado correctamente"))
                .andExpect(jsonPath("$.reparto.estadoReparto").value("ENTREGADO"));

        verify(repartoService).actualizarEstadoReparto(1L, "ENTREGADO");
    }

    @Test
    void actualizarEstado_CuandoEstadoInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estadoReparto", "MALO");

        mockMvc.perform(put("/reparto/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación en reparto"));

        verify(repartoService, never()).actualizarEstadoReparto(any(Long.class), any(String.class));
    }

    @Test
    void eliminar_CuandoExiste_DebeResponderOk() throws Exception {
        doNothing().when(repartoService).eliminar(1L);

        mockMvc.perform(delete("/reparto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reparto eliminado correctamente"))
                .andExpect(jsonPath("$.idEliminado").value(1));

        verify(repartoService).eliminar(1L);
    }

    private RepartoResponseDTO crearResponse(String estado) {
        return new RepartoResponseDTO(
                1L,
                1,
                1,
                "Juan Perez",
                "juan@mail.com",
                "Av Siempre Viva 123",
                estado,
                "Pedro",
                "2026-06-29 18:00:00"
        );
    }

    private Map<String, Object> bodyReparto() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pedidoId", 1);
        body.put("usuarioId", 1);
        body.put("direccionEntrega", "Av Siempre Viva 123");
        body.put("repartidor", "Pedro");
        return body;
    }

}
