package com.pizzas.inventario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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

import com.pizzas.inventario.dto.DescuentoInventarioDTO;
import com.pizzas.inventario.dto.InventarioRequestDTO;
import com.pizzas.inventario.dto.InventarioResponseDTO;
import com.pizzas.inventario.exception.ExcepcionPersonalizada;
import com.pizzas.inventario.model.Inventario;
import com.pizzas.inventario.repository.InventarioRepository;

// Pruebas unitarias para InventarioService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class InventarioServiceTest {
    @Mock
    private InventarioRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inventarioService, "URL_CATALOGO", "http://catalogo/");

        inventario = new Inventario();
        inventario.setId(1);
        inventario.setCatalogoId(1);
        inventario.setCantidad(50);
    }

    @Test
    void listar_DebeRetornarInventario() {
        when(repository.findAll()).thenReturn(List.of(inventario));

        List<InventarioResponseDTO> respuesta = inventarioService.listar();

        assertEquals(1, respuesta.size());
        assertEquals(1, respuesta.get(0).getId());
        assertEquals(1, respuesta.get(0).getCatalogoId());
        assertEquals(50, respuesta.get(0).getCantidad());
        assertEquals("DISPONIBLE", respuesta.get(0).getEstadoStock());
    }

    @Test
    void buscarPorCatalogoId_CuandoExiste_DebeRetornarInventario() {
        when(repository.findByCatalogoId(1)).thenReturn(Optional.of(inventario));

        InventarioResponseDTO respuesta = inventarioService.buscarPorCatalogoId(1);

        assertEquals(1, respuesta.getId());
        assertEquals(1, respuesta.getCatalogoId());
        assertEquals(50, respuesta.getCantidad());
        assertEquals("DISPONIBLE", respuesta.getEstadoStock());
    }

    @Test
    void buscarPorCatalogoId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findByCatalogoId(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> inventarioService.buscarPorCatalogoId(99)
        );

        assertEquals("No se encontró stock para el ID de catálogo: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void listarStockBajo_CuandoLimiteValido_DebeRetornarLista() {
        Inventario bajo = new Inventario();
        bajo.setId(2);
        bajo.setCatalogoId(2);
        bajo.setCantidad(10);

        when(repository.findByCantidadLessThan(20)).thenReturn(List.of(bajo));

        List<InventarioResponseDTO> respuesta = inventarioService.listarStockBajo(20);

        assertEquals(1, respuesta.size());
        assertEquals(2, respuesta.get(0).getCatalogoId());
        assertEquals(10, respuesta.get(0).getCantidad());
        assertEquals("STOCK BAJO", respuesta.get(0).getEstadoStock());
    }

    @Test
    void listarStockBajo_CuandoLimiteInvalido_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> inventarioService.listarStockBajo(-1)
        );

        assertEquals("El límite de stock debe ser mayor o igual a 0.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void guardar_CuandoProductoExisteYNoHayDuplicado_DebeGuardarInventario() {
        InventarioRequestDTO dto = crearRequest(1, 50);

        when(restTemplate.getForObject("http://catalogo/pizzas/1", Object.class)).thenReturn(new Object());
        when(repository.existsByCatalogoId(1)).thenReturn(false);

        when(repository.save(any(Inventario.class))).thenAnswer(invocation -> {
            Inventario guardado = invocation.getArgument(0);
            guardado.setId(1);
            return guardado;
        });

        InventarioResponseDTO respuesta = inventarioService.guardar(dto);

        assertEquals(1, respuesta.getId());
        assertEquals(1, respuesta.getCatalogoId());
        assertEquals(50, respuesta.getCantidad());
        assertEquals("DISPONIBLE", respuesta.getEstadoStock());

        verify(repository).save(any(Inventario.class));
    }

    @Test
    void guardar_CuandoYaExisteInventario_DebeLanzarExcepcion() {
        InventarioRequestDTO dto = crearRequest(1, 50);

        when(restTemplate.getForObject("http://catalogo/pizzas/1", Object.class)).thenReturn(new Object());
        when(repository.existsByCatalogoId(1)).thenReturn(true);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> inventarioService.guardar(dto)
        );

        assertEquals("Ya existe inventario para el producto con ID de catálogo: 1", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());

        verify(repository, never()).save(any(Inventario.class));
    }

    @Test
    void actualizar_CuandoDatosValidos_DebeActualizarInventario() {
        InventarioRequestDTO dto = crearRequest(1, 15);

        when(restTemplate.getForObject("http://catalogo/pizzas/1", Object.class)).thenReturn(new Object());
        when(repository.findByCatalogoId(1)).thenReturn(Optional.of(inventario));
        when(repository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventarioResponseDTO respuesta = inventarioService.actualizar(1, dto);

        assertEquals(1, respuesta.getCatalogoId());
        assertEquals(15, respuesta.getCantidad());
        assertEquals("STOCK BAJO", respuesta.getEstadoStock());

        verify(repository).save(any(Inventario.class));
    }

    @Test
    void actualizar_CuandoCatalogoIdNoCoincide_DebeLanzarExcepcion() {
        InventarioRequestDTO dto = crearRequest(2, 15);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> inventarioService.actualizar(1, dto)
        );

        assertEquals("No puedes modificar el catalogoId de un registro existente.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(repository, never()).save(any(Inventario.class));
    }

    @Test
    void actualizar_CuandoNoExisteInventario_DebeLanzarExcepcion() {
        InventarioRequestDTO dto = crearRequest(99, 15);

        when(restTemplate.getForObject("http://catalogo/pizzas/99", Object.class)).thenReturn(new Object());
        when(repository.findByCatalogoId(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> inventarioService.actualizar(99, dto)
        );

        assertEquals("No se puede actualizar: no existe stock para el ID de catálogo: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void eliminar_CuandoExiste_DebeEliminarInventario() {
        when(repository.findByCatalogoId(1)).thenReturn(Optional.of(inventario));

        inventarioService.eliminar(1);

        verify(repository).delete(inventario);
    }

    @Test
    void eliminar_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findByCatalogoId(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> inventarioService.eliminar(99)
        );

        assertEquals("No se puede eliminar: no existe stock para el ID de catálogo: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(repository, never()).delete(any(Inventario.class));
    }

    @Test
    void descontarStock_CuandoHayStock_DebeDescontar() {
        DescuentoInventarioDTO item = crearDescuento(1, 5);

        when(repository.findByCatalogoId(1)).thenReturn(Optional.of(inventario));
        when(repository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventarioService.descontarStock(List.of(item));

        assertEquals(45, inventario.getCantidad());

        verify(repository).save(inventario);
    }

    @Test
    void descontarStock_CuandoListaVacia_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> inventarioService.descontarStock(List.of())
        );

        assertEquals("La lista de productos a descontar no puede estar vacía.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void descontarStock_CuandoNoExisteProducto_DebeLanzarExcepcion() {
        DescuentoInventarioDTO item = crearDescuento(99, 2);

        when(repository.findByCatalogoId(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> inventarioService.descontarStock(List.of(item))
        );

        assertEquals("Producto no encontrado en inventario: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void descontarStock_CuandoStockInsuficiente_DebeLanzarExcepcion() {
        DescuentoInventarioDTO item = crearDescuento(1, 100);

        when(repository.findByCatalogoId(1)).thenReturn(Optional.of(inventario));

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> inventarioService.descontarStock(List.of(item))
        );

        assertEquals("Stock insuficiente para el producto con ID de catálogo: 1", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(repository, never()).save(any(Inventario.class));
    }

    private InventarioRequestDTO crearRequest(Integer catalogoId, Integer cantidad) {
        InventarioRequestDTO dto = new InventarioRequestDTO();
        dto.setCatalogoId(catalogoId);
        dto.setCantidad(cantidad);
        return dto;
    }

    private DescuentoInventarioDTO crearDescuento(Integer catalogoId, Integer cantidad) {
        DescuentoInventarioDTO dto = new DescuentoInventarioDTO();
        dto.setCatalogoId(catalogoId);
        dto.setCantidad(cantidad);
        return dto;
    }

}
