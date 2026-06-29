package com.pizzas.inventario.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.pizzas.inventario.dto.DescuentoInventarioDTO;
import com.pizzas.inventario.dto.InventarioRequestDTO;
import com.pizzas.inventario.dto.InventarioResponseDTO;
import com.pizzas.inventario.exception.ExcepcionPersonalizada;
import com.pizzas.inventario.model.Inventario;
import com.pizzas.inventario.repository.InventarioRepository;

// Service con la lógica del inventario
@Service
public class InventarioService {
     private static final Logger logger = LoggerFactory.getLogger(InventarioService.class);

    private final InventarioRepository repository;
    private final RestTemplate restTemplate;

    @Value("${url.catalogo}")
    private String URL_CATALOGO;

    // Constructor para inyectar dependencias
    public InventarioService(InventarioRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // Lista todo el inventario
    public List<InventarioResponseDTO> listar() {
        List<Inventario> inventarios = repository.findAll();
        List<InventarioResponseDTO> respuesta = new ArrayList<>();

        for (Inventario inventario : inventarios) {
            respuesta.add(mapearAResponse(inventario));
        }

        logger.info("Listado de inventario obtenido. Total: {}", respuesta.size());

        return respuesta;
    }

    // Busca inventario por ID de catálogo
    public InventarioResponseDTO buscarPorCatalogoId(Integer catalogoId) {
        Inventario inventario = repository.findByCatalogoId(catalogoId)
                .orElseThrow(() -> {
                    logger.warn("Inventario no encontrado para catalogoId: {}", catalogoId);
                    return new ExcepcionPersonalizada(
                            "No se encontró stock para el ID de catálogo: " + catalogoId,
                            HttpStatus.NOT_FOUND
                    );
                });

        return mapearAResponse(inventario);
    }

    // Lista productos con bajo stock
    public List<InventarioResponseDTO> listarStockBajo(Integer limite) {
        if (limite == null || limite < 0) {
            throw new ExcepcionPersonalizada(
                    "El límite de stock debe ser mayor o igual a 0.",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<Inventario> inventarios = repository.findByCantidadLessThan(limite);
        List<InventarioResponseDTO> respuesta = new ArrayList<>();

        for (Inventario inventario : inventarios) {
            respuesta.add(mapearAResponse(inventario));
        }

        logger.info("Listado de stock bajo obtenido. Total: {}", respuesta.size());

        return respuesta;
    }

    // Guarda inventario validando que el producto exista en catálogo
    public InventarioResponseDTO guardar(InventarioRequestDTO dto) {
        validarProductoEnCatalogo(dto.getCatalogoId());

        if (repository.existsByCatalogoId(dto.getCatalogoId())) {
            logger.warn("Intento de crear inventario duplicado para catalogoId: {}", dto.getCatalogoId());
            throw new ExcepcionPersonalizada(
                    "Ya existe inventario para el producto con ID de catálogo: " + dto.getCatalogoId(),
                    HttpStatus.CONFLICT
            );
        }

        Inventario inventario = new Inventario();
        inventario.setCatalogoId(dto.getCatalogoId());
        inventario.setCantidad(dto.getCantidad());

        Inventario guardado = repository.save(inventario);

        logger.info("Inventario creado para catalogoId: {}", guardado.getCatalogoId());

        return mapearAResponse(guardado);
    }

    // Actualiza stock de un producto existente
    public InventarioResponseDTO actualizar(Integer catalogoId, InventarioRequestDTO dto) {
        if (!catalogoId.equals(dto.getCatalogoId())) {
            throw new ExcepcionPersonalizada(
                    "No puedes modificar el catalogoId de un registro existente.",
                    HttpStatus.BAD_REQUEST
            );
        }

        validarProductoEnCatalogo(catalogoId);

        Inventario inventario = repository.findByCatalogoId(catalogoId)
                .orElseThrow(() -> {
                    logger.warn("Intento de actualizar inventario inexistente para catalogoId: {}", catalogoId);
                    return new ExcepcionPersonalizada(
                            "No se puede actualizar: no existe stock para el ID de catálogo: " + catalogoId,
                            HttpStatus.NOT_FOUND
                    );
                });

        inventario.setCantidad(dto.getCantidad());

        Inventario actualizado = repository.save(inventario);

        logger.info("Inventario actualizado para catalogoId: {}", catalogoId);

        return mapearAResponse(actualizado);
    }

    // Elimina inventario por ID de catálogo
    public void eliminar(Integer catalogoId) {
        Inventario inventario = repository.findByCatalogoId(catalogoId)
                .orElseThrow(() -> {
                    logger.warn("Intento de eliminar inventario inexistente para catalogoId: {}", catalogoId);
                    return new ExcepcionPersonalizada(
                            "No se puede eliminar: no existe stock para el ID de catálogo: " + catalogoId,
                            HttpStatus.NOT_FOUND
                    );
                });

        repository.delete(inventario);

        logger.info("Inventario eliminado para catalogoId: {}", catalogoId);
    }

    // Descuenta stock desde pedido
    @Transactional
    public void descontarStock(List<DescuentoInventarioDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "La lista de productos a descontar no puede estar vacía.",
                    HttpStatus.BAD_REQUEST
            );
        }

        for (DescuentoInventarioDTO item : items) {
            Inventario inventario = repository.findByCatalogoId(item.getCatalogoId())
                    .orElseThrow(() -> {
                        logger.warn("Producto sin inventario. catalogoId: {}", item.getCatalogoId());
                        return new ExcepcionPersonalizada(
                                "Producto no encontrado en inventario: " + item.getCatalogoId(),
                                HttpStatus.NOT_FOUND
                        );
                    });

            if (inventario.getCantidad() < item.getCantidad()) {
                logger.warn(
                        "Stock insuficiente para catalogoId {}. Stock actual: {}, solicitado: {}",
                        item.getCatalogoId(),
                        inventario.getCantidad(),
                        item.getCantidad()
                );

                throw new ExcepcionPersonalizada(
                        "Stock insuficiente para el producto con ID de catálogo: " + item.getCatalogoId(),
                        HttpStatus.BAD_REQUEST
                );
            }

            inventario.setCantidad(inventario.getCantidad() - item.getCantidad());
            repository.save(inventario);
        }

        logger.info("Stock descontado correctamente para {} productos", items.size());
    }

    // Valida que el producto exista en catálogo
    private void validarProductoEnCatalogo(Integer catalogoId) {
        try {
            restTemplate.getForObject(URL_CATALOGO + "pizzas/" + catalogoId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Producto no encontrado en catálogo. catalogoId: {}", catalogoId);
            throw new ExcepcionPersonalizada(
                    "El producto con ID " + catalogoId + " no existe en el catálogo.",
                    HttpStatus.NOT_FOUND
            );
        } catch (ResourceAccessException e) {
            logger.error("MS Catálogo no responde: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "No se pudo validar el producto porque catálogo no responde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con catálogo: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "Ocurrió un error al validar el producto en catálogo.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Convierte entidad a DTO de respuesta
    private InventarioResponseDTO mapearAResponse(Inventario inventario) {
        return new InventarioResponseDTO(
                inventario.getId(),
                inventario.getCatalogoId(),
                inventario.getCantidad(),
                obtenerEstadoStock(inventario.getCantidad())
        );
    }

    // Calcula estado simple del stock
    private String obtenerEstadoStock(Integer cantidad) {
        if (cantidad == null || cantidad == 0) {
            return "SIN STOCK";
        }

        if (cantidad <= 20) {
            return "STOCK BAJO";
        }

        return "DISPONIBLE";
    }
}
