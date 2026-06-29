package com.pizzas.certificacion.controller;

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
import com.pizzas.certificacion.dto.CertificacionRequestDTO;
import com.pizzas.certificacion.dto.CertificacionResponseDTO;
import com.pizzas.certificacion.exception.ExcepcionPersonalizada;
import com.pizzas.certificacion.exception.ManejadorErrores;
import com.pizzas.certificacion.service.CertificacionService;

// Pruebas unitarias del controller de certificación usando MockMvc
@WebMvcTest(CertificacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
public class CertificacionControllerTest {

     @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CertificacionService service;

    @Test
    void generarCertificacion_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        CertificacionResponseDTO certificacion = crearResponse();

        when(service.generarCertificacion(any(CertificacionRequestDTO.class))).thenReturn(certificacion);

        mockMvc.perform(post("/certificaciones/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyGenerar())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Certificación generada correctamente"))
                .andExpect(jsonPath("$.certificacion.id").value(1))
                .andExpect(jsonPath("$.certificacion.pedidoId").value(1))
                .andExpect(jsonPath("$.certificacion.estadoPedido").value("ENTREGADO"));

        verify(service).generarCertificacion(any(CertificacionRequestDTO.class));
    }

    @Test
    void generarCertificacion_CuandoPedidoIdInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pedidoId", 0);

        mockMvc.perform(post("/certificaciones/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación"));

        verify(service, never()).generarCertificacion(any(CertificacionRequestDTO.class));
    }

    @Test
    void generarCertificacion_CuandoYaExiste_DebeResponderConflict() throws Exception {
        when(service.generarCertificacion(any(CertificacionRequestDTO.class)))
                .thenThrow(new ExcepcionPersonalizada(
                        "Ya existe una certificación para el pedido con ID: 1",
                        HttpStatus.CONFLICT
                ));

        mockMvc.perform(post("/certificaciones/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyGenerar())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una certificación para el pedido con ID: 1"));
    }

    @Test
    void listarTodas_DebeResponderCertificaciones() throws Exception {
        CertificacionResponseDTO certificacion = crearResponse();

        when(service.listarTodas()).thenReturn(List.of(certificacion));

        mockMvc.perform(get("/certificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Listado de certificaciones obtenido correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.certificaciones[0].id").value(1))
                .andExpect(jsonPath("$.certificaciones[0].tieneResena").value(true));

        verify(service).listarTodas();
    }

    @Test
    void buscarPorId_CuandoExiste_DebeResponderCertificacion() throws Exception {
        CertificacionResponseDTO certificacion = crearResponse();

        when(service.buscarPorId(1)).thenReturn(certificacion);

        mockMvc.perform(get("/certificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Certificación encontrada"))
                .andExpect(jsonPath("$.certificacion.id").value(1))
                .andExpect(jsonPath("$.certificacion.pedidoId").value(1));

        verify(service).buscarPorId(1);
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeResponderNotFound() throws Exception {
        when(service.buscarPorId(99))
                .thenThrow(new ExcepcionPersonalizada(
                        "Certificación no encontrada con ID: 99",
                        HttpStatus.NOT_FOUND
                ));

        mockMvc.perform(get("/certificaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Certificación no encontrada con ID: 99"));
    }

    @Test
    void buscarPorPedido_DebeResponderCertificacion() throws Exception {
        CertificacionResponseDTO certificacion = crearResponse();

        when(service.buscarPorPedido(1)).thenReturn(certificacion);

        mockMvc.perform(get("/certificaciones/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Certificación encontrada para el pedido"))
                .andExpect(jsonPath("$.certificacion.pedidoId").value(1));

        verify(service).buscarPorPedido(1);
    }

    @Test
    void buscarPorUsuario_DebeResponderCertificaciones() throws Exception {
        CertificacionResponseDTO certificacion = crearResponse();

        when(service.buscarPorUsuario(1)).thenReturn(List.of(certificacion));

        mockMvc.perform(get("/certificaciones/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Certificaciones encontradas para el usuario"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.certificaciones[0].usuarioId").value(1));

        verify(service).buscarPorUsuario(1);
    }

    @Test
    void buscarPorEstadoPedido_DebeResponderCertificaciones() throws Exception {
        CertificacionResponseDTO certificacion = crearResponse();

        when(service.buscarPorEstadoPedido("ENTREGADO")).thenReturn(List.of(certificacion));

        mockMvc.perform(get("/certificaciones/estado/ENTREGADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Certificaciones encontradas por estado de pedido"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.certificaciones[0].estadoPedido").value("ENTREGADO"));

        verify(service).buscarPorEstadoPedido("ENTREGADO");
    }

    @Test
    void buscarPorTieneResena_DebeResponderCertificaciones() throws Exception {
        CertificacionResponseDTO certificacion = crearResponse();

        when(service.buscarPorTieneResena(true)).thenReturn(List.of(certificacion));

        mockMvc.perform(get("/certificaciones/resena/true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Certificaciones filtradas por reseña"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.certificaciones[0].tieneResena").value(true));

        verify(service).buscarPorTieneResena(true);
    }

    @Test
    void eliminar_CuandoExiste_DebeResponderOk() throws Exception {
        doNothing().when(service).eliminar(1);

        mockMvc.perform(delete("/certificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Certificación eliminada correctamente"))
                .andExpect(jsonPath("$.idEliminado").value(1));

        verify(service).eliminar(1);
    }

    private CertificacionResponseDTO crearResponse() {
        return new CertificacionResponseDTO(
                1,
                1,
                1,
                "Juan Perez",
                "juan@mail.com",
                "2026-06-29 17:00:00",
                "Pizza napolitana x2",
                2,
                19980,
                "DEBITO",
                "ENTREGADO",
                "Pedro",
                "2026-06-29 17:30:00",
                "A TIEMPO",
                true,
                "Pizza napolitana: Excelente servicio",
                5,
                "2026-06-29 18:00:00",
                "Certificación encontrada"
        );
    }

    private Map<String, Object> bodyGenerar() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pedidoId", 1);
        return body;
    }

}
