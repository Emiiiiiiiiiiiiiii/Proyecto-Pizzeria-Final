package com.pizzas.certificacion.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pizzas.certificacion.dto.CertificacionRequestDTO;
import com.pizzas.certificacion.dto.PedidoDTO;
import com.pizzas.certificacion.dto.RepartoDTO;
import com.pizzas.certificacion.exception.ExcepcionPersonalizada;
import com.pizzas.certificacion.model.Certificacion;
import com.pizzas.certificacion.repository.CertificacionRepository;

@Service

public class CertificacionService {
    @Autowired
    private CertificacionRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url.pedidos}") private String URL_PEDIDOS;
    @Value("${url.reparto}") private String URL_REPARTO;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    
    public CertificacionResponseDTO mapearAResponse(Certificacion cert) {
        return new CertificacionResponseDTO(
            cert.getId(), cert.getPedidoId(), cert.getUsuarioId(), 
            cert.getNombreUsuario(), cert.getEmailUsuario(), cert.getNombreRepartidor(), 
            cert.getFechaPedido(), cert.getHoraEntrega(), cert.getEstadoPuntualidad(), cert.getFechaEmision()
        );
    }

    //GENERAR
    public CertificacionResponseDTO generarCertificacion(CertificacionRequestDTO request) {
        if (!repository.findByPedidoId(request.getPedidoId()).isEmpty()) {
            throw new ExcepcionPersonalizada("Ya existe una certificación para este pedido.", HttpStatus.CONFLICT);
        }

        PedidoDTO pedido = restTemplate.getForObject(URL_PEDIDOS + request.getPedidoId(), PedidoDTO.class);
        if (pedido == null) throw new ExcepcionPersonalizada("Pedido no encontrado", HttpStatus.NOT_FOUND);

        RepartoDTO reparto = restTemplate.getForObject(URL_REPARTO + "por-pedido/" + request.getPedidoId(), RepartoDTO.class);
        if (reparto == null) throw new ExcepcionPersonalizada("Información de reparto no encontrada", HttpStatus.NOT_FOUND);

        
        LocalDateTime fechaPedido = LocalDateTime.parse(pedido.getFechaPedido(), formatter);
        LocalDateTime horaEntrega = LocalDateTime.parse(reparto.getHoraEntrega(), formatter);
        String estadoPuntualidad = horaEntrega.isBefore(fechaPedido.plusMinutes(45)) ? "A TIEMPO" : "CON RETRASO";

        Certificacion cert = new Certificacion();
        cert.setPedidoId(request.getPedidoId());
        cert.setUsuarioId(pedido.getUsuarioId());
        cert.setFechaPedido(pedido.getFechaPedido());
        cert.setNombreUsuario(pedido.getNombreCliente());
        cert.setEmailUsuario(pedido.getEmailCliente());
        cert.setNombreRepartidor(reparto.getRepartidor());
        cert.setHoraEntrega(reparto.getHoraEntrega());
        cert.setEstadoPuntualidad(estadoPuntualidad);
        cert.setFechaEmision(LocalDateTime.now().format(formatter));

        return mapearAResponse(repository.save(cert));
    }

    // 2. LISTAR TODOS
    public List<CertificacionResponseDTO> listarTodas() {
        return repository.findAll().stream().map(this::mapearAResponse).collect(Collectors.toList());
    }

    // 3. BUSCAR POR ID
    public CertificacionResponseDTO buscarPorId(Integer id) {
        Certificacion cert = repository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada("Certificación no encontrada con ID: " + id, HttpStatus.NOT_FOUND));
        return mapearAResponse(cert);
    }

    // 4. BUSCAR POR USUARIO
    public List<CertificacionResponseDTO> buscarPorUsuario(Integer usuarioId) {
        List<Certificacion> lista = repository.findByUsuarioId(usuarioId);
        if (lista.isEmpty()) throw new ExcepcionPersonalizada("No hay certificados para este usuario", HttpStatus.NOT_FOUND);
        return lista.stream().map(this::mapearAResponse).collect(Collectors.toList());
    }

    // 5. ELIMINAR
    public void eliminar(Integer id) {
        if (!repository.existsById(id)) throw new ExcepcionPersonalizada("ID no encontrado", HttpStatus.NOT_FOUND);
        repository.deleteById(id);
    }

}
