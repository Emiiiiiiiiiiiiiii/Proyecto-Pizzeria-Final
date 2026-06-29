package com.servicio.notificaciones.notiService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.servicio.notificaciones.dto.NotiDTO;
import com.servicio.notificaciones.dto.NotiListadoDTO;
import com.servicio.notificaciones.dto.NotiResponseDTO;
import com.servicio.notificaciones.exception.ExcepcionPersonalizada;
import com.servicio.notificaciones.model.Notificacion;
import com.servicio.notificaciones.notiRepository.NotiRepository;

// Service con la lógica del microservicio notificaciones
@Service
public class NotiService {

    private static final Logger logger = LoggerFactory.getLogger(NotiService.class);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NotiRepository repository;

    // Constructor para inyectar repository
    public NotiService(NotiRepository repository) {
        this.repository = repository;
    }

    // Registra una notificación como enviada
    public NotiResponseDTO enviar(NotiDTO dto) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(dto.getUsuarioId());
        notificacion.setPedidoId(dto.getPedidoId());
        notificacion.setTipo(normalizarTipo(dto.getTipo()));
        notificacion.setDestinatario(dto.getDestinatario().trim());
        notificacion.setMensaje(obtenerMensaje(dto));
        notificacion.setEstado("ENVIADA");
        notificacion.setFechaEnvio(obtenerFecha(dto.getFecha()));

        Notificacion guardada = repository.save(notificacion);

        logger.info(
                "Notificación registrada para usuario ID: {}, tipo: {}",
                guardada.getUsuarioId(),
                guardada.getTipo()
        );

        return mapearAResponse(guardada);
    }

    // Lista todas las notificaciones
    public List<NotiListadoDTO> listarTodas() {
        List<Notificacion> notificaciones = repository.findAll();
        List<NotiListadoDTO> respuesta = new ArrayList<>();

        for (Notificacion notificacion : notificaciones) {
            respuesta.add(mapearAListado(notificacion));
        }

        logger.info("Listado de notificaciones obtenido. Total: {}", respuesta.size());

        return respuesta;
    }

    // Busca notificación por ID
    public NotiResponseDTO buscarPorId(Integer id) {
        Notificacion notificacion = repository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada(
                        "Notificación no encontrada con ID: " + id,
                        HttpStatus.NOT_FOUND
                ));

        return mapearAResponse(notificacion);
    }

    // Busca notificaciones por usuario
    public List<NotiListadoDTO> buscarPorUsuario(Integer usuarioId) {
        List<Notificacion> notificaciones = repository.findByUsuarioId(usuarioId);

        if (notificaciones.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "No existen notificaciones para el usuario con ID: " + usuarioId,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAListado(notificaciones);
    }

    // Busca notificaciones por pedido
    public List<NotiListadoDTO> buscarPorPedido(Integer pedidoId) {
        List<Notificacion> notificaciones = repository.findByPedidoId(pedidoId);

        if (notificaciones.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "No existen notificaciones para el pedido con ID: " + pedidoId,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAListado(notificaciones);
    }

    // Busca notificaciones por tipo
    public List<NotiListadoDTO> buscarPorTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new ExcepcionPersonalizada(
                    "El tipo de notificación es obligatorio.",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<Notificacion> notificaciones = repository.findByTipoIgnoreCase(tipo.trim());

        if (notificaciones.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "No existen notificaciones del tipo: " + tipo,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAListado(notificaciones);
    }

    // Busca notificaciones por destinatario
    public List<NotiListadoDTO> buscarPorDestinatario(String destinatario) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new ExcepcionPersonalizada(
                    "El destinatario es obligatorio.",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<Notificacion> notificaciones = repository.findByDestinatarioIgnoreCase(destinatario.trim());

        if (notificaciones.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "No existen notificaciones para el destinatario: " + destinatario,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAListado(notificaciones);
    }

    // Busca notificaciones por estado
    public List<NotiListadoDTO> buscarPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ExcepcionPersonalizada(
                    "El estado es obligatorio.",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<Notificacion> notificaciones = repository.findByEstadoIgnoreCase(estado.trim());

        if (notificaciones.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "No existen notificaciones con estado: " + estado,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAListado(notificaciones);
    }

    // Elimina una notificación por ID
    public void eliminar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ExcepcionPersonalizada(
                    "No existe la notificación para eliminar.",
                    HttpStatus.NOT_FOUND
            );
        }

        repository.deleteById(id);

        logger.info("Notificación eliminada correctamente con ID: {}", id);
    }

    // Obtiene mensaje enviado o genera uno automático
    private String obtenerMensaje(NotiDTO dto) {
        if (dto.getMensaje() != null && !dto.getMensaje().isBlank()) {
            return dto.getMensaje().trim();
        }

        String tipo = normalizarTipo(dto.getTipo());

        if ("REGISTRO".equals(tipo)) {
            return "Registro realizado correctamente.";
        }

        if ("PAGO".equals(tipo)) {
            return "Pago procesado correctamente.";
        }

        if ("REPARTO".equals(tipo)) {
            return "Reparto generado correctamente.";
        }

        if ("CAMBIO_ESTADO_REPARTO".equals(tipo)) {
            return "El estado del reparto fue actualizado.";
        }

        if ("PEDIDO".equals(tipo)) {
            return "Pedido registrado correctamente.";
        }

        if ("CERTIFICACION".equals(tipo)) {
            return "Certificación generada correctamente.";
        }

        if ("RESENA".equals(tipo)) {
            return "Reseña registrada correctamente.";
        }

        return "Notificación registrada correctamente.";
    }

    // Usa fecha recibida o fecha actual
    private LocalDateTime obtenerFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(fecha);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(fecha, FORMATO_FECHA);
            } catch (DateTimeParseException ex) {
                logger.warn("Fecha recibida con formato inválido, se usará fecha actual: {}", fecha);
                return LocalDateTime.now();
            }
        }
    }

    // Normaliza el tipo de notificación
    private String normalizarTipo(String tipo) {
        return tipo.trim().toUpperCase();
    }

    // Convierte entidad a DTO de respuesta
    private NotiResponseDTO mapearAResponse(Notificacion notificacion) {
        return new NotiResponseDTO(
                notificacion.getId(),
                notificacion.getUsuarioId(),
                notificacion.getPedidoId(),
                notificacion.getTipo(),
                notificacion.getDestinatario(),
                notificacion.getMensaje(),
                notificacion.getEstado(),
                notificacion.getFechaEnvio()
        );
    }

    // Convierte entidad a DTO de listado
    private NotiListadoDTO mapearAListado(Notificacion notificacion) {
        return new NotiListadoDTO(
                notificacion.getId(),
                notificacion.getUsuarioId(),
                notificacion.getPedidoId(),
                notificacion.getTipo(),
                notificacion.getDestinatario(),
                notificacion.getEstado(),
                notificacion.getFechaEnvio()
        );
    }

    // Convierte lista de entidades a listado DTO
    private List<NotiListadoDTO> mapearListaAListado(List<Notificacion> notificaciones) {
        List<NotiListadoDTO> respuesta = new ArrayList<>();

        for (Notificacion notificacion : notificaciones) {
            respuesta.add(mapearAListado(notificacion));
        }

        return respuesta;
    }
}