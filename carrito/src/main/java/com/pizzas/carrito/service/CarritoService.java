package com.pizzas.carrito.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
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
    // Logger para registrar acciones importantes del carrito
    private static final Logger logger = LoggerFactory.getLogger(CarritoService.class);

    // URLs de los microservicios que consume carrito
    private static final String AUTH_URL = "http://localhost:8080/auth/usuarios/";
    private static final String CATALOGO_URL = "http://localhost:8081/catalogo/pizzas/";

    // Repositorio del carrito
    private final CarritoRepository repository;

    // Cliente para comunicarse con otros microservicios
    private final RestTemplate restTemplate;

    public CarritoService(CarritoRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // Agrega un producto al carrito o aumenta la cantidad si ya existe
    public Carrito agregarAlCarrito(CarritoDTO dto) {

        UsuarioDTO usuario = obtenerUsuario(dto.getUsuarioId());
        ProductoDTO producto = obtenerProducto(dto.getCatalogoId());

        Carrito carrito = repository.findByUsuarioIdAndCatalogoId(dto.getUsuarioId(), dto.getCatalogoId())
                .orElse(null);

        if (carrito != null) {
            int nuevaCantidad = carrito.getCantidad() + dto.getCantidad();

            if (nuevaCantidad > 50) {
                logger.warn("Cantidad excedida en carrito. Usuario ID: {}, Producto ID: {}", dto.getUsuarioId(), dto.getCatalogoId());
                throw new ExcepcionPersonalizada("No puedes tener más de 50 unidades del mismo producto en el carrito.");
            }

            carrito.setCantidad(nuevaCantidad);
            carrito.setPrecioUnitario(producto.getPrecio());
            carrito.setPrecioTotal(nuevaCantidad * producto.getPrecio());

            Carrito actualizado = repository.save(carrito);

            logger.info("Cantidad actualizada en carrito. Usuario: {}, Producto: {}, Cantidad: {}",
                    usuario.getNombre(), producto.getNombre(), nuevaCantidad);

            return actualizado;
        }

        Carrito nuevo = new Carrito();
        nuevo.setUsuarioId(dto.getUsuarioId());
        nuevo.setCatalogoId(dto.getCatalogoId());
        nuevo.setCantidad(dto.getCantidad());
        nuevo.setPrecioUnitario(producto.getPrecio());
        nuevo.setPrecioTotal(dto.getCantidad() * producto.getPrecio());

        Carrito guardado = repository.save(nuevo);

        logger.info("Producto agregado al carrito. Usuario: {}, Producto: {}, Cantidad: {}",
                usuario.getNombre(), producto.getNombre(), dto.getCantidad());

        return guardado;
    }

    // Lista el carrito de un usuario con datos de usuario y pizza
    public List<CarritoDetalleDTO> listarDetalladoPorUsuario(Integer usuarioId) {

        UsuarioDTO usuario = obtenerUsuario(usuarioId);

        List<Carrito> listaCarrito = repository.findByUsuarioId(usuarioId);

        if (listaCarrito.isEmpty()) {
            logger.warn("Carrito vacío para usuario ID: {}", usuarioId);
            throw new ExcepcionPersonalizada("No se encontraron productos en el carrito del usuario con ID: " + usuarioId);
        }

        List<CarritoDetalleDTO> listaDetallada = new ArrayList<>();

        for (Carrito item : listaCarrito) {
            ProductoDTO producto = obtenerProducto(item.getCatalogoId());

            CarritoDetalleDTO detalle = new CarritoDetalleDTO();
            detalle.setId(item.getId());
            detalle.setCatalogoId(item.getCatalogoId());
            detalle.setNombreUsuario(usuario.getNombre());
            detalle.setNombrePizza(producto.getNombre());
            detalle.setTamanio(producto.getTamanio());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalle.setPrecioTotal(item.getPrecioTotal());

            listaDetallada.add(detalle);
        }

        logger.info("Carrito listado correctamente para usuario ID: {}. Total items: {}", usuarioId, listaDetallada.size());

        return listaDetallada;
    }

    // Lista todos los items del carrito sin detalle remoto
    public List<Carrito> listarTodos() {
        List<Carrito> carritos = repository.findAll();

        logger.info("Listado general de carrito obtenido. Total: {}", carritos.size());

        return carritos;
    }

    // Busca un item del carrito por su ID
    public Carrito buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Item de carrito no encontrado con ID: {}", id);
                    return new ExcepcionPersonalizada("El item del carrito con ID " + id + " no existe.");
                });
    }

    // Actualiza la cantidad de un item del carrito
    public Carrito actualizarCantidad(Integer id, Integer nuevaCantidad) {

        if (nuevaCantidad == null || nuevaCantidad < 1) {
            logger.warn("Cantidad inválida al actualizar carrito. ID item: {}", id);
            throw new ExcepcionPersonalizada("La cantidad debe ser mayor o igual a 1.");
        }

        if (nuevaCantidad > 50) {
            logger.warn("Cantidad mayor a 50 al actualizar carrito. ID item: {}", id);
            throw new ExcepcionPersonalizada("No puedes agregar más de 50 unidades del mismo producto.");
        }

        Carrito carrito = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Intento de actualizar item inexistente con ID: {}", id);
                    return new ExcepcionPersonalizada("No se puede actualizar. El item del carrito no existe.");
                });

        carrito.setCantidad(nuevaCantidad);
        carrito.setPrecioTotal(nuevaCantidad * carrito.getPrecioUnitario());

        Carrito actualizado = repository.save(carrito);

        logger.info("Cantidad actualizada correctamente. Item ID: {}, Nueva cantidad: {}", id, nuevaCantidad);

        return actualizado;
    }

    // Elimina un item específico del carrito
    public void eliminarDelCarrito(Integer id) {

        if (!repository.existsById(id)) {
            logger.warn("Intento de eliminar item inexistente con ID: {}", id);
            throw new ExcepcionPersonalizada("No se puede eliminar. El item del carrito no existe.");
        }

        repository.deleteById(id);

        logger.info("Item eliminado correctamente del carrito. ID: {}", id);
    }

    // Elimina todo el carrito de un usuario
    @Transactional
    public void vaciarCarritoPorUsuario(Integer usuarioId) {

        obtenerUsuario(usuarioId);

        List<Carrito> productos = repository.findByUsuarioId(usuarioId);

        if (productos.isEmpty()) {
            logger.warn("Intento de vaciar carrito vacío. Usuario ID: {}", usuarioId);
            throw new ExcepcionPersonalizada("El usuario no tiene productos en el carrito.");
        }

        repository.deleteByUsuarioId(usuarioId);

        logger.info("Carrito vaciado correctamente para usuario ID: {}", usuarioId);
    }

    // Obtiene un usuario desde el microservicio autenticación
    @SuppressWarnings("unchecked")
    private UsuarioDTO obtenerUsuario(Integer usuarioId) {
        try {
            Map<String, Object> respuesta = restTemplate.getForObject(AUTH_URL + usuarioId, Map.class);

            if (respuesta == null) {
                throw new ExcepcionPersonalizada("No se pudo obtener información del usuario.");
            }

            Object datosUsuario = respuesta.containsKey("usuario") ? respuesta.get("usuario") : respuesta;

            if (!(datosUsuario instanceof Map)) {
                throw new ExcepcionPersonalizada("La respuesta del microservicio autenticación no tiene el formato esperado.");
            }

            Map<String, Object> usuarioMap = (Map<String, Object>) datosUsuario;

            UsuarioDTO usuario = new UsuarioDTO();
            usuario.setId(convertirAEntero(usuarioMap.get("id")));
            usuario.setNombre((String) usuarioMap.get("nombre"));
            usuario.setApellido((String) usuarioMap.get("apellido"));
            usuario.setEmail((String) usuarioMap.get("email"));
            usuario.setRol((String) usuarioMap.get("rol"));

            return usuario;

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Usuario no encontrado en autenticación. ID: {}", usuarioId);
            throw new ExcepcionPersonalizada("El usuario con ID " + usuarioId + " no existe.");
        } catch (ResourceAccessException e) {
            logger.error("MS Autenticación no responde: {}", e.getMessage());
            throw new ExcepcionPersonalizada("No se pudo validar el usuario porque el microservicio de autenticación no responde.");
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con MS Autenticación: {}", e.getMessage());
            throw new ExcepcionPersonalizada("Ocurrió un error al validar el usuario en autenticación.");
        }
    }

    // Obtiene una pizza/producto desde el microservicio catálogo
    private ProductoDTO obtenerProducto(Integer catalogoId) {
        try {
            ProductoDTO producto = restTemplate.getForObject(CATALOGO_URL + catalogoId, ProductoDTO.class);

            if (producto == null || producto.getId() == null) {
                throw new ExcepcionPersonalizada("El producto con ID " + catalogoId + " no existe en el catálogo.");
            }

            if (producto.getPrecio() == null || producto.getPrecio() < 0) {
                throw new ExcepcionPersonalizada("El producto no tiene un precio válido.");
            }

            return producto;

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Producto no encontrado en catálogo. ID: {}", catalogoId);
            throw new ExcepcionPersonalizada("El producto con ID " + catalogoId + " no existe en el catálogo.");
        } catch (ResourceAccessException e) {
            logger.error("MS Catálogo no responde: {}", e.getMessage());
            throw new ExcepcionPersonalizada("No se pudo validar el producto porque el microservicio catálogo no responde.");
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con MS Catálogo: {}", e.getMessage());
            throw new ExcepcionPersonalizada("Ocurrió un error al validar el producto en catálogo.");
        }
    }

    // Convierte números recibidos desde respuestas Map
    private Integer convertirAEntero(Object valor) {
        if (valor instanceof Integer) {
            return (Integer) valor;
        }

        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }

        return null;
    }

}
