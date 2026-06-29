package com.servicio.reparto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.servicio.reparto.model.Reparto;

// Repository para consultar repartos
@Repository
public interface RepartoRepository extends JpaRepository<Reparto, Long> {

    // Busca reparto por ID de pedido
    Optional<Reparto> findByPedidoId(Integer pedidoId);

    // Lista repartos de un usuario
    List<Reparto> findByUsuarioId(Integer usuarioId);

    // Lista repartos por repartidor
    List<Reparto> findByRepartidorIgnoreCase(String repartidor);

    // Lista repartos por estado
    List<Reparto> findByEstadoReparto(String estadoReparto);

    // Verifica si un pedido ya tiene reparto asignado
    boolean existsByPedidoId(Integer pedidoId);

}
