package com.pizzas.pedido.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.pizzas.pedido.dto.CarritoDetalleDTO;
import com.pizzas.pedido.dto.PedidoRequestDTO;
import com.pizzas.pedido.dto.PedidoResponseDTO;
import com.pizzas.pedido.exception.ExcepcionPersonalizada;
import com.pizzas.pedido.model.Pedido;
import com.pizzas.pedido.repository.PedidoRepository;

// Pruebas unitarias para PedidoService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

     @Mock
    private PedidoRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pedidoService, "URL_AUTH", "http://auth/");
        ReflectionTestUtils.setField(pedidoService, "URL_CARRITO", "http://carrito/");
        ReflectionTestUtils.setField(pedidoService, "URL_INVENTARIO", "http://inventario/");
        ReflectionTestUtils.setField(pedidoService, "URL_PAGOS", "http://pagos/");
        ReflectionTestUtils.setField(pedidoService, "URL_NOTIFICACIONES", "http://notificaciones/");

        pedido = new Pedido();
        pedido.setId(1);
        pedido.setUsuarioId(1);
        pedido.setNombreCliente("Juan Perez");
        pedido.setEmailCliente("juan@mail.com");
        pedido.setMontoTotal(25980);
        pedido.setCantidadTotalItems(2);
        pedido.setDetalleProductos("2x Pepperoni Familiar");
        pedido.setEstado("PAGADO");
        pedido.setFechaPedido("2026-06-29 10:00:00");
        pedido.setMetodoPago("DEBITO");
    }

    @Test
    void crearPedido_CuandoDatosValidos_DebeCrearPedidoPagado() {
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setUsuarioId(1);
        request.setMetodoPago("debito");

        when(restTemplate.getForObject("http://auth/usuarios/1", Map.class)).thenReturn(usuarioMap());

        when(restTemplate.exchange(
                eq("http://carrito/usuario/1"),
                eq(HttpMethod.GET),
                eq(null),
                ArgumentMatchers.<ParameterizedTypeReference<List<CarritoDetalleDTO>>>any()
        )).thenReturn(ResponseEntity.ok(List.of(itemCarrito())));

        when(repository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido guardado = invocation.getArgument(0);
            guardado.setId(1);
            return guardado;
        });

        when(restTemplate.postForObject(eq("http://pagos/procesar"), any(), eq(Map.class)))
                .thenReturn(Map.of("status", "APROBADO"));

        PedidoResponseDTO respuesta = pedidoService.crearPedido(request);

        assertEquals(1, respuesta.getId());
        assertEquals("Juan Perez", respuesta.getNombreCliente());
        assertEquals(25980, respuesta.getMontoTotal());
        assertEquals("PAGADO", respuesta.getEstado());
        assertEquals("2x Pepperoni Familiar", respuesta.getDetalleProductos());
        assertEquals("Pedido completado correctamente", respuesta.getMensaje());

        verify(repository, org.mockito.Mockito.times(2)).save(any(Pedido.class));
    }

    @Test
    void crearPedido_CuandoPagoEsRechazado_DebeLanzarExcepcion() {
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setUsuarioId(1);
        request.setMetodoPago("DEBITO");

        when(restTemplate.getForObject("http://auth/usuarios/1", Map.class)).thenReturn(usuarioMap());

        when(restTemplate.exchange(
                eq("http://carrito/usuario/1"),
                eq(HttpMethod.GET),
                eq(null),
                ArgumentMatchers.<ParameterizedTypeReference<List<CarritoDetalleDTO>>>any()
        )).thenReturn(ResponseEntity.ok(List.of(itemCarrito())));

        when(repository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido guardado = invocation.getArgument(0);
            guardado.setId(1);
            return guardado;
        });

        when(restTemplate.postForObject(eq("http://pagos/procesar"), any(), eq(Map.class)))
                .thenReturn(Map.of("status", "RECHAZADO"));

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pedidoService.crearPedido(request)
        );

        assertEquals("El pago fue rechazado.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(repository, org.mockito.Mockito.times(2)).save(any(Pedido.class));
    }

    @Test
    void listarPedidos_DebeRetornarLista() {
        when(repository.findAll()).thenReturn(List.of(pedido));

        List<Pedido> respuesta = pedidoService.listarPedidos();

        assertEquals(1, respuesta.size());
        assertEquals("Juan Perez", respuesta.get(0).getNombreCliente());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarPedido() {
        when(repository.findById(1)).thenReturn(Optional.of(pedido));

        Pedido respuesta = pedidoService.buscarPorId(1);

        assertEquals(1, respuesta.getId());
        assertEquals("PAGADO", respuesta.getEstado());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pedidoService.buscarPorId(99)
        );

        assertEquals("Pedido no encontrado con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorUsuario_CuandoTienePedidos_DebeRetornarLista() {
        when(repository.findByUsuarioId(1)).thenReturn(List.of(pedido));

        List<Pedido> respuesta = pedidoService.buscarPorUsuario(1);

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getUsuarioId());
    }

    @Test
    void buscarPorUsuario_CuandoNoTienePedidos_DebeLanzarExcepcion() {
        when(repository.findByUsuarioId(99)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pedidoService.buscarPorUsuario(99)
        );

        assertEquals("No existen pedidos para el usuario con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorEstado_CuandoEstadoExiste_DebeRetornarLista() {
        when(repository.findByEstado("PAGADO")).thenReturn(List.of(pedido));

        List<Pedido> respuesta = pedidoService.buscarPorEstado("pagado");

        assertEquals(1, respuesta.size());
        assertEquals("PAGADO", respuesta.get(0).getEstado());
    }

    @Test
    void buscarPorEstado_CuandoEstadoInvalido_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pedidoService.buscarPorEstado("MALO")
        );

        assertEquals("Estado de pedido no válido: MALO", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void actualizarPedido_CuandoExiste_DebeActualizarEstado() {
        when(repository.findById(1)).thenReturn(Optional.of(pedido));
        when(repository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido respuesta = pedidoService.actualizarPedido(1, "entregado");

        assertEquals("ENTREGADO", respuesta.getEstado());
        verify(repository).save(any(Pedido.class));
    }

    @Test
    void eliminarPedido_CuandoExiste_DebeEliminar() {
        when(repository.existsById(1)).thenReturn(true);

        pedidoService.eliminarPedido(1);

        verify(repository).deleteById(1);
    }

    @Test
    void eliminarPedido_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.existsById(99)).thenReturn(false);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> pedidoService.eliminarPedido(99)
        );

        assertEquals("No existe el pedido para eliminar.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(repository, never()).deleteById(99);
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

    private CarritoDetalleDTO itemCarrito() {
        CarritoDetalleDTO item = new CarritoDetalleDTO();
        item.setId(1);
        item.setCatalogoId(1);
        item.setNombreUsuario("Juan");
        item.setNombrePizza("Pepperoni");
        item.setTamanio("Familiar");
        item.setCantidad(2);
        item.setPrecioUnitario(12990);
        item.setPrecioTotal(25980);
        return item;
    }

}
