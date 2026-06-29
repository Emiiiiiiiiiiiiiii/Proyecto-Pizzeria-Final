package com.pizzas.inventario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import com.pizzas.inventario.dto.InventarioRequestDTO;
import com.pizzas.inventario.dto.InventarioResponseDTO;
import com.pizzas.inventario.exception.ExcepcionPersonalizada;
import com.pizzas.inventario.exception.ManejadorErrores;
import com.pizzas.inventario.service.InventarioService;

// Pruebas unitarias del controller de inventario usando MockMvc
@WebMvcTest(InventarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
public class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private InventarioService service;

    @Test
    void listar_DebeResponderInventario() throws Exception {
        InventarioResponseDTO inventario = crearResponse(1, 1, 50, "DISPONIBLE");

        when(service.listar()).thenReturn(List.of(inventario));

        mockMvc.perform(get("/inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Listado de inventario obtenido correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.inventario[0].catalogoId").value(1))
                .andExpect(jsonPath("$.inventario[0].estadoStock").value("DISPONIBLE"));

        verify(service).listar();
    }

    @Test
    void buscar_CuandoExiste_DebeResponderInventarioDirecto() throws Exception {
        InventarioResponseDTO inventario = crearResponse(1, 1, 50, "DISPONIBLE");

        when(service.buscarPorCatalogoId(1)).thenReturn(inventario);

        mockMvc.perform(get("/inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.catalogoId").value(1))
                .andExpect(jsonPath("$.cantidad").value(50))
                .andExpect(jsonPath("$.estadoStock").value("DISPONIBLE"));

        verify(service).buscarPorCatalogoId(1);
    }

    @Test
    void buscar_CuandoNoExiste_DebeResponderNotFound() throws Exception {
        when(service.buscarPorCatalogoId(99))
                .thenThrow(new ExcepcionPersonalizada(
                        "No se encontró stock para el ID de catálogo: 99",
                        HttpStatus.NOT_FOUND
                ));

        mockMvc.perform(get("/inventario/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró stock para el ID de catálogo: 99"));
    }

    @Test
    void listarStockBajo_DebeResponderInventario() throws Exception {
        InventarioResponseDTO inventario = crearResponse(2, 2, 10, "STOCK BAJO");

        when(service.listarStockBajo(20)).thenReturn(List.of(inventario));

        mockMvc.perform(get("/inventario/stock-bajo/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Listado de productos con stock bajo obtenido correctamente"))
                .andExpect(jsonPath("$.limite").value(20))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.inventario[0].estadoStock").value("STOCK BAJO"));

        verify(service).listarStockBajo(20);
    }

    @Test
    void guardar_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        InventarioResponseDTO inventario = crearResponse(1, 1, 50, "DISPONIBLE");

        when(service.guardar(any(InventarioRequestDTO.class))).thenReturn(inventario);

        Map<String, Object> body = bodyInventario(1, 50);

        mockMvc.perform(post("/inventario/agregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Inventario agregado correctamente"))
                .andExpect(jsonPath("$.inventario.catalogoId").value(1))
                .andExpect(jsonPath("$.inventario.cantidad").value(50));

        verify(service).guardar(any(InventarioRequestDTO.class));
    }

    @Test
    void guardar_CuandoCantidadNegativa_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = bodyInventario(1, -5);

        mockMvc.perform(post("/inventario/agregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación en inventario"));

        verify(service, never()).guardar(any(InventarioRequestDTO.class));
    }

    @Test
    void actualizar_CuandoDatosValidos_DebeResponderOk() throws Exception {
        InventarioResponseDTO actualizado = crearResponse(1, 1, 15, "STOCK BAJO");

        when(service.actualizar(eq(1), any(InventarioRequestDTO.class))).thenReturn(actualizado);

        Map<String, Object> body = bodyInventario(1, 15);

        mockMvc.perform(put("/inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Inventario actualizado correctamente"))
                .andExpect(jsonPath("$.inventario.catalogoId").value(1))
                .andExpect(jsonPath("$.inventario.cantidad").value(15))
                .andExpect(jsonPath("$.inventario.estadoStock").value("STOCK BAJO"));

        verify(service).actualizar(eq(1), any(InventarioRequestDTO.class));
    }

    @Test
    void eliminar_CuandoExiste_DebeResponderOk() throws Exception {
        doNothing().when(service).eliminar(1);

        mockMvc.perform(delete("/inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Inventario eliminado correctamente"))
                .andExpect(jsonPath("$.catalogoIdEliminado").value(1));

        verify(service).eliminar(1);
    }

    @Test
    void descontar_CuandoDatosValidos_DebeResponderOk() throws Exception {
        doNothing().when(service).descontarStock(any());

        List<Map<String, Object>> body = List.of(bodyDescuento(1, 2));

        mockMvc.perform(post("/inventario/descontar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Stock descontado exitosamente"))
                .andExpect(jsonPath("$.totalItemsDescontados").value(1));

        verify(service).descontarStock(any());
    }

    @Test
    void descontar_CuandoListaVacia_DebeResponderBadRequest() throws Exception {
        doThrow(new ExcepcionPersonalizada(
                "La lista de productos a descontar no puede estar vacía.",
                HttpStatus.BAD_REQUEST
        )).when(service).descontarStock(any());

        mockMvc.perform(post("/inventario/descontar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La lista de productos a descontar no puede estar vacía."));

        verify(service).descontarStock(any());
    }

    private InventarioResponseDTO crearResponse(Integer id, Integer catalogoId, Integer cantidad, String estadoStock) {
        return new InventarioResponseDTO(id, catalogoId, cantidad, estadoStock);
    }

    private Map<String, Object> bodyInventario(Integer catalogoId, Integer cantidad) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("catalogoId", catalogoId);
        body.put("cantidad", cantidad);
        return body;
    }

    private Map<String, Object> bodyDescuento(Integer catalogoId, Integer cantidad) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("catalogoId", catalogoId);
        body.put("cantidad", cantidad);
        return body;
    }

}
