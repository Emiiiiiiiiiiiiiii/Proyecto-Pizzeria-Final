package com.pago.service.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pago.service.model.Pagos;



@Repository
public interface PagosRepository extends JpaRepository<Pagos, Integer> {

    Optional<Pagos> findByPedidoId(Integer pedidoId);

    List<Pagos> findByUsuarioId(Integer usuarioId);

}
