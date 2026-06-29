package com.pizzas.resenas.service;

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

import com.pizzas.resenas.dto.ResenaRequestDTO;
import com.pizzas.resenas.dto.ResenaResponseDTO;
import com.pizzas.resenas.dto.ResenaUpdateDTO;
import com.pizzas.resenas.exception.ExcepcionPersonalizada;
import com.pizzas.resenas.model.Resena;
import com.pizzas.resenas.repository.ResenaRepository;

// Pruebas unitarias para ResenaService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class ResenaServiceTest {

    @Mock
    private ResenaRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ResenaService service;

    private Resena resena;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "URL_PEDIDOS", "http://pedido/");
        ReflectionTestUtils.setField(service, "URL_AUTH", "http://auth/usuarios/");
        ReflectionTestUtils.setField(service, "URL_NOTIFICACIONES", "http://notificaciones/");

        resena = crearEntidad();
    }

    @Test
    void crearResena_CuandoDatosValidos_DebeCrearResena() {
        ResenaRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoConEmail());

        when(repository.save(any(Resena.class))).thenAnswer(invocation -> {
            Resena guardada = invocation.getArgument(0);
            guardada.setId(1);
            return guardada;
        });

        ResenaResponseDTO respuesta = service.crearResena(dto);

        assertEquals(1, respuesta.getId());
        assertEquals(1, respuesta.getPedidoId());
        assertEquals(1, respuesta.getUsuarioId());
        assertEquals("Juan Perez", respuesta.getClienteNombre());
        assertEquals("Pizza familiar napolitana", respuesta.getNombreProducto());
        assertEquals("Muy rica la pizza", respuesta.getComentario());
        assertEquals(5, respuesta.getEstrellas());
        assertNotNull(respuesta.getFechaResena());

        verify(repository).save(any(Resena.class));
    }

    @Test
    void crearResena_CuandoPedidoYaTieneResena_DebeLanzarExcepcion() {
        ResenaRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(true);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.crearResena(dto)
        );

        assertEquals("El pedido con ID 1 ya tiene una reseña registrada.", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());

        verify(repository, never()).save(any(Resena.class));
    }

    @Test
    void crearResena_CuandoUsuarioNoCoincide_DebeLanzarExcepcion() {
        ResenaRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoOtroUsuario());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.crearResena(dto)
        );

        assertEquals("El usuario enviado no coincide con el usuario del pedido.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(repository, never()).save(any(Resena.class));
    }

    @Test
    void crearResena_CuandoPedidoCancelado_DebeLanzarExcepcion() {
        ResenaRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoCancelado());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.crearResena(dto)
        );

        assertEquals("No se puede crear reseña para un pedido en estado: CANCELADO", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(repository, never()).save(any(Resena.class));
    }

    @Test
    void crearResena_CuandoPedidoNoTraeNombreNiEmail_DebeBuscarUsuarioYCrear() {
        ResenaRequestDTO dto = crearRequest();

        when(repository.existsByPedidoId(1)).thenReturn(false);
        when(restTemplate.getForObject("http://pedido/1", Map.class)).thenReturn(respuestaPedidoSinNombreNiEmail());
        when(restTemplate.getForObject("http://auth/usuarios/1", Map.class)).thenReturn(respuestaUsuario());

        when(repository.save(any(Resena.class))).thenAnswer(invocation -> {
            Resena guardada = invocation.getArgument(0);
            guardada.setId(1);
            return guardada;
        });

        ResenaResponseDTO respuesta = service.crearResena(dto);

        assertEquals("Pedro Gomez", respuesta.getClienteNombre());
        assertEquals("Pedido #1", respuesta.getNombreProducto());
        assertEquals(5, respuesta.getEstrellas());

        verify(repository).save(any(Resena.class));
    }

    @Test
    void listarTodas_DebeRetornarResenas() {
        when(repository.findAll()).thenReturn(List.of(resena));

        List<ResenaResponseDTO> respuesta = service.listarTodas();

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getId());
        assertEquals("Muy rica la pizza", respuesta.get(0).getComentario());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarResena() {
        when(repository.findById(1)).thenReturn(Optional.of(resena));

        ResenaResponseDTO respuesta = service.buscarPorId(1);

        assertEquals(1, respuesta.getId());
        assertEquals(1, respuesta.getPedidoId());
        assertEquals(5, respuesta.getEstrellas());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorId(99)
        );

        assertEquals("Reseña no encontrada con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorPedido_CuandoExiste_DebeRetornarLista() {
        when(repository.findByPedidoId(1)).thenReturn(Optional.of(resena));

        List<ResenaResponseDTO> respuesta = service.buscarPorPedido(1);

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getPedidoId());
    }

    @Test
    void buscarPorPedido_CuandoNoExiste_DebeRetornarListaVacia() {
        when(repository.findByPedidoId(99)).thenReturn(Optional.empty());

        List<ResenaResponseDTO> respuesta = service.buscarPorPedido(99);

        assertEquals(0, respuesta.size());
    }

    @Test
    void buscarPorUsuario_CuandoTieneResenas_DebeRetornarLista() {
        when(repository.findByUsuarioId(1)).thenReturn(List.of(resena));

        List<ResenaResponseDTO> respuesta = service.buscarPorUsuario(1);

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getUsuarioId());
    }

    @Test
    void buscarPorUsuario_CuandoNoTieneResenas_DebeLanzarExcepcion() {
        when(repository.findByUsuarioId(99)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorUsuario(99)
        );

        assertEquals("No existen reseñas para el usuario con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorEstrellas_CuandoExisten_DebeRetornarLista() {
        when(repository.findByEstrellas(5)).thenReturn(List.of(resena));

        List<ResenaResponseDTO> respuesta = service.buscarPorEstrellas(5);

        assertEquals(1, respuesta.size());
        assertEquals(5, respuesta.get(0).getEstrellas());
    }

    @Test
    void buscarPorEstrellas_CuandoValorInvalido_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorEstrellas(6)
        );

        assertEquals("Las estrellas deben estar entre 1 y 5.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void buscarPorEstrellas_CuandoNoExisten_DebeLanzarExcepcion() {
        when(repository.findByEstrellas(2)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.buscarPorEstrellas(2)
        );

        assertEquals("No existen reseñas con 2 estrellas.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void actualizar_CuandoExiste_DebeActualizarResena() {
        ResenaUpdateDTO dto = crearUpdate();

        when(repository.findById(1)).thenReturn(Optional.of(resena));
        when(repository.save(any(Resena.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResenaResponseDTO respuesta = service.actualizar(1, dto);

        assertEquals("Comentario actualizado", respuesta.getComentario());
        assertEquals(4, respuesta.getEstrellas());

        verify(repository).save(resena);
    }

    @Test
    void actualizar_CuandoNoExiste_DebeLanzarExcepcion() {
        ResenaUpdateDTO dto = crearUpdate();

        when(repository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> service.actualizar(99, dto)
        );

        assertEquals("Reseña no encontrada con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void eliminar_CuandoExiste_DebeEliminar() {
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

        assertEquals("No existe la reseña para eliminar.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(repository, never()).deleteById(99);
    }

    private Resena crearEntidad() {
        Resena r = new Resena();
        r.setId(1);
        r.setPedidoId(1);
        r.setUsuarioId(1);
        r.setClienteNombre("Juan Perez");
        r.setNombreProducto("Pizza familiar napolitana");
        r.setComentario("Muy rica la pizza");
        r.setEstrellas(5);
        r.setFechaResena(LocalDateTime.now());
        return r;
    }

    private ResenaRequestDTO crearRequest() {
        ResenaRequestDTO dto = new ResenaRequestDTO();
        dto.setPedidoId(1);
        dto.setUsuarioId(1);
        dto.setComentario(" Muy rica la pizza ");
        dto.setEstrellas(5);
        return dto;
    }

    private ResenaUpdateDTO crearUpdate() {
        ResenaUpdateDTO dto = new ResenaUpdateDTO();
        dto.setComentario(" Comentario actualizado ");
        dto.setEstrellas(4);
        return dto;
    }

    private Map<String, Object> respuestaPedidoConEmail() {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", 1);
        pedido.put("usuarioId", 1);
        pedido.put("nombreCliente", "Juan Perez");
        pedido.put("emailCliente", "juan@mail.com");
        pedido.put("detalleProductos", "Pizza familiar napolitana");
        pedido.put("estado", "ENTREGADO");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("pedido", pedido);
        return respuesta;
    }

    private Map<String, Object> respuestaPedidoOtroUsuario() {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", 1);
        pedido.put("usuarioId", 2);
        pedido.put("nombreCliente", "Otro Usuario");
        pedido.put("emailCliente", "otro@mail.com");
        pedido.put("detalleProductos", "Pizza");
        pedido.put("estado", "ENTREGADO");

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
        pedido.put("detalleProductos", "Pizza");
        pedido.put("estado", "CANCELADO");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("pedido", pedido);
        return respuesta;
    }

    private Map<String, Object> respuestaPedidoSinNombreNiEmail() {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", 1);
        pedido.put("usuarioId", 1);
        pedido.put("estado", "ENTREGADO");
        return pedido;
    }

    private Map<String, Object> respuestaUsuario() {
        Map<String, Object> usuario = new LinkedHashMap<>();
        usuario.put("id", 1);
        usuario.put("nombre", "Pedro");
        usuario.put("apellido", "Gomez");
        usuario.put("email", "pedro@mail.com");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("usuario", usuario);
        return respuesta;
    }

}
