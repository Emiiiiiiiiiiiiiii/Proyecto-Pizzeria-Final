package com.pizzas.carrito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.pizzas.carrito.dto.CarritoDTO;
import com.pizzas.carrito.dto.CarritoDetalleDTO;
import com.pizzas.carrito.dto.ProductoDTO;
import com.pizzas.carrito.exception.ExcepcionPersonalizada;
import com.pizzas.carrito.model.Carrito;
import com.pizzas.carrito.repository.CarritoRepository;

// Pruebas unitarias para CarritoService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class CarritoServiceTest {

    @Mock
    private CarritoRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CarritoService carritoService;

    private Carrito item;
    private ProductoDTO producto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(carritoService, "AUTH_URL", "http://auth/");
        ReflectionTestUtils.setField(carritoService, "CATALOGO_URL", "http://catalogo/");

        item = new Carrito();
        item.setId(1);
        item.setUsuarioId(1);
        item.setCatalogoId(1);
        item.setCantidad(2);
        item.setPrecioUnitario(12990);
        item.setPrecioTotal(25980);

        producto = new ProductoDTO();
        producto.setId(1);
        producto.setNombre("Pepperoni");
        producto.setPrecio(12990);
        producto.setTamanio("Familiar");
    }

    @Test
    void agregarAlCarrito_CuandoProductoNoExiste_DebeGuardarNuevoItem() {
        CarritoDTO dto = new CarritoDTO(1, 1, 2);

        when(restTemplate.getForObject("http://auth/1", Map.class)).thenReturn(usuarioMap());
        when(restTemplate.getForObject("http://catalogo/1", ProductoDTO.class)).thenReturn(producto);
        when(repository.findByUsuarioIdAndCatalogoId(1, 1)).thenReturn(Optional.empty());

        when(repository.save(any(Carrito.class))).thenAnswer(invocation -> {
            Carrito guardado = invocation.getArgument(0);
            guardado.setId(1);
            return guardado;
        });

        Carrito respuesta = carritoService.agregarAlCarrito(dto);

        assertEquals(1, respuesta.getUsuarioId());
        assertEquals(1, respuesta.getCatalogoId());
        assertEquals(2, respuesta.getCantidad());
        assertEquals(12990, respuesta.getPrecioUnitario());
        assertEquals(25980, respuesta.getPrecioTotal());

        verify(repository).save(any(Carrito.class));
    }

    @Test
    void agregarAlCarrito_CuandoProductoYaExiste_DebeAumentarCantidad() {
        CarritoDTO dto = new CarritoDTO(1, 1, 3);

        when(restTemplate.getForObject("http://auth/1", Map.class)).thenReturn(usuarioMap());
        when(restTemplate.getForObject("http://catalogo/1", ProductoDTO.class)).thenReturn(producto);
        when(repository.findByUsuarioIdAndCatalogoId(1, 1)).thenReturn(Optional.of(item));
        when(repository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito respuesta = carritoService.agregarAlCarrito(dto);

        assertEquals(5, respuesta.getCantidad());
        assertEquals(64950, respuesta.getPrecioTotal());

        verify(repository).save(any(Carrito.class));
    }

    @Test
    void agregarAlCarrito_CuandoCantidadSupera50_DebeLanzarExcepcion() {
        item.setCantidad(49);

        CarritoDTO dto = new CarritoDTO(1, 1, 2);

        when(restTemplate.getForObject("http://auth/1", Map.class)).thenReturn(usuarioMap());
        when(restTemplate.getForObject("http://catalogo/1", ProductoDTO.class)).thenReturn(producto);
        when(repository.findByUsuarioIdAndCatalogoId(1, 1)).thenReturn(Optional.of(item));

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> carritoService.agregarAlCarrito(dto)
        );

        assertEquals("No puedes tener más de 50 unidades del mismo producto en el carrito.", ex.getMessage());

        verify(repository, never()).save(any(Carrito.class));
    }

    @Test
    void listarDetalladoPorUsuario_CuandoTieneProductos_DebeRetornarDetalle() {
        when(restTemplate.getForObject("http://auth/1", Map.class)).thenReturn(usuarioMap());
        when(repository.findByUsuarioId(1)).thenReturn(List.of(item));
        when(restTemplate.getForObject("http://catalogo/1", ProductoDTO.class)).thenReturn(producto);

        List<CarritoDetalleDTO> respuesta = carritoService.listarDetalladoPorUsuario(1);

        assertEquals(1, respuesta.size());
        assertEquals("Juan", respuesta.get(0).getNombreUsuario());
        assertEquals("Pepperoni", respuesta.get(0).getNombrePizza());
        assertEquals("Familiar", respuesta.get(0).getTamanio());
        assertEquals(25980, respuesta.get(0).getPrecioTotal());
    }

    @Test
    void listarDetalladoPorUsuario_CuandoCarritoVacio_DebeLanzarExcepcion() {
        when(restTemplate.getForObject("http://auth/1", Map.class)).thenReturn(usuarioMap());
        when(repository.findByUsuarioId(1)).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> carritoService.listarDetalladoPorUsuario(1)
        );

        assertEquals("No se encontraron productos en el carrito del usuario con ID: 1", ex.getMessage());
    }

    @Test
    void listarTodos_DebeRetornarItems() {
        when(repository.findAll()).thenReturn(List.of(item));

        List<Carrito> respuesta = carritoService.listarTodos();

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getId());
        assertEquals(25980, respuesta.get(0).getPrecioTotal());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarItem() {
        when(repository.findById(1)).thenReturn(Optional.of(item));

        Carrito respuesta = carritoService.buscarPorId(1);

        assertEquals(1, respuesta.getId());
        assertEquals(2, respuesta.getCantidad());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> carritoService.buscarPorId(99)
        );

        assertEquals("El item del carrito con ID 99 no existe.", ex.getMessage());
    }

    @Test
    void actualizarCantidad_CuandoCantidadValida_DebeActualizar() {
        when(repository.findById(1)).thenReturn(Optional.of(item));
        when(repository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito respuesta = carritoService.actualizarCantidad(1, 4);

        assertEquals(4, respuesta.getCantidad());
        assertEquals(51960, respuesta.getPrecioTotal());

        verify(repository).save(any(Carrito.class));
    }

    @Test
    void actualizarCantidad_CuandoCantidadInvalida_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> carritoService.actualizarCantidad(1, 0)
        );

        assertEquals("La cantidad debe ser mayor o igual a 1.", ex.getMessage());

        verify(repository, never()).save(any(Carrito.class));
    }

    @Test
    void eliminarDelCarrito_CuandoExiste_DebeEliminar() {
        when(repository.existsById(1)).thenReturn(true);

        carritoService.eliminarDelCarrito(1);

        verify(repository).deleteById(1);
    }

    @Test
    void eliminarDelCarrito_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.existsById(99)).thenReturn(false);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> carritoService.eliminarDelCarrito(99)
        );

        assertEquals("No se puede eliminar. El item del carrito no existe.", ex.getMessage());

        verify(repository, never()).deleteById(99);
    }

    @Test
    void vaciarCarritoPorUsuario_CuandoTieneProductos_DebeVaciarCarrito() {
        when(restTemplate.getForObject("http://auth/1", Map.class)).thenReturn(usuarioMap());
        when(repository.findByUsuarioId(1)).thenReturn(List.of(item));

        carritoService.vaciarCarritoPorUsuario(1);

        verify(repository).deleteByUsuarioId(1);
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
