package com.pizzas.catalogo.service;

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

import com.pizzas.catalogo.dto.CatalogoRequestDTO;
import com.pizzas.catalogo.dto.CatalogoResponseDTO;
import com.pizzas.catalogo.exception.ExcepcionPersonalizada;
import com.pizzas.catalogo.model.Catalogo;
import com.pizzas.catalogo.repository.CatalogoRepository;

// Pruebas unitarias para CatalogoService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class CatalogoServiceTest {

     @Mock
    private CatalogoRepository repository;

    @InjectMocks
    private CatalogoService catalogoService;

    private Catalogo pizza;

    @BeforeEach
    void setUp() {
        pizza = new Catalogo();
        pizza.setId(1);
        pizza.setNombre("Pepperoni");
        pizza.setTipo("Clasica");
        pizza.setTamanio("Familiar");
        pizza.setPrecio(12990);
    }

    @Test
    void listar_DebeRetornarListaDePizzas() {
        when(repository.findAll()).thenReturn(List.of(pizza));

        List<CatalogoResponseDTO> respuesta = catalogoService.listar();

        assertEquals(1, respuesta.size());
        assertEquals("Pepperoni", respuesta.get(0).getNombre());
        assertEquals("Familiar", respuesta.get(0).getTamanio());
        assertEquals(12990, respuesta.get(0).getPrecio());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarPizza() {
        when(repository.findById(1)).thenReturn(Optional.of(pizza));

        CatalogoResponseDTO respuesta = catalogoService.buscarPorId(1);

        assertEquals(1, respuesta.getId());
        assertEquals("Pepperoni", respuesta.getNombre());
        assertEquals("Clasica", respuesta.getTipo());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> catalogoService.buscarPorId(99)
        );

        assertEquals("Pizza no encontrada con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorNombre_CuandoExiste_DebeRetornarPizzas() {
        when(repository.findByNombreContainingIgnoreCase("Pepperoni")).thenReturn(List.of(pizza));

        List<CatalogoResponseDTO> respuesta = catalogoService.buscarPorNombre("Pepperoni");

        assertEquals(1, respuesta.size());
        assertEquals("Pepperoni", respuesta.get(0).getNombre());
    }

    @Test
    void buscarPorNombre_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.findByNombreContainingIgnoreCase("Hawaiana")).thenReturn(List.of());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> catalogoService.buscarPorNombre("Hawaiana")
        );

        assertEquals("No se encontró ninguna pizza con nombre: Hawaiana", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void buscarPorNombre_CuandoTextoVacio_DebeLanzarExcepcion() {
        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> catalogoService.buscarPorNombre(" ")
        );

        assertEquals("El valor de búsqueda por nombre es obligatorio.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void listarPorTipo_CuandoExiste_DebeRetornarPizzas() {
        when(repository.findByTipoIgnoreCase("Clasica")).thenReturn(List.of(pizza));

        List<CatalogoResponseDTO> respuesta = catalogoService.listarPorTipo("Clasica");

        assertEquals(1, respuesta.size());
        assertEquals("Clasica", respuesta.get(0).getTipo());
    }

    @Test
    void listarPorTamanio_CuandoExiste_DebeRetornarPizzas() {
        when(repository.findByTamanioIgnoreCase("Familiar")).thenReturn(List.of(pizza));

        List<CatalogoResponseDTO> respuesta = catalogoService.listarPorTamanio("Familiar");

        assertEquals(1, respuesta.size());
        assertEquals("Familiar", respuesta.get(0).getTamanio());
    }

    @Test
    void guardarPizza_CuandoNoExisteDuplicado_DebeGuardarPizza() {
        CatalogoRequestDTO dto = crearRequest(" Pepperoni ", " Clasica ", " Familiar ", 12990);

        when(repository.existsByNombreIgnoreCaseAndTamanioIgnoreCase("Pepperoni", "Familiar")).thenReturn(false);
        when(repository.save(any(Catalogo.class))).thenAnswer(invocation -> {
            Catalogo guardada = invocation.getArgument(0);
            guardada.setId(1);
            return guardada;
        });

        CatalogoResponseDTO respuesta = catalogoService.guardarPizza(dto);

        assertEquals(1, respuesta.getId());
        assertEquals("Pepperoni", respuesta.getNombre());
        assertEquals("Clasica", respuesta.getTipo());
        assertEquals("Familiar", respuesta.getTamanio());

        verify(repository).save(any(Catalogo.class));
    }

    @Test
    void guardarPizza_CuandoExisteDuplicado_DebeLanzarExcepcion() {
        CatalogoRequestDTO dto = crearRequest("Pepperoni", "Clasica", "Familiar", 12990);

        when(repository.existsByNombreIgnoreCaseAndTamanioIgnoreCase("Pepperoni", "Familiar")).thenReturn(true);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> catalogoService.guardarPizza(dto)
        );

        assertEquals("Ya existe una pizza con ese nombre y tamaño.", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());

        verify(repository, never()).save(any(Catalogo.class));
    }

    @Test
    void actualizarPizza_CuandoExiste_DebeActualizarPizza() {
        CatalogoRequestDTO dto = crearRequest("Pepperoni Especial", "Premium", "Familiar", 14990);

        when(repository.findById(1)).thenReturn(Optional.of(pizza));
        when(repository.existsByNombreIgnoreCaseAndTamanioIgnoreCase("Pepperoni Especial", "Familiar")).thenReturn(false);
        when(repository.save(any(Catalogo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CatalogoResponseDTO respuesta = catalogoService.actualizarPizza(1, dto);

        assertEquals("Pepperoni Especial", respuesta.getNombre());
        assertEquals("Premium", respuesta.getTipo());
        assertEquals(14990, respuesta.getPrecio());

        verify(repository).save(any(Catalogo.class));
    }

    @Test
    void actualizarPizza_CuandoNoExiste_DebeLanzarExcepcion() {
        CatalogoRequestDTO dto = crearRequest("Pepperoni", "Clasica", "Familiar", 12990);

        when(repository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> catalogoService.actualizarPizza(99, dto)
        );

        assertEquals("No se puede actualizar: pizza no encontrada con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void actualizarPizza_CuandoExisteOtraPizzaDuplicada_DebeLanzarExcepcion() {
        CatalogoRequestDTO dto = crearRequest("Napolitana", "Clasica", "Familiar", 13990);

        when(repository.findById(1)).thenReturn(Optional.of(pizza));
        when(repository.existsByNombreIgnoreCaseAndTamanioIgnoreCase("Napolitana", "Familiar")).thenReturn(true);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> catalogoService.actualizarPizza(1, dto)
        );

        assertEquals("Ya existe otra pizza con ese nombre y tamaño.", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());

        verify(repository, never()).save(any(Catalogo.class));
    }

    @Test
    void eliminarPizza_CuandoExiste_DebeEliminarPizza() {
        when(repository.existsById(1)).thenReturn(true);

        catalogoService.eliminarPizza(1);

        verify(repository).deleteById(1);
    }

    @Test
    void eliminarPizza_CuandoNoExiste_DebeLanzarExcepcion() {
        when(repository.existsById(99)).thenReturn(false);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> catalogoService.eliminarPizza(99)
        );

        assertEquals("No se puede eliminar: pizza no encontrada con ID: 99", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(repository, never()).deleteById(99);
    }

    private CatalogoRequestDTO crearRequest(String nombre, String tipo, String tamanio, Integer precio) {
        CatalogoRequestDTO dto = new CatalogoRequestDTO();
        dto.setNombre(nombre);
        dto.setTipo(tipo);
        dto.setTamanio(tamanio);
        dto.setPrecio(precio);
        return dto;
    }

}
