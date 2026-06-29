package com.pizzas.inventario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.inventario.model.Inventario;

// Repository para consultar inventario
@Repository

public interface InventarioRepository extends JpaRepository<Inventario, Integer>{
    // Busca stock usando el ID del producto en catálogo
    Optional<Inventario> findByCatalogoId(Integer catalogoId);

    // Verifica si ya existe inventario para un producto del catálogo
    boolean existsByCatalogoId(Integer catalogoId);

    // Lista productos con stock exacto
    List<Inventario> findByCantidad(Integer cantidad);

    // Lista productos con stock menor a una cantidad
    List<Inventario> findByCantidadLessThan(Integer cantidad);

}
