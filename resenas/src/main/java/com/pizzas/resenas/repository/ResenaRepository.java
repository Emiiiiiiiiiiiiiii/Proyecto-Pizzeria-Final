package com.pizzas.resenas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.resenas.model.Resena;

// Repository para consultar reseñas
@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {
    
    // Busca reseña por pedido
    Optional<Resena> findByPedidoId(Integer pedidoId);

    // Lista reseñas de un usuario
    List<Resena> findByUsuarioId(Integer usuarioId);

    // Lista reseñas por cantidad de estrellas
    List<Resena> findByEstrellas(Integer estrellas);

    // Verifica si un pedido ya tiene reseña
    boolean existsByPedidoId(Integer pedidoId);

    // Verifica si un usuario ya reseñó un pedido
    boolean existsByPedidoIdAndUsuarioId(Integer pedidoId, Integer usuarioId);
}