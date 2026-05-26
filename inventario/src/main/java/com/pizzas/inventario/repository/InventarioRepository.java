package com.pizzas.inventario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.inventario.model.Inventario;

@Repository

public interface InventarioRepository extends JpaRepository<Inventario, Integer>{
    Optional<Inventario> findByCatalogoId(Integer catalogoId);

}
