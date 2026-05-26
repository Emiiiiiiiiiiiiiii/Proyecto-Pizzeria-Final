package com.pizzas.pedido.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.pedido.model.Pedido;

@Repository

public interface PedidoRepository extends JpaRepository<Pedido, Integer>{
    List<Pedido> findByUsuarioId(Integer usuarioId);
    List<Pedido> findByEstado(String estado);

}
