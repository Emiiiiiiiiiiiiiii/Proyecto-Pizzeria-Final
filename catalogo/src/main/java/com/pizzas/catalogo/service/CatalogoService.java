package com.pizzas.catalogo.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.pizzas.catalogo.dto.CatalogoRequestDTO;
import com.pizzas.catalogo.dto.CatalogoResponseDTO;
import com.pizzas.catalogo.exception.ExcepcionPersonalizada;
import com.pizzas.catalogo.model.Catalogo;
import com.pizzas.catalogo.repository.CatalogoRepository;

// Service con la lógica del catálogo
@Service

public class CatalogoService {
    private static final Logger logger = LoggerFactory.getLogger(CatalogoService.class);

    private final CatalogoRepository repository;

    // Constructor para inyectar repository
    public CatalogoService(CatalogoRepository repository) {
        this.repository = repository;
    }

    // Lista todas las pizzas
    public List<CatalogoResponseDTO> listar() {
        List<Catalogo> pizzas = repository.findAll();
        List<CatalogoResponseDTO> respuesta = new ArrayList<>();

        for (Catalogo pizza : pizzas) {
            respuesta.add(mapearAResponseDTO(pizza));
        }

        logger.info("Listado de catálogo obtenido. Total: {}", respuesta.size());

        return respuesta;
    }

    // Busca pizza por ID
    public CatalogoResponseDTO buscarPorId(Integer id) {
        Catalogo pizza = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Pizza no encontrada con ID: {}", id);
                    return new ExcepcionPersonalizada(
                            "Pizza no encontrada con ID: " + id,
                            HttpStatus.NOT_FOUND
                    );
                });

        return mapearAResponseDTO(pizza);
    }

    // Busca pizzas por nombre
    public List<CatalogoResponseDTO> buscarPorNombre(String nombre) {
        validarTextoBusqueda(nombre, "nombre");

        List<Catalogo> pizzas = repository.findByNombreContainingIgnoreCase(nombre.trim());

        if (pizzas.isEmpty()) {
            logger.warn("No se encontraron pizzas con nombre: {}", nombre);
            throw new ExcepcionPersonalizada(
                    "No se encontró ninguna pizza con nombre: " + nombre,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAResponseDTO(pizzas);
    }

    // Lista pizzas por tipo
    public List<CatalogoResponseDTO> listarPorTipo(String tipo) {
        validarTextoBusqueda(tipo, "tipo");

        List<Catalogo> pizzas = repository.findByTipoIgnoreCase(tipo.trim());

        if (pizzas.isEmpty()) {
            logger.warn("No existen pizzas del tipo: {}", tipo);
            throw new ExcepcionPersonalizada(
                    "No existen pizzas del tipo: " + tipo,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAResponseDTO(pizzas);
    }

    // Lista pizzas por tamaño
    public List<CatalogoResponseDTO> listarPorTamanio(String tamanio) {
        validarTextoBusqueda(tamanio, "tamaño");

        List<Catalogo> pizzas = repository.findByTamanioIgnoreCase(tamanio.trim());

        if (pizzas.isEmpty()) {
            logger.warn("No existen pizzas de tamaño: {}", tamanio);
            throw new ExcepcionPersonalizada(
                    "No existen pizzas de tamaño: " + tamanio,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAResponseDTO(pizzas);
    }

    // Guarda una nueva pizza
    public CatalogoResponseDTO guardarPizza(CatalogoRequestDTO dto) {
        String nombre = normalizarTexto(dto.getNombre());
        String tipo = normalizarTexto(dto.getTipo());
        String tamanio = normalizarTexto(dto.getTamanio());

        if (repository.existsByNombreIgnoreCaseAndTamanioIgnoreCase(nombre, tamanio)) {
            logger.warn("Intento de agregar pizza duplicada: {} {}", nombre, tamanio);
            throw new ExcepcionPersonalizada(
                    "Ya existe una pizza con ese nombre y tamaño.",
                    HttpStatus.CONFLICT
            );
        }

        Catalogo pizza = new Catalogo();
        pizza.setNombre(nombre);
        pizza.setTipo(tipo);
        pizza.setTamanio(tamanio);
        pizza.setPrecio(dto.getPrecio());

        Catalogo guardada = repository.save(pizza);

        logger.info("Pizza agregada al catálogo con ID: {}", guardada.getId());

        return mapearAResponseDTO(guardada);
    }

    // Actualiza una pizza existente
    public CatalogoResponseDTO actualizarPizza(Integer id, CatalogoRequestDTO dto) {
        Catalogo pizza = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Intento de actualizar pizza inexistente con ID: {}", id);
                    return new ExcepcionPersonalizada(
                            "No se puede actualizar: pizza no encontrada con ID: " + id,
                            HttpStatus.NOT_FOUND
                    );
                });

        String nombre = normalizarTexto(dto.getNombre());
        String tipo = normalizarTexto(dto.getTipo());
        String tamanio = normalizarTexto(dto.getTamanio());

        if (!pizza.getNombre().equalsIgnoreCase(nombre)
                || !pizza.getTamanio().equalsIgnoreCase(tamanio)) {

            if (repository.existsByNombreIgnoreCaseAndTamanioIgnoreCase(nombre, tamanio)) {
                logger.warn("Intento de actualizar a pizza duplicada: {} {}", nombre, tamanio);
                throw new ExcepcionPersonalizada(
                        "Ya existe otra pizza con ese nombre y tamaño.",
                        HttpStatus.CONFLICT
                );
            }
        }

        pizza.setNombre(nombre);
        pizza.setTipo(tipo);
        pizza.setTamanio(tamanio);
        pizza.setPrecio(dto.getPrecio());

        Catalogo actualizada = repository.save(pizza);

        logger.info("Pizza actualizada correctamente con ID: {}", id);

        return mapearAResponseDTO(actualizada);
    }

    // Elimina una pizza si existe
    public void eliminarPizza(Integer id) {
        if (!repository.existsById(id)) {
            logger.warn("Intento de eliminar pizza inexistente con ID: {}", id);
            throw new ExcepcionPersonalizada(
                    "No se puede eliminar: pizza no encontrada con ID: " + id,
                    HttpStatus.NOT_FOUND
            );
        }

        repository.deleteById(id);

        logger.info("Pizza eliminada correctamente con ID: {}", id);
    }

    // Convierte entidad a DTO de respuesta
    private CatalogoResponseDTO mapearAResponseDTO(Catalogo pizza) {
        return new CatalogoResponseDTO(
                pizza.getId(),
                pizza.getNombre(),
                pizza.getTipo(),
                pizza.getTamanio(),
                pizza.getPrecio()
        );
    }

    // Convierte lista de entidades a lista de DTOs de respuesta
    private List<CatalogoResponseDTO> mapearListaAResponseDTO(List<Catalogo> pizzas) {
        List<CatalogoResponseDTO> respuesta = new ArrayList<>();

        for (Catalogo pizza : pizzas) {
            respuesta.add(mapearAResponseDTO(pizza));
        }

        return respuesta;
    }

    // Valida texto recibido por path
    private void validarTextoBusqueda(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ExcepcionPersonalizada(
                    "El valor de búsqueda por " + campo + " es obligatorio.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // Limpia espacios sobrantes
    private String normalizarTexto(String valor) {
        return valor.trim();
    }

}
