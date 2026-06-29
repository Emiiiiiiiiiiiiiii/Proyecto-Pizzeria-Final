package com.pago.service.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pago.service.model.Pagos;

// Repository para consultar pagos
@Repository
public interface PagosRepository extends JpaRepository<Pagos, Integer> {

    // Busca un pago por pedido
    Optional<Pagos> findByPedidoId(Integer pedidoId);

    // Lista pagos por usuario
    List<Pagos> findByUsuarioId(Integer usuarioId);

    // Lista pagos por estado
    List<Pagos> findByEstado(String estado);

    // Verifica si un pedido ya tiene pago registrado
    boolean existsByPedidoId(Integer pedidoId);

}
