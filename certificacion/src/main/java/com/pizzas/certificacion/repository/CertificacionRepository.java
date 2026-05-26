package com.pizzas.certificacion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.certificacion.model.Certificacion;

@Repository

public interface CertificacionRepository extends JpaRepository<Certificacion, Integer> {
    List<Certificacion> findByPedidoId(Integer pedidoId);
    
    List<Certificacion> findByEmailUsuario(String emailUsuario);

    List<Certificacion> findByUsuarioId(Integer usuarioId);

}
