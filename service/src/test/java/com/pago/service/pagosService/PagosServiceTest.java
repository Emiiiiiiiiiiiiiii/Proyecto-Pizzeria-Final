package com.pago.service.pagosService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.pago.service.ExcepcionPersonalizada;
import com.pago.service.dto.PagosDTO;
import com.pago.service.model.Pagos;
import com.pago.service.pagosService.PagosService;
import com.pago.service.repository.PagosRepository;

// Pruebas unitarias para PagosService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class PagosServiceTest {
     @Mock
    private PagosRepository pagosRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PagosService pagosService;

    private Pagos pago;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagosService, "URL_AUTH", "http://auth/");
        ReflectionTestUtils.setField(pagosService, "URL_NOTIFICACIONES", "http://notificaciones/");

        pago = new Pagos();
        pago.setId(1);
        pago.setMonto(25980);
        pago.setEstado("APROBADO");
        pago.setPedidoId(1);
        pago.setUsuarioId(1);
        pago.setFechaPago(LocalDateTime.now());
    }

    @Test
    void generarPago_CuandoDatosValidos_DebeAprobarPago() {
        PagosDTO dto = crearDTO();

        when(pagosRepository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://auth/usuarios/1", Map.class)).thenReturn(usuarioMap());

        when(pagosRepository.save(any(Pagos.class))).thenAnswer(invocation -> {
            Pagos guardado = invocation.getArgument(0);
            guardado.setId(1);
            return guardado;
        });

        Map<String, Object> respuesta = pagosService.generarPago(dto);

        assertEquals("APROBADO", respuesta.get("status"));
        assertEquals("Pago aprobado correctamente", respuesta.get("mensaje"));
        assertEquals(1, respuesta.get("idPago"));
        assertEquals(25980, respuesta.get("montoAprobado"));
        assertEquals(1, respuesta.get("pedidoId"));
        assertEquals(1, respuesta.get("usuarioId"));
        assertEquals("Juan", respuesta.get("nombreUsuario"));
        assertNotNull(respuesta.get("pago"));

        verify(pagosRepository).save(any(Pagos.class));
    }

    @Test
    void generarPago_CuandoPedidoYaTienePago_DebeLanzarExcepcion() {
        PagosDTO dto = crearDTO();

        when(pagosRepository.existsByPedidoId(1)).thenReturn(true);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pagosService.generarPago(dto)
        );

        assertEquals("El pedido con ID 1 ya tiene un pago registrado.", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());

        verify(pagosRepository, never()).save(any(Pagos.class));
    }

    @Test
    void listarTodos_DebeRetornarPagos() {
        when(pagosRepository.findAll()).thenReturn(List.of(pago));

        List<Pagos> respuesta = pagosService.listarTodos();

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getId());
        assertEquals("APROBADO", respuesta.get(0).getEstado());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarPago() {
        when(pagosRepository.findById(1)).thenReturn(Optional.of(pago));

        Pagos respuesta = pagosService.buscarPorId(1);

        assertEquals(1, respuesta.getId());
        assertEquals(25980, respuesta.getMonto());
        assertEquals("APROBADO", respuesta.getEstado());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(pagosRepository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pagosService.buscarPorId(99)
        );

        assertEquals("Pago no encontrado con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorPedido_CuandoExiste_DebeRetornarPago() {
        when(pagosRepository.findByPedidoId(1)).thenReturn(Optional.of(pago));

        Pagos respuesta = pagosService.buscarPorPedido(1);

        assertEquals(1, respuesta.getPedidoId());
        assertEquals("APROBADO", respuesta.getEstado());
    }

    @Test
    void buscarPorPedido_CuandoNoExiste_DebeLanzarExcepcion() {
        when(pagosRepository.findByPedidoId(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pagosService.buscarPorPedido(99)
        );

        assertEquals("No existe pago para el pedido con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorUsuario_CuandoTienePagos_DebeRetornarLista() {
        when(pagosRepository.findByUsuarioId(1)).thenReturn(List.of(pago));

        List<Pagos> respuesta = pagosService.buscarPorUsuario(1);

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getUsuarioId());
    }

    @Test
    void buscarPorUsuario_CuandoNoTienePagos_DebeLanzarExcepcion() {
        when(pagosRepository.findByUsuarioId(99)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pagosService.buscarPorUsuario(99)
        );

        assertEquals("No existen pagos para el usuario con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorEstado_CuandoEstadoExiste_DebeRetornarLista() {
        when(pagosRepository.findByEstado("APROBADO")).thenReturn(List.of(pago));

        List<Pagos> respuesta = pagosService.buscarPorEstado("aprobado");

        assertEquals(1, respuesta.size());
        assertEquals("APROBADO", respuesta.get(0).getEstado());
    }

    @Test
    void buscarPorEstado_CuandoEstadoInvalido_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pagosService.buscarPorEstado("MALO")
        );

        assertEquals("Estado de pago no válido: MALO", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void buscarPorEstado_CuandoNoExistenPagos_DebeLanzarExcepcion() {
        when(pagosRepository.findByEstado("RECHAZADO")).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pagosService.buscarPorEstado("RECHAZADO")
        );

        assertEquals("No existen pagos con estado: RECHAZADO", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void eliminarPorId_CuandoExiste_DebeEliminarPago() {
        when(pagosRepository.existsById(1)).thenReturn(true);

        pagosService.eliminarPorId(1);

        verify(pagosRepository).deleteById(1);
    }

    @Test
    void eliminarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(pagosRepository.existsById(99)).thenReturn(false);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pagosService.eliminarPorId(99)
        );

        assertEquals("No se puede eliminar, el ID de pago no existe.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(pagosRepository, never()).deleteById(99);
    }

    private PagosDTO crearDTO() {
        PagosDTO dto = new PagosDTO();
        dto.setMonto(25980);
        dto.setPedidoId(1);
        dto.setUsuarioId(1);
        return dto;
    }

    private Map<String, Object> usuarioMap() {
        Map<String, Object> usuario = new LinkedHashMap<>();
        usuario.put("id", 1);
        usuario.put("nombre", "Juan");
        usuario.put("apellido", "Perez");
        usuario.put("email", "juan@mail.com");
        usuario.put("rol", "CLIENTE");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("usuario", usuario);

        return respuesta;
    }

}
