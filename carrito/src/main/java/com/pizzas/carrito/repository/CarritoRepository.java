package com.pizzas.carrito.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.carrito.model.Carrito;

@Repository

public interface CarritoRepository extends JpaRepository<Carrito, Integer>{
    Carrito findByUsuarioIdAndCatalogoId(Integer usuarioId, Integer catalogoId);
    
    List<Carrito> findByUsuarioId(Integer usuarioId);

}
