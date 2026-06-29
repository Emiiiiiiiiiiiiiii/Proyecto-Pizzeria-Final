package com.pizzas.certificacion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.certificacion.model.Certificacion;

// Repository para consultar certificaciones
@Repository
public interface CertificacionRepository extends JpaRepository<Certificacion, Integer> {
    // Busca una certificación por el ID del pedido
    Optional<Certificacion> findByPedidoId(Integer pedidoId);

    // Lista certificaciones asociadas a un usuario
    List<Certificacion> findByUsuarioId(Integer usuarioId);

    // Lista certificaciones según el estado del pedido
    List<Certificacion> findByEstadoPedido(String estadoPedido);

    // Lista certificaciones que tienen o no tienen reseña
    List<Certificacion> findByTieneResena(Boolean tieneResena);

    // Verifica si ya existe una certificación para un pedido
    boolean existsByPedidoId(Integer pedidoId);

}
