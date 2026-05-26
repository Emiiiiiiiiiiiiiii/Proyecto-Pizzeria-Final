package com.servicio.reparto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.servicio.reparto.model.Reparto;


@Repository
public interface RepartoRepository extends JpaRepository<Reparto, Long> {

    Optional<Reparto> findByPedidoId(Long pedidoId);

    List<Reparto> findByRepartidor(String repartidor);

}
