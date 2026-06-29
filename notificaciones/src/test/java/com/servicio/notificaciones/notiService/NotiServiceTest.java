package com.servicio.notificaciones.notiService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.servicio.notificaciones.dto.NotiDTO;
import com.servicio.notificaciones.dto.NotiListadoDTO;
import com.servicio.notificaciones.dto.NotiResponseDTO;
import com.servicio.notificaciones.exception.ExcepcionPersonalizada;
import com.servicio.notificaciones.model.Notificacion;
import com.servicio.notificaciones.notiRepository.NotiRepository;

// Pruebas unitarias para NotiService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class NotiServiceTest {

     @Mock
    private NotiRepository repository;

    @InjectMocks
    private NotiService service;

    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        notificacion = crearEntidad();
    }

    @Test
    void enviar_CuandoDatosValidos_DebeGuardarNotificacion() {
        NotiDTO dto = crearDTO();

        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion guardada = invocation.getArgument(0);
            guardada.setId(1);
            return guardada;
        });

        NotiResponseDTO respuesta = service.enviar(dto);

        assertEquals(1, respuesta.getId());
        assertEquals(1, respuesta.getUsuarioId());
        assertEquals(10, respuesta.getPedidoId());
        assertEquals("PAGO", respuesta.getTipo());
        assertEquals("cliente@mail.com", respuesta.getDestinatario());
        assertEquals("Pago recibido correctamente", respuesta.getMensaje());
        assertEquals("ENVIADA", respuesta.getEstado());
        assertNotNull(respuesta.getFechaEnvio());

        verify(repository).save(any(Notificacion.class));
    }

    @Test
    void enviar_CuandoMensajeVacio_DebeGenerarMensajeAutomatico() {
        NotiDTO dto = crearDTO();
        dto.setMensaje(" ");
        dto.setTipo("registro");
        dto.setPedidoId(null);

        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion guardada = invocation.getArgument(0);
            guardada.setId(2);
            return guardada;
        });

        NotiResponseDTO respuesta = service.enviar(dto);

        assertEquals(2, respuesta.getId());
        assertEquals("REGISTRO", respuesta.getTipo());
        assertEquals("Registro realizado correctamente.", respuesta.getMensaje());
        assertEquals("ENVIADA", respuesta.getEstado());
    }

    @Test
    void enviar_CuandoFechaValida_DebeUsarFechaEnviada() {
        NotiDTO dto = crearDTO();
        dto.setFecha("2026-06-29T18:30:00");

        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion guardada = invocation.getArgument(0);
            guardada.setId(3);
            return guardada;
        });

        NotiResponseDTO respuesta = service.enviar(dto);

        assertEquals(LocalDateTime.of(2026, 6, 29, 18, 30), respuesta.getFechaEnvio());
    }

    @Test
    void listarTodas_DebeRetornarNotificaciones() {
        when(repository.findAll()).thenReturn(List.of(notificacion));

        List<NotiListadoDTO> respuesta = service.listarTodas();

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getId());
        assertEquals("PAGO", respuesta.get(0).getTipo());
        assertEquals("ENVIADA", respuesta.get(0).getEstado());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarNotificacion() {
        when(repository.findById(1)).thenReturn(Optional.of(notificacion));

        NotiResponseDTO respuesta = service.buscarPorId(1);

        assertEquals(1, respuesta.getId());
        assertEquals(1, respuesta.getUsuarioId());
        assertEquals("cliente@mail.com", respuesta.getDestinatario());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorId(99)
        );

        assertEquals("Notificación no encontrada con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorUsuario_CuandoTieneNotificaciones_DebeRetornarLista() {
        when(repository.findByUsuarioId(1)).thenReturn(List.of(notificacion));

        List<NotiListadoDTO> respuesta = service.buscarPorUsuario(1);

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getUsuarioId());
    }

    @Test
    void buscarPorUsuario_CuandoNoTieneNotificaciones_DebeLanzarExcepcion() {
        when(repository.findByUsuarioId(99)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorUsuario(99)
        );

        assertEquals("No existen notificaciones para el usuario con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorPedido_CuandoTieneNotificaciones_DebeRetornarLista() {
        when(repository.findByPedidoId(10)).thenReturn(List.of(notificacion));

        List<NotiListadoDTO> respuesta = service.buscarPorPedido(10);

        assertEquals(1, respuesta.size());
        assertEquals(10, respuesta.get(0).getPedidoId());
    }

    @Test
    void buscarPorPedido_CuandoNoTieneNotificaciones_DebeLanzarExcepcion() {
        when(repository.findByPedidoId(99)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorPedido(99)
        );

        assertEquals("No existen notificaciones para el pedido con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorTipo_CuandoExiste_DebeRetornarLista() {
        when(repository.findByTipoIgnoreCase("PAGO")).thenReturn(List.of(notificacion));

        List<NotiListadoDTO> respuesta = service.buscarPorTipo("PAGO");

        assertEquals(1, respuesta.size());
        assertEquals("PAGO", respuesta.get(0).getTipo());
    }

    @Test
    void buscarPorTipo_CuandoTipoVacio_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorTipo(" ")
        );

        assertEquals("El tipo de notificación es obligatorio.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void buscarPorTipo_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findByTipoIgnoreCase("RESENA")).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorTipo("RESENA")
        );

        assertEquals("No existen notificaciones del tipo: RESENA", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorDestinatario_CuandoExiste_DebeRetornarLista() {
        when(repository.findByDestinatarioIgnoreCase("cliente@mail.com")).thenReturn(List.of(notificacion));

        List<NotiListadoDTO> respuesta = service.buscarPorDestinatario("cliente@mail.com");

        assertEquals(1, respuesta.size());
        assertEquals("cliente@mail.com", respuesta.get(0).getDestinatario());
    }

    @Test
    void buscarPorDestinatario_CuandoVacio_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorDestinatario(" ")
        );

        assertEquals("El destinatario es obligatorio.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void buscarPorEstado_CuandoExiste_DebeRetornarLista() {
        when(repository.findByEstadoIgnoreCase("ENVIADA")).thenReturn(List.of(notificacion));

        List<NotiListadoDTO> respuesta = service.buscarPorEstado("ENVIADA");

        assertEquals(1, respuesta.size());
        assertEquals("ENVIADA", respuesta.get(0).getEstado());
    }

    @Test
    void buscarPorEstado_CuandoVacio_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorEstado(" ")
        );

        assertEquals("El estado es obligatorio.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void buscarPorEstado_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findByEstadoIgnoreCase("ERROR")).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorEstado("ERROR")
        );

        assertEquals("No existen notificaciones con estado: ERROR", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void eliminar_CuandoExiste_DebeEliminarNotificacion() {
        when(repository.existsById(1)).thenReturn(true);

        service.eliminar(1);

        verify(repository).deleteById(1);
    }

    @Test
    void eliminar_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.existsById(99)).thenReturn(false);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.eliminar(99)
        );

        assertEquals("No existe la notificación para eliminar.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(repository, never()).deleteById(99);
    }

    private NotiDTO crearDTO() {
        NotiDTO dto = new NotiDTO();
        dto.setUsuarioId(1);
        dto.setPedidoId(10);
        dto.setTipo("pago");
        dto.setDestinatario(" cliente@mail.com ");
        dto.setMensaje(" Pago recibido correctamente ");
        return dto;
    }

    private Notificacion crearEntidad() {
        Notificacion n = new Notificacion();
        n.setId(1);
        n.setUsuarioId(1);
        n.setPedidoId(10);
        n.setTipo("PAGO");
        n.setDestinatario("cliente@mail.com");
        n.setMensaje("Pago recibido correctamente");
        n.setEstado("ENVIADA");
        n.setFechaEnvio(LocalDateTime.now());
        return n;
    }

}
