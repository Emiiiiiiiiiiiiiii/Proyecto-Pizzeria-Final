package com.pizzas.carrito.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pizzas.carrito.dto.CarritoDTO;
import com.pizzas.carrito.dto.CarritoDetalleDTO;
import com.pizzas.carrito.dto.ProductoDTO;
import com.pizzas.carrito.dto.UsuarioDTO;
import com.pizzas.carrito.exception.ExcepcionPersonalizada;
import com.pizzas.carrito.model.Carrito;
import com.pizzas.carrito.repository.CarritoRepository;


@Service

public class CarritoService {
    private final CarritoRepository repository;
    private final RestTemplate restTemplate;

    public CarritoService(CarritoRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    private final String AUTH_URL = "http://localhost:8080/auth/usuarios/";
    private final String CATALOGO_URL = "http://localhost:8081/catalogo/pizzas/";

    public Carrito agregarAlCarrito(CarritoDTO dto) {
        try {
            restTemplate.getForObject(AUTH_URL + dto.getUsuarioId(), Object.class);
        } catch (Exception e) {
            throw new ExcepcionPersonalizada("Usuario con ID " + dto.getUsuarioId() + " no encontrado.");
        }

        ProductoDTO producto = restTemplate.getForObject(CATALOGO_URL + dto.getCatalogoId(), ProductoDTO.class);
        if (producto == null) {
            throw new ExcepcionPersonalizada("Producto con ID " + dto.getCatalogoId() + " no existe en el catálogo.");
        }

        Carrito existente = repository.findByUsuarioIdAndCatalogoId(dto.getUsuarioId(), dto.getCatalogoId());

        if (existente != null) {
            int nuevaCantidad = existente.getCantidad() + dto.getCantidad();
            existente.setCantidad(nuevaCantidad);
            existente.setPrecioTotal(nuevaCantidad * existente.getPrecioUnitario());
            return repository.save(existente);
        } else {
            Carrito nuevo = new Carrito();
            nuevo.setUsuarioId(dto.getUsuarioId());
            nuevo.setCatalogoId(dto.getCatalogoId());
            nuevo.setCantidad(dto.getCantidad());
            nuevo.setPrecioUnitario(producto.getPrecio());
            nuevo.setPrecioTotal(dto.getCantidad() * producto.getPrecio());
            return repository.save(nuevo);
        }
    }

    public List<CarritoDetalleDTO> listarDetalladoPorUsuario(Integer usuarioId) {
        List<Carrito> listaEntities = repository.findByUsuarioId(usuarioId);
        
        if (listaEntities.isEmpty()) {
            throw new ExcepcionPersonalizada("No se encontró carrito para el usuario con ID: " + usuarioId);
        }

        List<CarritoDetalleDTO> listaDetallada = new ArrayList<>();
        UsuarioDTO usuario = restTemplate.getForObject(AUTH_URL + usuarioId, UsuarioDTO.class);

        for (Carrito c : listaEntities) {
            ProductoDTO pizza = restTemplate.getForObject(CATALOGO_URL + c.getCatalogoId(), ProductoDTO.class);

            CarritoDetalleDTO dto = new CarritoDetalleDTO();
            dto.setId(c.getId());
            
            
            dto.setCatalogoId(c.getCatalogoId());
            
            
            dto.setNombreUsuario(usuario != null ? usuario.getNombre() : "Desconocido");
            dto.setNombrePizza(pizza != null ? pizza.getNombre() : "Producto Eliminado");
            dto.setTamanio(pizza != null ? pizza.getTamanio() : "N/A");
            dto.setCantidad(c.getCantidad());
            dto.setPrecioUnitario(c.getPrecioUnitario());
            dto.setPrecioTotal(c.getPrecioTotal());

            listaDetallada.add(dto);
        }
        return listaDetallada;
    }

    public Carrito actualizarCantidad(Integer id, Integer nuevaCantidad) {
        Carrito c = repository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada("Ítem no encontrado con id: " + id));
        c.setCantidad(nuevaCantidad);
        c.setPrecioTotal(nuevaCantidad * c.getPrecioUnitario());
        return repository.save(c);
    }

    public void eliminarDelCarrito(Integer id) {
        if (!repository.existsById(id)) {
            throw new ExcepcionPersonalizada("No se puede eliminar: Ítem no encontrado con id: " + id);
        }
        repository.deleteById(id);
    }

}
