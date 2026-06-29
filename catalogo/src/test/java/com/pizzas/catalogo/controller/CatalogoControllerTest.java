package com.pizzas.catalogo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.pizzas.catalogo.dto.CatalogoRequestDTO;
import com.pizzas.catalogo.dto.CatalogoResponseDTO;
import com.pizzas.catalogo.exception.ExcepcionPersonalizada;
import com.pizzas.catalogo.exception.ManejadorErrores;
import com.pizzas.catalogo.service.CatalogoService;

// Pruebas unitarias del controller de catálogo usando MockMvc
@WebMvcTest(CatalogoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
public class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CatalogoService catalogoService;

    @Test
    void listar_DebeResponderCatalogo() throws Exception {
        CatalogoResponseDTO pizza = crearResponse();

        when(catalogoService.listar()).thenReturn(List.of(pizza));

        mockMvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Listado de catálogo obtenido correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pizzas[0].nombre").value("Pepperoni"));

        verify(catalogoService).listar();
    }

    @Test
    void listarPizzas_DebeResponderPizzas() throws Exception {
        CatalogoResponseDTO pizza = crearResponse();

        when(catalogoService.listar()).thenReturn(List.of(pizza));

        mockMvc.perform(get("/catalogo/pizzas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Listado de pizzas obtenido correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pizzas[0].nombre").value("Pepperoni"));

        verify(catalogoService).listar();
    }

    @Test
    void buscarPorId_CuandoExiste_DebeResponderPizzaDirecta() throws Exception {
        CatalogoResponseDTO pizza = crearResponse();

        when(catalogoService.buscarPorId(1)).thenReturn(pizza);

        mockMvc.perform(get("/catalogo/pizzas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Pepperoni"))
                .andExpect(jsonPath("$.precio").value(12990));

        verify(catalogoService).buscarPorId(1);
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeResponderNotFound() throws Exception {
        when(catalogoService.buscarPorId(99))
                .thenThrow(new ExcepcionPersonalizada(
                        "Pizza no encontrada con ID: 99",
                        HttpStatus.NOT_FOUND
                ));

        mockMvc.perform(get("/catalogo/pizzas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pizza no encontrada con ID: 99"));
    }

    @Test
    void buscarPorNombre_DebeResponderPizzas() throws Exception {
        CatalogoResponseDTO pizza = crearResponse();

        when(catalogoService.buscarPorNombre("Pepperoni")).thenReturn(List.of(pizza));

        mockMvc.perform(get("/catalogo/nombre/Pepperoni"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pizzas encontradas por nombre"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pizzas[0].nombre").value("Pepperoni"));

        verify(catalogoService).buscarPorNombre("Pepperoni");
    }

    @Test
    void buscarPorTipo_DebeResponderPizzas() throws Exception {
        CatalogoResponseDTO pizza = crearResponse();

        when(catalogoService.listarPorTipo("Clasica")).thenReturn(List.of(pizza));

        mockMvc.perform(get("/catalogo/tipo/Clasica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pizzas encontradas por tipo"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pizzas[0].tipo").value("Clasica"));

        verify(catalogoService).listarPorTipo("Clasica");
    }

    @Test
    void buscarPorTamanio_DebeResponderPizzas() throws Exception {
        CatalogoResponseDTO pizza = crearResponse();

        when(catalogoService.listarPorTamanio("Familiar")).thenReturn(List.of(pizza));

        mockMvc.perform(get("/catalogo/tamanio/Familiar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pizzas encontradas por tamaño"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pizzas[0].tamanio").value("Familiar"));

        verify(catalogoService).listarPorTamanio("Familiar");
    }

    @Test
    void agregar_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        CatalogoResponseDTO pizza = crearResponse();

        when(catalogoService.guardarPizza(any(CatalogoRequestDTO.class))).thenReturn(pizza);

        Map<String, Object> body = crearBodyValido();

        mockMvc.perform(post("/catalogo/agregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Pizza agregada correctamente al catálogo"))
                .andExpect(jsonPath("$.pizza.id").value(1))
                .andExpect(jsonPath("$.pizza.nombre").value("Pepperoni"));

        verify(catalogoService).guardarPizza(any(CatalogoRequestDTO.class));
    }

    @Test
    void agregar_CuandoPrecioInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = crearBodyValido();
        body.put("precio", 0);

        mockMvc.perform(post("/catalogo/agregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(catalogoService, never()).guardarPizza(any(CatalogoRequestDTO.class));
    }

    @Test
    void actualizar_CuandoExiste_DebeResponderOk() throws Exception {
        CatalogoResponseDTO pizza = new CatalogoResponseDTO(
                1,
                "Pepperoni Especial",
                "Premium",
                "Familiar",
                14990
        );

        when(catalogoService.actualizarPizza(eq(1), any(CatalogoRequestDTO.class))).thenReturn(pizza);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Pepperoni Especial");
        body.put("tipo", "Premium");
        body.put("tamanio", "Familiar");
        body.put("precio", 14990);

        mockMvc.perform(put("/catalogo/actualizar/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pizza actualizada correctamente"))
                .andExpect(jsonPath("$.pizza.nombre").value("Pepperoni Especial"))
                .andExpect(jsonPath("$.pizza.precio").value(14990));

        verify(catalogoService).actualizarPizza(eq(1), any(CatalogoRequestDTO.class));
    }

    @Test
    void eliminar_CuandoExiste_DebeResponderOk() throws Exception {
        doNothing().when(catalogoService).eliminarPizza(1);

        mockMvc.perform(delete("/catalogo/eliminar/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pizza eliminada correctamente"))
                .andExpect(jsonPath("$.idEliminado").value(1));

        verify(catalogoService).eliminarPizza(1);
    }

    private CatalogoResponseDTO crearResponse() {
        return new CatalogoResponseDTO(
                1,
                "Pepperoni",
                "Clasica",
                "Familiar",
                12990
        );
    }

    private Map<String, Object> crearBodyValido() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Pepperoni");
        body.put("tipo", "Clasica");
        body.put("tamanio", "Familiar");
        body.put("precio", 12990);
        return body;
    }

}
