package com.servicio.notificaciones.notiRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.servicio.notificaciones.model.Notificacion;

// Repository para consultar notificaciones
@Repository
public interface  NotiRepository extends JpaRepository<Notificacion, Integer> {
    // Lista notificaciones de un usuario
    List<Notificacion> findByUsuarioId(Integer usuarioId);

    // Lista notificaciones asociadas a un pedido
    List<Notificacion> findByPedidoId(Integer pedidoId);

    // Lista notificaciones por tipo
    List<Notificacion> findByTipoIgnoreCase(String tipo);

    // Lista notificaciones por destinatario
    List<Notificacion> findByDestinatarioIgnoreCase(String destinatario);

    // Lista notificaciones por estado
    List<Notificacion> findByEstadoIgnoreCase(String estado);


}
