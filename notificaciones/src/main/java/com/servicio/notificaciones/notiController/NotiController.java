package com.servicio.notificaciones.notiController;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.notificaciones.dto.NotiDTO;
import com.servicio.notificaciones.dto.NotiListadoDTO;
import com.servicio.notificaciones.model.Notificacion;
import com.servicio.notificaciones.notiService.NotiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotiController {

    private final NotiService notiService;

    @PostMapping("/enviar")
    public ResponseEntity<Notificacion> enviarNoti(@Valid @RequestBody NotiDTO notiDTO) {
        
        Notificacion guardada = notiService.enviarNoti(notiDTO);
        return ResponseEntity.status(201).body(guardada);
    }
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotiListadoDTO>> buscarPorUsuario(@PathVariable Integer usuarioId) {
        
        List<NotiListadoDTO> lista = notiService.buscarPorUsuario(usuarioId);
        return ResponseEntity.ok(lista);
    }
    
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<Notificacion>> buscarPorPedido(@PathVariable Integer pedidoId) {
    List<Notificacion> lista = notiService.buscarPorPedido(pedidoId);
    if (lista.isEmpty()) {
        return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(lista);
    }
}