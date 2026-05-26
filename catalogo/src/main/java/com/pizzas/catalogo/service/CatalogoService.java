package com.pizzas.catalogo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pizzas.catalogo.exception.ExcepcionPersonalizada;
import com.pizzas.catalogo.model.Catalogo;
import com.pizzas.catalogo.repository.CatalogoRepository;

@Service

public class CatalogoService {
    @Autowired
    private CatalogoRepository repository;

    public List<Catalogo> listar() {
        return repository.findAll();
    }

    public List<Catalogo> buscarPorNombre(String nombre) {
    List<Catalogo> pizzas = repository.findByNombreIgnoreCase(nombre);
    
    if (pizzas.isEmpty()) {
        throw new ExcepcionPersonalizada("No se encontró ninguna pizza llamada: " + nombre);
    }
    
    return pizzas;
}

    public Catalogo buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada("Pizza no encontrada con id: " + id));
    }

    public List<Catalogo> listarPorTipo(String tipo) {
    List<Catalogo> lista = repository.findByTipoIgnoreCase(tipo);
    if (lista.isEmpty()) {
        throw new ExcepcionPersonalizada("No existen pizzas del tipo: " + tipo);
    }
    return lista;
}
    public void eliminarPizzas(Integer id) {
        if (!repository.existsById(id)) {
            throw new ExcepcionPersonalizada("No se puede eliminar: Pizza no encontrada con id: " + id);
        }
        repository.deleteById(id);
    }

    public Catalogo guardarPizzas(Catalogo catalogo) {
        return repository.save(catalogo);
    }

    public void actualizarPizzas(Integer id, Catalogo catalogo) {
        Catalogo c = repository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada("No se puede actualizar: Pizza no encontrada con id: " + id));
        
        c.setNombre(catalogo.getNombre());
        c.setTipo(catalogo.getTipo());
        c.setTamanio(catalogo.getTamanio());
        
        c.setPrecio(catalogo.getPrecio());
        
        repository.save(c);
    }

}
