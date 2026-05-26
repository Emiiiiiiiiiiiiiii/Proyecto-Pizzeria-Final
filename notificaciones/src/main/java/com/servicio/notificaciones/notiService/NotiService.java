package com.servicio.notificaciones.notiService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.servicio.notificaciones.dto.NotiDTO;
import com.servicio.notificaciones.dto.NotiListadoDTO;
import com.servicio.notificaciones.model.Notificacion;
import com.servicio.notificaciones.notiRepository.NotiRepository;

@Service
public class NotiService {

    private final NotiRepository notiRepository;

    @Autowired
    public NotiService(NotiRepository notiRepository) {
        this.notiRepository = notiRepository;
    }

    public Notificacion enviarNoti(NotiDTO dto) {
        String mensajeFinal = "";

        if (dto.getTipo().equalsIgnoreCase("PAGO")) {
            mensajeFinal = "Pago exitoso! tu pizza ya esta en el horno";
        } else if (dto.getTipo().equalsIgnoreCase("REPARTO")) {
            mensajeFinal = "El repartidor esta en camino!";
        } else if (dto.getTipo().equalsIgnoreCase("REGISTRO")) {
            mensajeFinal = "BIENVENIDO A LAS GORDAS PIZZAS";
        }

        Notificacion nuevaNoti = new Notificacion();
        nuevaNoti.setPedidoId(dto.getPedidoId());
        nuevaNoti.setTipo(dto.getTipo());
        nuevaNoti.setDestinatario(dto.getDestinatario());
        nuevaNoti.setFecha(dto.getFecha());
        nuevaNoti.setUsuarioId(dto.getUsuarioId());
        nuevaNoti.setMensaje(mensajeFinal);

        if (dto.getFecha() == null || dto.getFecha().trim().isEmpty()) {
        nuevaNoti.setFecha(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    } else {
        nuevaNoti.setFecha(dto.getFecha());
    }

    return notiRepository.save(nuevaNoti);

    }

    
    public List<NotiListadoDTO> buscarPorUsuario(Integer usuarioId) {
        List<Notificacion> notis = notiRepository.findByUsuarioId(usuarioId);
        List<NotiListadoDTO> listaDTO = new ArrayList<>();

        for (Notificacion n : notis) {
            NotiListadoDTO dto = new NotiListadoDTO();
            dto.setPedidoId(n.getPedidoId());
            dto.setTipoNoti(n.getTipo());
            dto.setDestinatario(n.getDestinatario());
            dto.setMensaje(n.getMensaje());
            listaDTO.add(dto);
        }
        return listaDTO;
    }

    
    public List<Notificacion> buscarPorPedido(Integer pedidoId) {
        return notiRepository.findByPedidoId(pedidoId);
    }
}