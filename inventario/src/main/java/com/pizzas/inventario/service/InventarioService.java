package com.pizzas.inventario.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pizzas.inventario.dto.InventarioRequestDTO;
import com.pizzas.inventario.dto.InventarioResponseDTO;
import com.pizzas.inventario.exception.ExcepcionPersonalizada;
import com.pizzas.inventario.model.Inventario;
import com.pizzas.inventario.repository.InventarioRepository;

@Service
public class InventarioService {
    @Autowired
    private InventarioRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms-catalogo.url}")
    private String catalogoUrl;

    public List<InventarioResponseDTO> listar() {
        List<Inventario> inventarios = repository.findAll();
        List<InventarioResponseDTO> listaResponse = new ArrayList<>();

        for (Inventario i : inventarios) {
            listaResponse.add(new InventarioResponseDTO(i.getId(), i.getCatalogoId(), i.getCantidad()));
        }
        return listaResponse;
    }

    public InventarioResponseDTO buscarPorCatalogoId(Integer catalogoId) {
        Inventario i = repository.findByCatalogoId(catalogoId)
                .orElseThrow(() -> new ExcepcionPersonalizada("No se encontró stock para el ID de catálogo: " + catalogoId));
        return new InventarioResponseDTO(i.getId(), i.getCatalogoId(), i.getCantidad());
    }

    public InventarioResponseDTO guardar(InventarioRequestDTO dto) {
        String url = catalogoUrl + "pizzas/" + dto.getCatalogoId();

        try {
            restTemplate.getForObject(url, Object.class);
        } catch (Exception e) {
            throw new ExcepcionPersonalizada("Error: El producto con ID " + dto.getCatalogoId() + " no existe en el catálogo.");
        }

        if (repository.findByCatalogoId(dto.getCatalogoId()).isPresent()) {
            throw new ExcepcionPersonalizada("Error: Ya existe un registro de stock para el ID " + dto.getCatalogoId());
        }

        Inventario nuevo = new Inventario();
        nuevo.setCatalogoId(dto.getCatalogoId());
        nuevo.setCantidad(dto.getCantidad());
        
        Inventario guardado = repository.save(nuevo);
        return new InventarioResponseDTO(guardado.getId(), guardado.getCatalogoId(), guardado.getCantidad());
    }

    public InventarioResponseDTO actualizar(Integer catalogoId, InventarioRequestDTO dto) {
        if (!catalogoId.equals(dto.getCatalogoId())) {
            throw new ExcepcionPersonalizada("Error: No puedes modificar el catalogoId de un registro existente.");
        }

        Inventario i = repository.findByCatalogoId(catalogoId)
                .orElseThrow(() -> new ExcepcionPersonalizada("No se puede actualizar: No existe stock para el ID: " + catalogoId));
    
        i.setCantidad(dto.getCantidad());
        repository.save(i);
    
        return new InventarioResponseDTO(i.getId(), i.getCatalogoId(), i.getCantidad());
    }

    public void eliminar(Integer catalogoId) {
        Inventario i = repository.findByCatalogoId(catalogoId)
                .orElseThrow(() -> new ExcepcionPersonalizada("No se puede eliminar: No existe stock para el ID: " + catalogoId));
        
        repository.delete(i);
    }

    public void descontarStock(List<InventarioRequestDTO> items) {
        for (InventarioRequestDTO item : items) {
            Inventario inventario = repository.findByCatalogoId(item.getCatalogoId())
                    .orElseThrow(() -> new ExcepcionPersonalizada("Producto no encontrado en inventario"));

            if (inventario.getCantidad() < item.getCantidad()) {
                throw new ExcepcionPersonalizada("Stock insuficiente para el producto: " + item.getCatalogoId());
            }


            inventario.setCantidad(inventario.getCantidad() - item.getCantidad());
            repository.save(inventario);
        }

    }
}
