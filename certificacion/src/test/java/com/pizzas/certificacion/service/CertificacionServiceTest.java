package com.pizzas.certificacion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.pizzas.certificacion.dto.CertificacionRequestDTO;
import com.pizzas.certificacion.dto.CertificacionResponseDTO;
import com.pizzas.certificacion.dto.RepartoDTO;
import com.pizzas.certificacion.dto.ResenaDTO;
import com.pizzas.certificacion.exception.ExcepcionPersonalizada;
import com.pizzas.certificacion.model.Certificacion;
import com.pizzas.certificacion.repository.CertificacionRepository;

// Pruebas unitarias para CertificacionService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class CertificacionServiceTest {

    @Mock
    private CertificacionRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CertificacionService service;

    private Certificacion certificacion;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "URL_PEDIDOS", "http://pedido/");
        ReflectionTestUtils.setField(service, "URL_REPARTO", "http://reparto/");
        ReflectionTestUtils.setField(service, "URL_RESENAS", "http://resenas/");

        certificacion = crearEntidad();
    }

    @Test
    void generarCertificacion_CuandoDatosValidos_DebeGenerarCertificacion() {
        CertificacionRequestDTO request = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoValido());
        when(restTemplate.getForObject("http://reparto/todos", RepartoDTO[].class)).thenReturn(repartos());
        when(restTemplate.getForObject("http://resenas/pedido/1", ResenaDTO[].class)).thenReturn(resenas());

        when(repository.save(any(Certificacion.class))).thenAnswer(invocation -> {
            Certificacion guardada = invocation.getArgument(0);
            guardada.setId(1);
            return guardada;
        });

        CertificacionResponseDTO respuesta = service.generarCertificacion(request);

        assertEquals(1, respuesta.getId());
        assertEquals(1, respuesta.getPedidoId());
        assertEquals(1, respuesta.getUsuarioId());
        assertEquals("Juan Perez", respuesta.getNombreUsuario());
        assertEquals("juan@mail.com", respuesta.getEmailUsuario());
        assertEquals("Pizza napolitana x2", respuesta.getDetalleProductos());
        assertEquals(2, respuesta.getCantidadTotalItems());
        assertEquals(19980, respuesta.getMontoTotal());
        assertEquals("DEBITO", respuesta.getMetodoPago());
        assertEquals("ENTREGADO", respuesta.getEstadoPedido());
        assertEquals("Pedro", respuesta.getNombreRepartidor());
        assertEquals("A TIEMPO", respuesta.getEstadoPuntualidad());
        assertEquals(true, respuesta.getTieneResena());
        assertEquals("Pizza napolitana: Excelente servicio", respuesta.getComentarioResena());
        assertEquals(5, respuesta.getEstrellasResena());
        assertEquals("Certificación generada correctamente", respuesta.getMensaje());

        verify(repository).save(any(Certificacion.class));
    }

    @Test
    void generarCertificacion_CuandoYaExisteCertificacion_DebeLanzarExcepcion() {
        CertificacionRequestDTO request = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(true);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.generarCertificacion(request)
        );

        assertEquals("Ya existe una certificación para el pedido con ID: 1", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());

        verify(repository, never()).save(any(Certificacion.class));
    }

    @Test
    void generarCertificacion_CuandoPedidoCancelado_DebeLanzarExcepcion() {
        CertificacionRequestDTO request = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoCancelado());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.generarCertificacion(request)
        );

        assertEquals("No se puede certificar un pedido en estado: CANCELADO", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(repository, never()).save(any(Certificacion.class));
    }

    @Test
    void generarCertificacion_CuandoPedidoSinCantidadValida_DebeLanzarExcepcion() {
        CertificacionRequestDTO request = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoSinCantidad());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.generarCertificacion(request)
        );

        assertEquals("El pedido no tiene una cantidad de productos válida.", ex.getMessage());
        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());

        verify(repository, never()).save(any(Certificacion.class));
    }

    @Test
    void generarCertificacion_CuandoNoHayRepartoNiResena_DebeGenerarConValoresPorDefecto() {
        CertificacionRequestDTO request = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoValido());
        when(restTemplate.getForObject("http://reparto/todos", RepartoDTO[].class)).thenReturn(new RepartoDTO[] {});
        when(restTemplate.getForObject("http://resenas/pedido/1", ResenaDTO[].class)).thenReturn(new ResenaDTO[] {});

        when(repository.save(any(Certificacion.class))).thenAnswer(invocation -> {
            Certificacion guardada = invocation.getArgument(0);
            guardada.setId(1);
            return guardada;
        });

        CertificacionResponseDTO respuesta = service.generarCertificacion(request);

        assertEquals("SIN ASIGNAR", respuesta.getNombreRepartidor());
        assertEquals("SIN INFORMACION", respuesta.getHoraEntrega());
        assertEquals("SIN INFORMACION", respuesta.getEstadoPuntualidad());
        assertEquals(false, respuesta.getTieneResena());
        assertNull(respuesta.getComentarioResena());
        assertEquals(0, respuesta.getEstrellasResena());
    }

    @Test
    void listarTodas_DebeRetornarCertificaciones() {
        when(repository.findAll()).thenReturn(List.of(certificacion));

        List<CertificacionResponseDTO> respuesta = service.listarTodas();

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getId());
        assertEquals("ENTREGADO", respuesta.get(0).getEstadoPedido());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarCertificacion() {
        when(repository.findById(1)).thenReturn(Optional.of(certificacion));

        CertificacionResponseDTO respuesta = service.buscarPorId(1);

        assertEquals(1, respuesta.getId());
        assertEquals(1, respuesta.getPedidoId());
        assertEquals("Juan Perez", respuesta.getNombreUsuario());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorId(99)
        );

        assertEquals("Certificación no encontrada con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorPedido_CuandoExiste_DebeRetornarCertificacion() {
        when(repository.findByPedidoId(1)).thenReturn(Optional.of(certificacion));

        CertificacionResponseDTO respuesta = service.buscarPorPedido(1);

        assertEquals(1, respuesta.getPedidoId());
        assertEquals("Certificación encontrada", respuesta.getMensaje());
    }

    @Test
    void buscarPorPedido_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findByPedidoId(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorPedido(99)
        );

        assertEquals("No existe certificación para el pedido con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorUsuario_CuandoTieneCertificaciones_DebeRetornarLista() {
        when(repository.findByUsuarioId(1)).thenReturn(List.of(certificacion));

        List<CertificacionResponseDTO> respuesta = service.buscarPorUsuario(1);

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getUsuarioId());
    }

    @Test
    void buscarPorUsuario_CuandoNoTieneCertificaciones_DebeLanzarExcepcion() {
        when(repository.findByUsuarioId(99)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorUsuario(99)
        );

        assertEquals("No existen certificaciones para el usuario con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorEstadoPedido_CuandoExiste_DebeRetornarLista() {
        when(repository.findByEstadoPedido("ENTREGADO")).thenReturn(List.of(certificacion));

        List<CertificacionResponseDTO> respuesta = service.buscarPorEstadoPedido("entregado");

        assertEquals(1, respuesta.size());
        assertEquals("ENTREGADO", respuesta.get(0).getEstadoPedido());
    }

    @Test
    void buscarPorEstadoPedido_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findByEstadoPedido("CANCELADO")).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorEstadoPedido("CANCELADO")
        );

        assertEquals("No existen certificaciones con estado de pedido: CANCELADO", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorTieneResena_CuandoExiste_DebeRetornarLista() {
        when(repository.findByTieneResena(true)).thenReturn(List.of(certificacion));

        List<CertificacionResponseDTO> respuesta = service.buscarPorTieneResena(true);

        assertEquals(1, respuesta.size());
        assertEquals(true, respuesta.get(0).getTieneResena());
    }

    @Test
    void buscarPorTieneResena_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findByTieneResena(false)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorTieneResena(false)
        );

        assertEquals("No existen certificaciones con ese filtro de reseña.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void eliminar_CuandoExiste_DebeEliminarCertificacion() {
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

        assertEquals("No existe la certificación para eliminar.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(repository, never()).deleteById(99);
    }

    private CertificacionRequestDTO crearRequest() {
        CertificacionRequestDTO request = new CertificacionRequestDTO();
        request.setPedidoId(1);
        return request;
    }

    private Certificacion crearEntidad() {
        Certificacion c = new Certificacion();
        c.setId(1);
        c.setPedidoId(1);
        c.setUsuarioId(1);
        c.setNombreUsuario("Juan Perez");
        c.setEmailUsuario("juan@mail.com");
        c.setFechaPedido("2026-06-29 17:00:00");
        c.setDetalleProductos("Pizza napolitana x2");
        c.setCantidadTotalItems(2);
        c.setMontoTotal(19980);
        c.setMetodoPago("DEBITO");
        c.setEstadoPedido("ENTREGADO");
        c.setNombreRepartidor("Pedro");
        c.setHoraEntrega("2026-06-29 17:30:00");
        c.setEstadoPuntualidad("A TIEMPO");
        c.setTieneResena(true);
        c.setComentarioResena("Pizza napolitana: Excelente servicio");
        c.setEstrellasResena(5);
        c.setFechaEmision("2026-06-29 18:00:00");
        return c;
    }

    private Map<String, Object> respuestaPedidoValido() {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", 1);
        pedido.put("usuarioId", 1);
        pedido.put("nombreCliente", "Juan Perez");
        pedido.put("emailCliente", "juan@mail.com");
        pedido.put("montoTotal", 19980);
        pedido.put("cantidadTotalItems", 2);
        pedido.put("detalleProductos", "Pizza napolitana x2");
        pedido.put("estado", "ENTREGADO");
        pedido.put("fechaPedido", "2026-06-29 17:00:00");
        pedido.put("metodoPago", "DEBITO");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("pedido", pedido);
        return respuesta;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> respuestaPedidoCancelado() {
        Map<String, Object> respuesta = respuestaPedidoValido();
        Map<String, Object> pedido = (Map<String, Object>) respuesta.get("pedido");
        pedido.put("estado", "CANCELADO");
        return respuesta;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> respuestaPedidoSinCantidad() {
        Map<String, Object> respuesta = respuestaPedidoValido();
        Map<String, Object> pedido = (Map<String, Object>) respuesta.get("pedido");
        pedido.put("cantidadTotalItems", 0);
        return respuesta;
    }

    private RepartoDTO[] repartos() {
        RepartoDTO reparto = new RepartoDTO(
                1L,
                1,
                1,
                "Juan Perez",
                "juan@mail.com",
                "Av Siempre Viva 123",
                "ENTREGADO",
                "Pedro",
                null,
                "2026-06-29 17:30:00",
                null
        );
        return new RepartoDTO[] { reparto };
    }

    private ResenaDTO[] resenas() {
        ResenaDTO resena = new ResenaDTO(
                1,
                1,
                1,
                "Juan Perez",
                "Pizza napolitana",
                "Excelente servicio",
                5
        );
        return new ResenaDTO[] { resena };
    }

}
