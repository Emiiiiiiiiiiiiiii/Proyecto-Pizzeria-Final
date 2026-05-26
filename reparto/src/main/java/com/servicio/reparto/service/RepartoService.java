package com.servicio.reparto.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.servicio.reparto.dto.RepartoDTO;
import com.servicio.reparto.model.Reparto;
import com.servicio.reparto.repository.RepartoRepository;

@Service
public class RepartoService {

    @Autowired
    private RepartoRepository repartoRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String URL_PEDIDOS = "http://localhost:8083/pedidos/";
    private final String URL_AUTH = "http://localhost:8080/auth/usuarios/";
    private final String URL_NOTI = "http://localhost:8086/notificaciones/enviar";

    public Reparto generarReparto(RepartoDTO dto) {
    
    try {
        Map pedidoExistente = restTemplate.getForObject(URL_PEDIDOS + dto.getPedidoId(), Map.class);
        if (pedidoExistente == null) {
            System.out.println("Pedido no encontrado en el sistema.");
            return null;
        }
    } catch (Exception e) {
        System.out.println("Servicio de pedidos caido, operando en contingencia.");
    }

    String nombre = "Usuario Desconocido";
    String email = "cliente@mail.com";
    try {
        Map usuarioReal = restTemplate.getForObject(URL_AUTH + dto.getUsuarioId(), Map.class);
        if (usuarioReal != null) {
            nombre = usuarioReal.get("nombre") + " " + usuarioReal.get("apellido");
            email = (String) usuarioReal.get("email");
        }
    } catch (Exception e) {
        System.out.println("Error conectando con modulo de Autenticacion.");
    }

    Reparto reparto = new Reparto();
    reparto.setPedidoId(dto.getPedidoId());
    reparto.setUsuarioId(dto.getUsuarioId());
    reparto.setNombreCliente(nombre);
    reparto.setEmailCliente(email);
    reparto.setDireccionEntrega(dto.getDireccionEntrega());
    
    
    reparto.setRepartidor(dto.getRepartidor());
    
    reparto.setEstadoReparto("PREPARANDO");
    
    
    reparto.setHoraEntrega(LocalDateTime.now().plusMinutes(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    
    Reparto guardado = repartoRepository.save(reparto);

    try {
        Map<String, Object> peticionNoti = new HashMap<>();
        peticionNoti.put("tipo", "REPARTO");
        peticionNoti.put("destinatario", email);
        peticionNoti.put("pedidoId", guardado.getPedidoId());
        peticionNoti.put("usuarioId", guardado.getUsuarioId());
        
        restTemplate.postForObject(URL_NOTI, peticionNoti, String.class);
    } catch (Exception e) {
        System.out.println("No se pudo enviar la alerta al MS de Notificaciones.");
    }

    return guardado;
}

    public List<Reparto> buscarTodosLosDespachos() {
        return repartoRepository.findAll();
    }

    public Optional<Reparto> buscarDespachoPorId(Long id) {
        return repartoRepository.findById(id);
    }

    public Reparto actualizarEstadoReparto(Long id, String nuevoEstado) {
        Reparto reparto = repartoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despacho no encontrado con el ID: " + id));
        
        reparto.setEstadoReparto(nuevoEstado.toUpperCase());
        
        Reparto guardado = repartoRepository.save(reparto);
        
        try {
            Map<String, Object> peticionNoti = new HashMap<>();
            peticionNoti.put("tipo", "CAMBIO_ESTADO_REPARTO");
            peticionNoti.put("destinatario", guardado.getEmailCliente());
            peticionNoti.put("pedidoId", guardado.getPedidoId());
            peticionNoti.put("usuarioId", guardado.getUsuarioId());
            peticionNoti.put("mensaje", "El reparto del pedido #" + guardado.getPedidoId() + " cambió a: " + guardado.getEstadoReparto());
            
            restTemplate.postForObject(URL_NOTI, peticionNoti, String.class);
        } catch (Exception e) {
            System.out.println("Aviso: No se pudo enviar la alerta de cambio de estado a Notificaciones.");
        }
        
        return guardado;
    }
}