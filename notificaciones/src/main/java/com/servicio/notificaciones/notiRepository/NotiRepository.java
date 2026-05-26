package com.servicio.notificaciones.notiRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.servicio.notificaciones.model.Notificacion;




@Repository
public interface  NotiRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByUsuarioId(Integer usuarioId);

    List<Notificacion> findByPedidoId(Integer pedidoId);


}
