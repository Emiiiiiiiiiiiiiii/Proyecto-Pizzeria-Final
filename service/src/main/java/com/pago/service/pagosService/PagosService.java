package com.pago.service.pagosService;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pago.service.ExcepcionPersonalizada;
import com.pago.service.dto.NotiDTO;
import com.pago.service.dto.PagosDTO;
import com.pago.service.model.Pagos;
import com.pago.service.repository.PagosRepository;

@Service
public class PagosService {
    private final PagosRepository pagosRepository;
    private final RestTemplate restTemplate;

    @Value("${url.auth}")
    private String URL_AUTH;

    @Value("${url.notificaciones}")
    private String URL_NOTIFICACIONES;

    @Value("${url.pedidos}") private String URL_PEDIDOS;

    public PagosService(PagosRepository pagosRepository, RestTemplate restTemplate) {
        this.pagosRepository = pagosRepository;
        this.restTemplate = restTemplate;
    }

    
    public Map<String, Object> generarPago(PagosDTO dto) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        Map<String, Object> usuario = null;

        
        try {
            usuario = restTemplate.getForObject(URL_AUTH + "usuarios/" + dto.getUsuarioId(), Map.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            throw new ExcepcionPersonalizada("El usuario con ID " + dto.getUsuarioId() + " no existe.");
        } catch (Exception e) {
            throw new ExcepcionPersonalizada("Error crítico: No se pudo conectar con el servicio de Autenticación.");
        }

        if (usuario == null) {
            throw new ExcepcionPersonalizada("Respuesta vacía del servicio de usuarios.");
        }

        
        Integer montoCalculado = 0;
        try {
            
            Map pedidoReal = restTemplate.getForObject(URL_PEDIDOS + dto.getPedidoId(), Map.class);
            if (pedidoReal != null && pedidoReal.get("montoTotal") != null) {
                montoCalculado = (Integer) pedidoReal.get("montoTotal");
            } else {
                throw new ExcepcionPersonalizada("El pedido no registra un monto válido.");
            }
        } catch (Exception e) {
            throw new ExcepcionPersonalizada("Error al recuperar el monto: El pedido #" + dto.getPedidoId() + " no existe o el servicio está caído.");
        }

        
        Pagos pago = new Pagos();
        pago.setMonto(montoCalculado);
        pago.setPedidoId(dto.getPedidoId());
        pago.setUsuarioId(dto.getUsuarioId());
        pago.setEstado("APROBADO");
        pago.setFechaPago(LocalDateTime.now());
        
        Pagos pagoGuardado = pagosRepository.save(pago);

        try {
            NotiDTO noti = new NotiDTO();
            noti.setPedidoId(dto.getPedidoId());
            noti.setTipo("PAGO");
            noti.setDestinatario(usuario.get("email").toString());
            noti.setFecha(LocalDate.now().toString());
            noti.setUsuarioId(dto.getUsuarioId());

            restTemplate.postForObject(URL_NOTIFICACIONES + "notificaciones/enviar", noti, Object.class);
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo enviar la notificación: " + e.getMessage());
        }

        respuesta.put("status", "APROBADO");
        respuesta.put("idPago", pagoGuardado.getId());
        respuesta.put("montoAprobado", pagoGuardado.getMonto());
        respuesta.put("nombreUsuario", usuario.get("nombre").toString());
        
        respuesta.put("mensaje", "Pago aprobado por un monto de $" + pagoGuardado.getMonto() + " para el pedido #" + pagoGuardado.getPedidoId());
        
        return respuesta;
    }
    public Pagos buscarPorId(Integer id) {
        return pagosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con el ID: " + id));
    }

    public void eliminarPorId(Integer id) {
        if (!pagosRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar, el ID de pago no existe.");
        }
        pagosRepository.deleteById(id);
    }

    public List<Pagos> listarTodos() {
        return pagosRepository.findAll();
    }


}
