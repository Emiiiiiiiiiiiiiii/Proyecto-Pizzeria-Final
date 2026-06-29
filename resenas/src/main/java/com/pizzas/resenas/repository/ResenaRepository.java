package com.pizzas.resenas.repository;

import com.pizzas.resenas.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {
    
    // Buscar todas las reseñas de un usuario específico
    List<Resena> findByUsuarioId(Integer usuarioId);
    
    // Buscar todas las reseñas de una pizza (catálogo)
    List<Resena> findByCatalogoId(Integer catalogoId);
    
    // Buscar una reseña específica de un pedido
    List<Resena> findByPedidoId(Integer pedidoId);
}