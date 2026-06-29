package com.servicio.notificaciones.notiController;

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
import com.servicio.notificaciones.dto.NotiDTO;
import com.servicio.notificaciones.dto.NotiListadoDTO;
import com.servicio.notificaciones.dto.NotiResponseDTO;
import com.servicio.notificaciones.exception.ExcepcionPersonalizada;
import com.servicio.notificaciones.exception.ManejadorErrores;
import com.servicio.notificaciones.notiService.NotiService;

// Pruebas unitarias del controller de notificaciones usando MockMvc
@WebMvcTest(NotiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
public class NotiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private NotiService service;

    @Test
    void enviar_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        NotiResponseDTO notificacion = crearResponse();

        when(service.enviar(any(NotiDTO.class))).thenReturn(notificacion);

        mockMvc.perform(post("/notificaciones/enviar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyNotificacion())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Notificación enviada correctamente"))
                .andExpect(jsonPath("$.notificacion.id").value(1))
                .andExpect(jsonPath("$.notificacion.usuarioId").value(1))
                .andExpect(jsonPath("$.notificacion.tipo").value("PAGO"))
                .andExpect(jsonPath("$.notificacion.estado").value("ENVIADA"));

        verify(service).enviar(any(NotiDTO.class));
    }

    @Test
    void enviar_CuandoEmailInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = bodyNotificacion();
        body.put("destinatario", "correo-malo");

        mockMvc.perform(post("/notificaciones/enviar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación en notificaciones"));

        verify(service, never()).enviar(any(NotiDTO.class));
    }

    @Test
    void enviar_CuandoUsuarioIdInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = bodyNotificacion();
        body.put("usuarioId", 0);

        mockMvc.perform(post("/notificaciones/enviar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación en notificaciones"));

        verify(service, never()).enviar(any(NotiDTO.class));
    }

    @Test
    void listarTodas_DebeResponderNotificaciones() throws Exception {
        NotiListadoDTO notificacion = crearListado();

        when(service.listarTodas()).thenReturn(List.of(notificacion));

        mockMvc.perform(get("/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Listado de notificaciones obtenido correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.notificaciones[0].id").value(1))
                .andExpect(jsonPath("$.notificaciones[0].tipo").value("PAGO"));

        verify(service).listarTodas();
    }

    @Test
    void listarTodasAlternativo_DebeResponderNotificaciones() throws Exception {
        NotiListadoDTO notificacion = crearListado();

        when(service.listarTodas()).thenReturn(List.of(notificacion));

        mockMvc.perform(get("/notificaciones/todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Listado de notificaciones obtenido correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.notificaciones[0].estado").value("ENVIADA"));

        verify(service).listarTodas();
    }

    @Test
    void buscarPorId_CuandoExiste_DebeResponderNotificacion() throws Exception {
        NotiResponseDTO notificacion = crearResponse();

        when(service.buscarPorId(1)).thenReturn(notificacion);

        mockMvc.perform(get("/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Notificación encontrada"))
                .andExpect(jsonPath("$.notificacion.id").value(1))
                .andExpect(jsonPath("$.notificacion.destinatario").value("cliente@mail.com"));

        verify(service).buscarPorId(1);
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeResponderNotFound() throws Exception {
        when(service.buscarPorId(99))
                .thenThrow(new ExcepcionPersonalizada(
                        "Notificación no encontrada con ID: 99",
                        HttpStatus.NOT_FOUND
                ));

        mockMvc.perform(get("/notificaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Notificación no encontrada con ID: 99"));
    }

    @Test
    void buscarPorUsuario_DebeResponderNotificaciones() throws Exception {
        NotiListadoDTO notificacion = crearListado();

        when(service.buscarPorUsuario(1)).thenReturn(List.of(notificacion));

        mockMvc.perform(get("/notificaciones/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Notificaciones encontradas para el usuario"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.notificaciones[0].usuarioId").value(1));

        verify(service).buscarPorUsuario(1);
    }

    @Test
    void buscarPorPedido_DebeResponderNotificaciones() throws Exception {
        NotiListadoDTO notificacion = crearListado();

        when(service.buscarPorPedido(10)).thenReturn(List.of(notificacion));

        mockMvc.perform(get("/notificaciones/pedido/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Notificaciones encontradas para el pedido"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.notificaciones[0].pedidoId").value(10));

        verify(service).buscarPorPedido(10);
    }

    @Test
    void buscarPorTipo_DebeResponderNotificaciones() throws Exception {
        NotiListadoDTO notificacion = crearListado();

        when(service.buscarPorTipo("PAGO")).thenReturn(List.of(notificacion));

        mockMvc.perform(get("/notificaciones/tipo/PAGO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Notificaciones encontradas por tipo"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.notificaciones[0].tipo").value("PAGO"));

        verify(service).buscarPorTipo("PAGO");
    }

    @Test
    void buscarPorDestinatario_DebeResponderNotificaciones() throws Exception {
        NotiListadoDTO notificacion = crearListado();

        when(service.buscarPorDestinatario("cliente@mail.com")).thenReturn(List.of(notificacion));

        mockMvc.perform(get("/notificaciones/destinatario/cliente@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Notificaciones encontradas por destinatario"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.notificaciones[0].destinatario").value("cliente@mail.com"));

        verify(service).buscarPorDestinatario("cliente@mail.com");
    }

    @Test
    void buscarPorEstado_DebeResponderNotificaciones() throws Exception {
        NotiListadoDTO notificacion = crearListado();

        when(service.buscarPorEstado("ENVIADA")).thenReturn(List.of(notificacion));

        mockMvc.perform(get("/notificaciones/estado/ENVIADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Notificaciones encontradas por estado"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.notificaciones[0].estado").value("ENVIADA"));

        verify(service).buscarPorEstado("ENVIADA");
    }

    @Test
    void eliminar_CuandoExiste_DebeResponderOk() throws Exception {
        doNothing().when(service).eliminar(1);

        mockMvc.perform(delete("/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Notificación eliminada correctamente"))
                .andExpect(jsonPath("$.idEliminado").value(1));

        verify(service).eliminar(1);
    }

    private NotiResponseDTO crearResponse() {
        return new NotiResponseDTO(
                1,
                1,
                10,
                "PAGO",
                "cliente@mail.com",
                "Pago recibido correctamente",
                "ENVIADA",
                LocalDateTime.now()
        );
    }

    private NotiListadoDTO crearListado() {
        return new NotiListadoDTO(
                1,
                1,
                10,
                "PAGO",
                "cliente@mail.com",
                "ENVIADA",
                LocalDateTime.now()
        );
    }

    private Map<String, Object> bodyNotificacion() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("usuarioId", 1);
        body.put("pedidoId", 10);
        body.put("tipo", "PAGO");
        body.put("destinatario", "cliente@mail.com");
        body.put("mensaje", "Pago recibido correctamente");
        return body;
    }

}
