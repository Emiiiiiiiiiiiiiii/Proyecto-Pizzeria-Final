package com.servicio.reparto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.servicio.reparto.dto.RepartoRequestDTO;
import com.servicio.reparto.dto.RepartoResponseDTO;
import com.servicio.reparto.exception.ExcepcionPersonalizada;
import com.servicio.reparto.model.Reparto;
import com.servicio.reparto.repository.RepartoRepository;

// Pruebas unitarias para RepartoService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class RepartoServiceTest {
    @Mock
    private RepartoRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RepartoService repartoService;

    private Reparto reparto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(repartoService, "URL_PEDIDOS", "http://pedido/");
        ReflectionTestUtils.setField(repartoService, "URL_AUTH", "http://auth/usuarios/");
        ReflectionTestUtils.setField(repartoService, "URL_NOTIFICACIONES", "http://notificaciones/");

        reparto = crearReparto();
    }

    @Test
    void generarReparto_CuandoDatosValidos_DebeGenerarReparto() {
        RepartoRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoConEmail());

        when(repository.save(any(Reparto.class))).thenAnswer(invocation -> {
            Reparto guardado = invocation.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        RepartoResponseDTO respuesta = repartoService.generarReparto(dto);

        assertEquals(1L, respuesta.getId());
        assertEquals(1, respuesta.getPedidoId());
        assertEquals(1, respuesta.getUsuarioId());
        assertEquals("Juan Perez", respuesta.getNombreCliente());
        assertEquals("juan@mail.com", respuesta.getEmailCliente());
        assertEquals("Av Siempre Viva 123", respuesta.getDireccionEntrega());
        assertEquals("PREPARANDO", respuesta.getEstadoReparto());
        assertEquals("Pedro", respuesta.getRepartidor());
        assertNotNull(respuesta.getHoraEntrega());

        verify(repository).save(any(Reparto.class));
    }

    @Test
    void generarReparto_CuandoPedidoYaTieneReparto_DebeLanzarExcepcion() {
        RepartoRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(true);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.generarReparto(dto)
        );

        assertEquals("El pedido con ID 1 ya tiene reparto asignado.", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());

        verify(repository, never()).save(any(Reparto.class));
    }

    @Test
    void generarReparto_CuandoUsuarioNoCoincide_DebeLanzarExcepcion() {
        RepartoRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoConOtroUsuario());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.generarReparto(dto)
        );

        assertEquals("El usuario enviado no coincide con el usuario del pedido.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(repository, never()).save(any(Reparto.class));
    }

    @Test
    void generarReparto_CuandoPedidoCancelado_DebeLanzarExcepcion() {
        RepartoRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoCancelado());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.generarReparto(dto)
        );

        assertEquals("No se puede generar reparto para un pedido en estado: CANCELADO", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(repository, never()).save(any(Reparto.class));
    }

    @Test
    void generarReparto_CuandoPedidoNoTraeEmail_DebeBuscarUsuarioYGenerar() {
        RepartoRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoSinEmail());
        when(restTemplate.getForObject("http://auth/usuarios/1", Map.class)).thenReturn(respuestaUsuario());

        when(repository.save(any(Reparto.class))).thenAnswer(invocation -> {
            Reparto guardado = invocation.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        RepartoResponseDTO respuesta = repartoService.generarReparto(dto);

        assertEquals("usuario@mail.com", respuesta.getEmailCliente());
        assertEquals("PREPARANDO", respuesta.getEstadoReparto());

        verify(repository).save(any(Reparto.class));
    }

    @Test
    void listarTodos_DebeRetornarRepartos() {
        when(repository.findAll()).thenReturn(List.of(reparto));

        List<RepartoResponseDTO> respuesta = repartoService.listarTodos();

        assertEquals(1, respuesta.size());
        assertEquals(1L, respuesta.get(0).getId());
        assertEquals("EN_CAMINO", respuesta.get(0).getEstadoReparto());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarReparto() {
        when(repository.findById(1L)).thenReturn(Optional.of(reparto));

        RepartoResponseDTO respuesta = repartoService.buscarPorId(1L);

        assertEquals(1L, respuesta.getId());
        assertEquals(1, respuesta.getPedidoId());
        assertEquals("Juan Perez", respuesta.getNombreCliente());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.buscarPorId(99L)
        );

        assertEquals("Reparto no encontrado con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorPedido_CuandoExiste_DebeRetornarReparto() {
        when(repository.findByPedidoId(1)).thenReturn(Optional.of(reparto));

        RepartoResponseDTO respuesta = repartoService.buscarPorPedido(1);

        assertEquals(1, respuesta.getPedidoId());
        assertEquals("EN_CAMINO", respuesta.getEstadoReparto());
    }

    @Test
    void buscarPorPedido_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findByPedidoId(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.buscarPorPedido(99)
        );

        assertEquals("No existe reparto para el pedido con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorUsuario_CuandoTieneRepartos_DebeRetornarLista() {
        when(repository.findByUsuarioId(1)).thenReturn(List.of(reparto));

        List<RepartoResponseDTO> respuesta = repartoService.buscarPorUsuario(1);

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getUsuarioId());
    }

    @Test
    void buscarPorUsuario_CuandoNoTieneRepartos_DebeLanzarExcepcion() {
        when(repository.findByUsuarioId(99)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.buscarPorUsuario(99)
        );

        assertEquals("No existen repartos para el usuario con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorEstado_CuandoEstadoExiste_DebeRetornarLista() {
        when(repository.findByEstadoReparto("EN_CAMINO")).thenReturn(List.of(reparto));

        List<RepartoResponseDTO> respuesta = repartoService.buscarPorEstado("en_camino");

        assertEquals(1, respuesta.size());
        assertEquals("EN_CAMINO", respuesta.get(0).getEstadoReparto());
    }

    @Test
    void buscarPorEstado_CuandoEstadoInvalido_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.buscarPorEstado("MALO")
        );

        assertEquals("Estado de reparto no válido: MALO", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void buscarPorEstado_CuandoNoExistenRepartos_DebeLanzarExcepcion() {
        when(repository.findByEstadoReparto("RETRASADO")).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.buscarPorEstado("RETRASADO")
        );

        assertEquals("No existen repartos con estado: RETRASADO", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void actualizarEstadoReparto_CuandoDatosValidos_DebeActualizarEstado() {
        when(repository.findById(1L)).thenReturn(Optional.of(reparto));
        when(repository.save(any(Reparto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepartoResponseDTO respuesta = repartoService.actualizarEstadoReparto(1L, "ENTREGADO");

        assertEquals("ENTREGADO", respuesta.getEstadoReparto());
        assertNotNull(respuesta.getHoraEntrega());

        verify(repository).save(reparto);
    }

    @Test
    void actualizarEstadoReparto_CuandoEstadoInvalido_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.actualizarEstadoReparto(1L, "MALO")
        );

        assertEquals("Estado de reparto no válido: MALO", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(repository, never()).save(any(Reparto.class));
    }

    @Test
    void actualizarEstadoReparto_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.actualizarEstadoReparto(99L, "ENTREGADO")
        );

        assertEquals("Reparto no encontrado con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void eliminar_CuandoExiste_DebeEliminarReparto() {
        when(repository.existsById(1L)).thenReturn(true);

        repartoService.eliminar(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void eliminar_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.existsById(99L)).thenReturn(false);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> repartoService.eliminar(99L)
        );

        assertEquals("No existe el reparto para eliminar.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(repository, never()).deleteById(99L);
    }

    private Reparto crearReparto() {
        Reparto r = new Reparto();
        r.setId(1L);
        r.setPedidoId(1);
        r.setUsuarioId(1);
        r.setNombreCliente("Juan Perez");
        r.setEmailCliente("juan@mail.com");
        r.setDireccionEntrega("Av Siempre Viva 123");
        r.setEstadoReparto("EN_CAMINO");
        r.setRepartidor("Pedro");
        r.setHoraEntrega("2026-06-29 18:00:00");
        return r;
    }

    private RepartoRequestDTO crearRequest() {
        RepartoRequestDTO dto = new RepartoRequestDTO();
        dto.setPedidoId(1);
        dto.setUsuarioId(1);
        dto.setDireccionEntrega(" Av Siempre Viva 123 ");
        dto.setRepartidor(" Pedro ");
        return dto;
    }

    private Map<String, Object> respuestaPedidoConEmail() {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", 1);
        pedido.put("usuarioId", 1);
        pedido.put("nombreCliente", "Juan Perez");
        pedido.put("emailCliente", "juan@mail.com");
        pedido.put("estado", "PAGADO");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("pedido", pedido);
        return respuesta;
    }

    private Map<String, Object> respuestaPedidoSinEmail() {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", 1);
        pedido.put("usuarioId", 1);
        pedido.put("nombreCliente", "Juan Perez");
        pedido.put("estado", "PAGADO");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("pedido", pedido);
        return respuesta;
    }

    private Map<String, Object> respuestaPedidoConOtroUsuario() {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", 1);
        pedido.put("usuarioId", 2);
        pedido.put("nombreCliente", "Otro Cliente");
        pedido.put("emailCliente", "otro@mail.com");
        pedido.put("estado", "PAGADO");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("pedido", pedido);
        return respuesta;
    }

    private Map<String, Object> respuestaPedidoCancelado() {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", 1);
        pedido.put("usuarioId", 1);
        pedido.put("nombreCliente", "Juan Perez");
        pedido.put("emailCliente", "juan@mail.com");
        pedido.put("estado", "CANCELADO");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("pedido", pedido);
        return respuesta;
    }

    private Map<String, Object> respuestaUsuario() {
        Map<String, Object> usuario = new LinkedHashMap<>();
        usuario.put("id", 1);
        usuario.put("email", "usuario@mail.com");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("usuario", usuario);
        return respuesta;
    }

}
