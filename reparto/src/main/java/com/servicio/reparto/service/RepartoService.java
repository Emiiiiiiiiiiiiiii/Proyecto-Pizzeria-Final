package com.servicio.reparto.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.servicio.reparto.dto.NotiDTO;
import com.servicio.reparto.dto.RepartoRequestDTO;
import com.servicio.reparto.dto.RepartoResponseDTO;
import com.servicio.reparto.exception.ExcepcionPersonalizada;
import com.servicio.reparto.model.Reparto;
import com.servicio.reparto.repository.RepartoRepository;

// Service con la lógica del microservicio reparto
@Service
public class RepartoService {

    private static final Logger logger = LoggerFactory.getLogger(RepartoService.class);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RepartoRepository repository;
    private final RestTemplate restTemplate;

    @Value("${url.pedidos}")
    private String URL_PEDIDOS;

    @Value("${url.auth}")
    private String URL_AUTH;

    @Value("${url.notificaciones}")
    private String URL_NOTIFICACIONES;

    // Constructor para inyectar dependencias
    public RepartoService(RepartoRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // Genera reparto usando datos del pedido
    public RepartoResponseDTO generarReparto(RepartoRequestDTO dto) {
        if (repository.existsByPedidoId(dto.getPedidoId())) {
            throw new ExcepcionPersonalizada(
                    "El pedido con ID " + dto.getPedidoId() + " ya tiene reparto asignado.",
                    HttpStatus.CONFLICT
            );
        }

        Map<String, Object> pedido = obtenerPedido(dto.getPedidoId());

        validarPedidoParaReparto(pedido, dto.getUsuarioId());

        String nombreCliente = normalizarTexto(pedido.get("nombreCliente"), "Cliente sin nombre");
        String emailCliente = normalizarTexto(pedido.get("emailCliente"), null);

        if (emailCliente == null) {
            Map<String, Object> usuario = obtenerUsuario(dto.getUsuarioId());
            emailCliente = normalizarTexto(usuario.get("email"), "cliente@mail.com");
        }

        Reparto reparto = new Reparto();
        reparto.setPedidoId(dto.getPedidoId());
        reparto.setUsuarioId(dto.getUsuarioId());
        reparto.setNombreCliente(nombreCliente);
        reparto.setEmailCliente(emailCliente);
        reparto.setDireccionEntrega(dto.getDireccionEntrega().trim());
        reparto.setRepartidor(dto.getRepartidor().trim());
        reparto.setEstadoReparto("PREPARANDO");
        reparto.setHoraEntrega(LocalDateTime.now().plusMinutes(30).format(FORMATO_FECHA));

        Reparto guardado = repository.save(reparto);

        enviarNotificacionReparto(guardado, "REPARTO", "El reparto del pedido #" + guardado.getPedidoId() + " fue generado.");

        logger.info("Reparto generado correctamente para pedido ID: {}", guardado.getPedidoId());

        return mapearAResponse(guardado);
    }

    // Lista todos los repartos
    public List<RepartoResponseDTO> listarTodos() {
        List<Reparto> repartos = repository.findAll();
        List<RepartoResponseDTO> respuesta = new ArrayList<>();

        for (Reparto reparto : repartos) {
            respuesta.add(mapearAResponse(reparto));
        }

        logger.info("Listado de repartos obtenido. Total: {}", respuesta.size());

        return respuesta;
    }

    // Busca reparto por ID
    public RepartoResponseDTO buscarPorId(Long id) {
        Reparto reparto = repository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada(
                        "Reparto no encontrado con ID: " + id,
                        HttpStatus.NOT_FOUND
                ));

        return mapearAResponse(reparto);
    }

    // Busca reparto por pedido
    public RepartoResponseDTO buscarPorPedido(Integer pedidoId) {
        Reparto reparto = repository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new ExcepcionPersonalizada(
                        "No existe reparto para el pedido con ID: " + pedidoId,
                        HttpStatus.NOT_FOUND
                ));

