package com.pizzas.carrito.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzas.carrito.dto.CarritoDTO;
import com.pizzas.carrito.dto.CarritoDetalleDTO;
import com.pizzas.carrito.exception.ManejadorErrores;
import com.pizzas.carrito.model.Carrito;
import com.pizzas.carrito.service.CarritoService;

// Pruebas unitarias del controller de carrito usando MockMvc
@WebMvcTest(CarritoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
public class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CarritoService service;

    @Test
    void agregar_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        Carrito item = crearItem();

        when(service.agregarAlCarrito(any(CarritoDTO.class))).thenReturn(item);

        Map<String, Object> body = bodyCarrito(1, 1, 2);

        mockMvc.perform(post("/carrito/agregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Producto agregado al carrito correctamente"))
                .andExpect(jsonPath("$.item.id").value(1))
                .andExpect(jsonPath("$.item.cantidad").value(2))
                .andExpect(jsonPath("$.item.precioTotal").value(25980));

        verify(service).agregarAlCarrito(any(CarritoDTO.class));
    }

    @Test
    void agregar_CuandoCantidadInvalida_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = bodyCarrito(1, 1, 0);

        mockMvc.perform(post("/carrito/agregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(service, never()).agregarAlCarrito(any(CarritoDTO.class));
    }

    @Test
    void agregarLista_CuandoListaValida_DebeResponderCreated() throws Exception {
        Carrito item = crearItem();

        when(service.agregarAlCarrito(any(CarritoDTO.class))).thenReturn(item);

        List<Map<String, Object>> body = List.of(
                bodyCarrito(1, 1, 2),
                bodyCarrito(1, 2, 1)
        );

        mockMvc.perform(post("/carrito/agregar-lista")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Productos agregados al carrito correctamente"))
                .andExpect(jsonPath("$.totalProcesados").value(2))
                .andExpect(jsonPath("$.items[0].id").value(1));

        verify(service, times(2)).agregarAlCarrito(any(CarritoDTO.class));
    }

    @Test
    void agregarLista_CuandoListaVacia_DebeResponderBadRequest() throws Exception {
        mockMvc.perform(post("/carrito/agregar-lista")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La lista de productos no puede estar vacía"));

        verify(service, never()).agregarAlCarrito(any(CarritoDTO.class));
    }

    @Test
    void listarPorUsuario_DebeResponderListaDirecta() throws Exception {
        CarritoDetalleDTO detalle = crearDetalle();

        when(service.listarDetalladoPorUsuario(1)).thenReturn(List.of(detalle));

        mockMvc.perform(get("/carrito/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombreUsuario").value("Juan"))
                .andExpect(jsonPath("$[0].nombrePizza").value("Pepperoni"));

        verify(service).listarDetalladoPorUsuario(1);
    }

    @Test
    void listarPorUsuarioConMensaje_DebeResponderDetalle() throws Exception {
        CarritoDetalleDTO detalle = crearDetalle();

        when(service.listarDetalladoPorUsuario(1)).thenReturn(List.of(detalle));

        mockMvc.perform(get("/carrito/usuario/1/respuesta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Carrito obtenido correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].nombrePizza").value("Pepperoni"));

        verify(service).listarDetalladoPorUsuario(1);
    }

    @Test
    void listarTodos_DebeResponderItems() throws Exception {
        Carrito item = crearItem();

        when(service.listarTodos()).thenReturn(List.of(item));

        mockMvc.perform(get("/carrito/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Items del carrito obtenidos correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(1));

        verify(service).listarTodos();
    }

    @Test
    void buscarPorId_DebeResponderItem() throws Exception {
        Carrito item = crearItem();

        when(service.buscarPorId(1)).thenReturn(item);

        mockMvc.perform(get("/carrito/item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Item del carrito encontrado correctamente"))
                .andExpect(jsonPath("$.item.id").value(1))
                .andExpect(jsonPath("$.item.cantidad").value(2));

        verify(service).buscarPorId(1);
    }

    @Test
    void actualizar_DebeResponderItemActualizado() throws Exception {
        Carrito actualizado = crearItem();
        actualizado.setCantidad(4);
        actualizado.setPrecioTotal(51960);

        when(service.actualizarCantidad(eq(1), eq(4))).thenReturn(actualizado);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cantidad", 4);

        mockMvc.perform(put("/carrito/item/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cantidad actualizada correctamente"))
                .andExpect(jsonPath("$.item.cantidad").value(4))
                .andExpect(jsonPath("$.item.precioTotal").value(51960));

        verify(service).actualizarCantidad(1, 4);
    }

    @Test
    void eliminarItem_DebeResponderOk() throws Exception {
        doNothing().when(service).eliminarDelCarrito(1);

        mockMvc.perform(delete("/carrito/item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Item eliminado del carrito correctamente"));

        verify(service).eliminarDelCarrito(1);
    }

    @Test
    void vaciarCarrito_DebeResponderOk() throws Exception {
        doNothing().when(service).vaciarCarritoPorUsuario(1);

        mockMvc.perform(delete("/carrito/usuario/1/vaciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Carrito vaciado correctamente"));

        verify(service).vaciarCarritoPorUsuario(1);
    }

    private Carrito crearItem() {
        Carrito item = new Carrito();
        item.setId(1);
        item.setUsuarioId(1);
        item.setCatalogoId(1);
        item.setCantidad(2);
        item.setPrecioUnitario(12990);
        item.setPrecioTotal(25980);
        return item;
    }

    private CarritoDetalleDTO crearDetalle() {
        CarritoDetalleDTO detalle = new CarritoDetalleDTO();
        detalle.setId(1);
        detalle.setCatalogoId(1);
        detalle.setNombreUsuario("Juan");
        detalle.setNombrePizza("Pepperoni");
        detalle.setTamanio("Familiar");
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(12990);
        detalle.setPrecioTotal(25980);
        return detalle;
    }

    private Map<String, Object> bodyCarrito(Integer usuarioId, Integer catalogoId, Integer cantidad) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("usuarioId", usuarioId);
        body.put("catalogoId", catalogoId);
        body.put("cantidad", cantidad);
        return body;
    }

}
