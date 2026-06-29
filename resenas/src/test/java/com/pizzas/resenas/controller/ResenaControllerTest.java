package com.pizzas.resenas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
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
import com.pizzas.resenas.dto.ResenaRequestDTO;
import com.pizzas.resenas.dto.ResenaResponseDTO;
import com.pizzas.resenas.dto.ResenaUpdateDTO;
import com.pizzas.resenas.exception.ExcepcionPersonalizada;
import com.pizzas.resenas.exception.ManejadorErrores;
import com.pizzas.resenas.service.ResenaService;

// Pruebas unitarias del controller de reseñas usando MockMvc
@WebMvcTest(ResenaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
public class ResenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ResenaService service;

    @Test
    void crear_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        ResenaResponseDTO resena = crearResponse();

        when(service.crearResena(any(ResenaRequestDTO.class))).thenReturn(resena);

        mockMvc.perform(post("/resenas/guardar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyCrear())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Reseña registrada correctamente"))
                .andExpect(jsonPath("$.resena.id").value(1))
                .andExpect(jsonPath("$.resena.pedidoId").value(1))
                .andExpect(jsonPath("$.resena.estrellas").value(5));

        verify(service).crearResena(any(ResenaRequestDTO.class));
    }

    @Test
    void crearAlternativo_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        ResenaResponseDTO resena = crearResponse();

        when(service.crearResena(any(ResenaRequestDTO.class))).thenReturn(resena);

        mockMvc.perform(post("/resenas/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyCrear())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Reseña registrada correctamente"))
                .andExpect(jsonPath("$.resena.comentario").value("Muy rica la pizza"));

        verify(service).crearResena(any(ResenaRequestDTO.class));
    }

    @Test
    void crear_CuandoComentarioInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = bodyCrear();
        body.put("comentario", "abc");

        mockMvc.perform(post("/resenas/guardar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación en reseñas"));

        verify(service, never()).crearResena(any(ResenaRequestDTO.class));
    }

    @Test
    void crear_CuandoPedidoYaTieneResena_DebeResponderConflict() throws Exception {
        when(service.crearResena(any(ResenaRequestDTO.class)))
                .thenThrow(new ExcepcionPersonalizada(
                        "El pedido con ID 1 ya tiene una reseña registrada.",
                        HttpStatus.CONFLICT
                ));

        mockMvc.perform(post("/resenas/guardar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyCrear())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El pedido con ID 1 ya tiene una reseña registrada."));
    }

    @Test
    void listarTodas_DebeResponderResenas() throws Exception {
        ResenaResponseDTO resena = crearResponse();

        when(service.listarTodas()).thenReturn(List.of(resena));

        mockMvc.perform(get("/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Listado de reseñas obtenido correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.resenas[0].id").value(1))
                .andExpect(jsonPath("$.resenas[0].estrellas").value(5));

        verify(service).listarTodas();
    }

    @Test
    void buscarPorId_CuandoExiste_DebeResponderResena() throws Exception {
        ResenaResponseDTO resena = crearResponse();

        when(service.buscarPorId(1)).thenReturn(resena);

        mockMvc.perform(get("/resenas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reseña encontrada"))
                .andExpect(jsonPath("$.resena.id").value(1))
                .andExpect(jsonPath("$.resena.pedidoId").value(1));

        verify(service).buscarPorId(1);
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeResponderNotFound() throws Exception {
        when(service.buscarPorId(99))
                .thenThrow(new ExcepcionPersonalizada(
                        "Reseña no encontrada con ID: 99",
                        HttpStatus.NOT_FOUND
                ));

        mockMvc.perform(get("/resenas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Reseña no encontrada con ID: 99"));
    }

    @Test
    void buscarPorPedido_DebeResponderListaDirecta() throws Exception {
        ResenaResponseDTO resena = crearResponse();

        when(service.buscarPorPedido(1)).thenReturn(List.of(resena));

        mockMvc.perform(get("/resenas/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].pedidoId").value(1));

        verify(service).buscarPorPedido(1);
    }

    @Test
    void buscarPorPedidoRespuesta_DebeResponderResenas() throws Exception {
        ResenaResponseDTO resena = crearResponse();

        when(service.buscarPorPedido(1)).thenReturn(List.of(resena));

        mockMvc.perform(get("/resenas/pedido/1/respuesta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reseñas encontradas para el pedido"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.resenas[0].pedidoId").value(1));

        verify(service).buscarPorPedido(1);
    }

    @Test
    void buscarPorUsuario_DebeResponderResenas() throws Exception {
        ResenaResponseDTO resena = crearResponse();

        when(service.buscarPorUsuario(1)).thenReturn(List.of(resena));

        mockMvc.perform(get("/resenas/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reseñas encontradas para el usuario"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.resenas[0].usuarioId").value(1));

        verify(service).buscarPorUsuario(1);
    }

    @Test
    void buscarPorEstrellas_DebeResponderResenas() throws Exception {
        ResenaResponseDTO resena = crearResponse();

        when(service.buscarPorEstrellas(5)).thenReturn(List.of(resena));

        mockMvc.perform(get("/resenas/estrellas/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reseñas encontradas por estrellas"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.resenas[0].estrellas").value(5));

        verify(service).buscarPorEstrellas(5);
    }

    @Test
    void actualizar_CuandoDatosValidos_DebeResponderOk() throws Exception {
        ResenaResponseDTO actualizada = crearResponseActualizada();

        when(service.actualizar(eq(1), any(ResenaUpdateDTO.class))).thenReturn(actualizada);

        mockMvc.perform(put("/resenas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyActualizar())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reseña actualizada correctamente"))
                .andExpect(jsonPath("$.resena.comentario").value("Comentario actualizado"))
                .andExpect(jsonPath("$.resena.estrellas").value(4));

        verify(service).actualizar(eq(1), any(ResenaUpdateDTO.class));
    }

    @Test
    void actualizar_CuandoEstrellasInvalidas_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = bodyActualizar();
        body.put("estrellas", 6);

        mockMvc.perform(put("/resenas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación en reseñas"));

        verify(service, never()).actualizar(any(Integer.class), any(ResenaUpdateDTO.class));
    }

    @Test
    void eliminar_CuandoExiste_DebeResponderOk() throws Exception {
        doNothing().when(service).eliminar(1);

        mockMvc.perform(delete("/resenas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reseña eliminada correctamente"))
                .andExpect(jsonPath("$.idEliminado").value(1));

        verify(service).eliminar(1);
    }

    private ResenaResponseDTO crearResponse() {
        return new ResenaResponseDTO(
                1,
                1,
                1,
                "Juan Perez",
                "Pizza familiar napolitana",
                "Muy rica la pizza",
                5,
                LocalDateTime.now()
        );
    }

    private ResenaResponseDTO crearResponseActualizada() {
        return new ResenaResponseDTO(
                1,
                1,
                1,
                "Juan Perez",
                "Pizza familiar napolitana",
                "Comentario actualizado",
                4,
                LocalDateTime.now()
        );
    }

    private Map<String, Object> bodyCrear() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pedidoId", 1);
        body.put("usuarioId", 1);
        body.put("comentario", "Muy rica la pizza");
        body.put("estrellas", 5);
        return body;
    }

    private Map<String, Object> bodyActualizar() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("comentario", "Comentario actualizado");
        body.put("estrellas", 4);
        return body;
    }

}