        return mapearAResponse(reparto);
    }

    // Busca repartos por usuario
    public List<RepartoResponseDTO> buscarPorUsuario(Integer usuarioId) {
        List<Reparto> repartos = repository.findByUsuarioId(usuarioId);

        if (repartos.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "No existen repartos para el usuario con ID: " + usuarioId,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAResponse(repartos);
    }

    // Busca repartos por estado
    public List<RepartoResponseDTO> buscarPorEstado(String estado) {
        String estadoNormalizado = validarEstado(estado);

        List<Reparto> repartos = repository.findByEstadoReparto(estadoNormalizado);

        if (repartos.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "No existen repartos con estado: " + estadoNormalizado,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAResponse(repartos);
    }

    // Actualiza el estado del reparto
    public RepartoResponseDTO actualizarEstadoReparto(Long id, String nuevoEstado) {
        String estadoNormalizado = validarEstado(nuevoEstado);

        Reparto reparto = repository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada(
                        "Reparto no encontrado con ID: " + id,
                        HttpStatus.NOT_FOUND
                ));

        reparto.setEstadoReparto(estadoNormalizado);

        if ("ENTREGADO".equals(estadoNormalizado)) {
            reparto.setHoraEntrega(LocalDateTime.now().format(FORMATO_FECHA));
        }

        Reparto guardado = repository.save(reparto);

        enviarNotificacionReparto(
                guardado,
                "CAMBIO_ESTADO_REPARTO",
                "El reparto del pedido #" + guardado.getPedidoId() + " cambió a: " + guardado.getEstadoReparto()
        );

        logger.info("Estado del reparto {} actualizado a {}", id, estadoNormalizado);

        return mapearAResponse(guardado);
    }

    // Elimina un reparto
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ExcepcionPersonalizada(
                    "No existe el reparto para eliminar.",
                    HttpStatus.NOT_FOUND
            );
        }

        repository.deleteById(id);

        logger.info("Reparto eliminado correctamente con ID: {}", id);
    }

    // Obtiene pedido desde el microservicio pedido
    @SuppressWarnings("unchecked")
    private Map<String, Object> obtenerPedido(Integer pedidoId) {
        try {
            Map<String, Object> respuesta = restTemplate.getForObject(URL_PEDIDOS + pedidoId, Map.class);

            if (respuesta == null) {
                throw new ExcepcionPersonalizada("Respuesta vacía del microservicio pedido.", HttpStatus.BAD_GATEWAY);
            }

            Object datosPedido = respuesta.containsKey("pedido") ? respuesta.get("pedido") : respuesta;

            if (!(datosPedido instanceof Map)) {
                throw new ExcepcionPersonalizada(
                        "La respuesta del microservicio pedido no tiene el formato esperado.",
                        HttpStatus.BAD_GATEWAY
                );
            }

            return (Map<String, Object>) datosPedido;

        } catch (HttpClientErrorException.NotFound e) {
            throw new ExcepcionPersonalizada(
                    "El pedido con ID " + pedidoId + " no existe.",
                    HttpStatus.NOT_FOUND
            );
        } catch (ResourceAccessException e) {
            throw new ExcepcionPersonalizada(
                    "No se pudo validar el pedido porque el microservicio pedido no responde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientException e) {
            throw new ExcepcionPersonalizada(
                    "Ocurrió un error al obtener el pedido.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Obtiene usuario desde autenticación
    @SuppressWarnings("unchecked")
    private Map<String, Object> obtenerUsuario(Integer usuarioId) {
        try {
            Map<String, Object> respuesta = restTemplate.getForObject(URL_AUTH + usuarioId, Map.class);

            if (respuesta == null) {
                throw new ExcepcionPersonalizada("Respuesta vacía del microservicio autenticación.", HttpStatus.BAD_GATEWAY);
            }

            Object datosUsuario = respuesta.containsKey("usuario") ? respuesta.get("usuario") : respuesta;

            if (!(datosUsuario instanceof Map)) {
                throw new ExcepcionPersonalizada(
                        "La respuesta del microservicio autenticación no tiene el formato esperado.",
                        HttpStatus.BAD_GATEWAY
                );
            }

            return (Map<String, Object>) datosUsuario;

        } catch (HttpClientErrorException.NotFound e) {
            throw new ExcepcionPersonalizada(
                    "El usuario con ID " + usuarioId + " no existe.",
                    HttpStatus.NOT_FOUND
            );
        } catch (ResourceAccessException e) {
            throw new ExcepcionPersonalizada(
                    "No se pudo validar el usuario porque autenticación no responde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientException e) {
            throw new ExcepcionPersonalizada(
                    "Ocurrió un error al obtener el usuario.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Valida que el pedido sea correcto para reparto
    private void validarPedidoParaReparto(Map<String, Object> pedido, Integer usuarioIdRequest) {
        Integer usuarioIdPedido = convertirAEntero(pedido.get("usuarioId"));

        if (usuarioIdPedido != null && !usuarioIdPedido.equals(usuarioIdRequest)) {
            throw new ExcepcionPersonalizada(
                    "El usuario enviado no coincide con el usuario del pedido.",
                    HttpStatus.BAD_REQUEST
            );
        }

        String estadoPedido = normalizarTexto(pedido.get("estado"), "");

        if ("CANCELADO".equalsIgnoreCase(estadoPedido) || "RECHAZADO".equalsIgnoreCase(estadoPedido)) {
            throw new ExcepcionPersonalizada(
                    "No se puede generar reparto para un pedido en estado: " + estadoPedido,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // Envía notificación sin romper reparto si falla
    private void enviarNotificacionReparto(Reparto reparto, String tipo, String mensaje) {
        try {
            NotiDTO noti = new NotiDTO();
            noti.setPedidoId(reparto.getPedidoId());
            noti.setUsuarioId(reparto.getUsuarioId());
            noti.setTipo(tipo);
            noti.setDestinatario(reparto.getEmailCliente());
            noti.setMensaje(mensaje);
            noti.setFecha(LocalDateTime.now().format(FORMATO_FECHA));

            restTemplate.postForObject(URL_NOTIFICACIONES + "enviar", noti, Object.class);

            logger.info("Notificación enviada para reparto del pedido ID: {}", reparto.getPedidoId());

        } catch (Exception e) {
            logger.warn("No se pudo enviar notificación de reparto: {}", e.getMessage());
        }
    }

    // Convierte entidad a DTO de respuesta
    private RepartoResponseDTO mapearAResponse(Reparto reparto) {
        return new RepartoResponseDTO(
                reparto.getId(),
                reparto.getPedidoId(),
                reparto.getUsuarioId(),
                reparto.getNombreCliente(),
                reparto.getEmailCliente(),
                reparto.getDireccionEntrega(),
                reparto.getEstadoReparto(),
                reparto.getRepartidor(),
                reparto.getHoraEntrega()
        );
    }

    // Convierte lista de entidades a DTO
    private List<RepartoResponseDTO> mapearListaAResponse(List<Reparto> repartos) {
        List<RepartoResponseDTO> respuesta = new ArrayList<>();

        for (Reparto reparto : repartos) {
            respuesta.add(mapearAResponse(reparto));
        }

        return respuesta;
    }

    // Valida estados permitidos
    private String validarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ExcepcionPersonalizada(
                    "El estado del reparto es obligatorio.",
                    HttpStatus.BAD_REQUEST
            );
        }

        String estadoNormalizado = estado.trim().toUpperCase();

        List<String> estadosPermitidos = List.of(
                "PREPARANDO",
                "EN_CAMINO",
                "ENTREGADO",
                "CANCELADO",
                "RETRASADO"
        );

        if (!estadosPermitidos.contains(estadoNormalizado)) {
            throw new ExcepcionPersonalizada(
                    "Estado de reparto no válido: " + estado,
                    HttpStatus.BAD_REQUEST
            );
        }

        return estadoNormalizado;
    }

    // Convierte valores a Integer
    private Integer convertirAEntero(Object valor) {
        if (valor instanceof Integer) {
            return (Integer) valor;
        }

        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }

        return null;
    }

    // Normaliza texto desde objetos
    private String normalizarTexto(Object valor, String valorPorDefecto) {
        if (valor == null || valor.toString().isBlank()) {
            return valorPorDefecto;
        }

        return valor.toString().trim();
    }
}