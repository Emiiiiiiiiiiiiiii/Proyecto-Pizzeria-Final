package com.pago.service.pagosService;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.pago.service.ExcepcionPersonalizada;
import com.pago.service.dto.NotiDTO;
import com.pago.service.dto.PagosDTO;
import com.pago.service.dto.PagosResponseDTO;
import com.pago.service.model.Pagos;
import com.pago.service.repository.PagosRepository;

@Service
public class PagosService {
    // Logger para registrar eventos importantes
    private static final Logger logger = LoggerFactory.getLogger(PagosService.class);

    // Formato para fechas enviadas a notificaciones
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Repository de pagos
    private final PagosRepository pagosRepository;

    // Cliente para comunicarse con otros microservicios
    private final RestTemplate restTemplate;

    @Value("${url.auth}")
    private String URL_AUTH;

    @Value("${url.notificaciones}")
    private String URL_NOTIFICACIONES;

    // Constructor para inyectar dependencias
    public PagosService(PagosRepository pagosRepository, RestTemplate restTemplate) {
        this.pagosRepository = pagosRepository;
        this.restTemplate = restTemplate;
    }

    // Procesa un pago enviado desde el microservicio pedido
    @Transactional
    public Map<String, Object> generarPago(PagosDTO dto) {

        if (pagosRepository.existsByPedidoId(dto.getPedidoId())) {
            logger.warn("Intento de duplicar pago para pedido ID: {}", dto.getPedidoId());
            throw new ExcepcionPersonalizada(
                    "El pedido con ID " + dto.getPedidoId() + " ya tiene un pago registrado.",
                    HttpStatus.CONFLICT
            );
        }

        Map<String, Object> usuario = obtenerUsuario(dto.getUsuarioId());

        Pagos pago = new Pagos();
        pago.setMonto(dto.getMonto());
        pago.setPedidoId(dto.getPedidoId());
        pago.setUsuarioId(dto.getUsuarioId());
        pago.setEstado("APROBADO");
        pago.setFechaPago(LocalDateTime.now());

        Pagos pagoGuardado = pagosRepository.save(pago);

        enviarNotificacionPago(pagoGuardado, usuario);

        String nombreUsuario = normalizarTexto(usuario.get("nombre"), "Usuario sin nombre");

        PagosResponseDTO pagoResponse = mapearAResponse(
                pagoGuardado,
                nombreUsuario,
                "Pago aprobado correctamente"
        );

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("status", "APROBADO");
        respuesta.put("mensaje", "Pago aprobado correctamente");
        respuesta.put("idPago", pagoGuardado.getId());
        respuesta.put("montoAprobado", pagoGuardado.getMonto());
        respuesta.put("pedidoId", pagoGuardado.getPedidoId());
        respuesta.put("usuarioId", pagoGuardado.getUsuarioId());
        respuesta.put("nombreUsuario", nombreUsuario);
        respuesta.put("pago", pagoResponse);

        logger.info("Pago aprobado para pedido ID: {}", pagoGuardado.getPedidoId());

        return respuesta;
    }

    // Lista todos los pagos
    public List<Pagos> listarTodos() {
        List<Pagos> pagos = pagosRepository.findAll();

        logger.info("Listado de pagos obtenido. Total: {}", pagos.size());

        return pagos;
    }

    // Busca un pago por ID
    public Pagos buscarPorId(Integer id) {
        return pagosRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Pago no encontrado con ID: {}", id);
                    return new ExcepcionPersonalizada(
                            "Pago no encontrado con ID: " + id,
                            HttpStatus.NOT_FOUND
                    );
                });
    }

    // Busca un pago por pedido
    public Pagos buscarPorPedido(Integer pedidoId) {
        return pagosRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> {
                    logger.warn("Pago no encontrado para pedido ID: {}", pedidoId);
                    return new ExcepcionPersonalizada(
                            "No existe pago para el pedido con ID: " + pedidoId,
                            HttpStatus.NOT_FOUND
                    );
                });
    }

    // Busca pagos por usuario
    public List<Pagos> buscarPorUsuario(Integer usuarioId) {
        List<Pagos> pagos = pagosRepository.findByUsuarioId(usuarioId);

        if (pagos.isEmpty()) {
            logger.warn("No existen pagos para usuario ID: {}", usuarioId);
            throw new ExcepcionPersonalizada(
                    "No existen pagos para el usuario con ID: " + usuarioId,
                    HttpStatus.NOT_FOUND
            );
        }

        return pagos;
    }

    // Busca pagos por estado
    public List<Pagos> buscarPorEstado(String estado) {
        String estadoNormalizado = validarEstado(estado);

        List<Pagos> pagos = pagosRepository.findByEstado(estadoNormalizado);

        if (pagos.isEmpty()) {
            logger.warn("No existen pagos con estado: {}", estadoNormalizado);
            throw new ExcepcionPersonalizada(
                    "No existen pagos con estado: " + estadoNormalizado,
                    HttpStatus.NOT_FOUND
            );
        }

        return pagos;
    }

    // Elimina un pago por ID
    public void eliminarPorId(Integer id) {
        if (!pagosRepository.existsById(id)) {
            logger.warn("Intento de eliminar pago inexistente con ID: {}", id);
            throw new ExcepcionPersonalizada(
                    "No se puede eliminar, el ID de pago no existe.",
                    HttpStatus.NOT_FOUND
            );
        }

        pagosRepository.deleteById(id);

        logger.info("Pago eliminado correctamente con ID: {}", id);
    }

    // Obtiene usuario desde autenticación
    @SuppressWarnings("unchecked")
    private Map<String, Object> obtenerUsuario(Integer usuarioId) {
        try {
            Map<String, Object> respuesta = restTemplate.getForObject(
                    URL_AUTH + "usuarios/" + usuarioId,
                    Map.class
            );

            if (respuesta == null) {
                throw new ExcepcionPersonalizada(
                        "Respuesta vacía del servicio de usuarios.",
                        HttpStatus.BAD_GATEWAY
                );
            }

            Object datosUsuario = respuesta.containsKey("usuario") ? respuesta.get("usuario") : respuesta;

            if (!(datosUsuario instanceof Map)) {
                throw new ExcepcionPersonalizada(
                        "La respuesta del usuario no tiene el formato esperado.",
                        HttpStatus.BAD_GATEWAY
                );
            }

            return (Map<String, Object>) datosUsuario;

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Usuario no encontrado en autenticación. ID: {}", usuarioId);
            throw new ExcepcionPersonalizada(
                    "El usuario con ID " + usuarioId + " no existe.",
                    HttpStatus.NOT_FOUND
            );
        } catch (ResourceAccessException e) {
            logger.error("MS Autenticación no responde: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "No se pudo validar el usuario porque autenticación no responde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con autenticación: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "Ocurrió un error al validar el usuario.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Envía notificación sin romper el pago si falla
    private void enviarNotificacionPago(Pagos pago, Map<String, Object> usuario) {
        try {
            String email = normalizarTexto(usuario.get("email"), null);

            if (email == null) {
                logger.warn("Usuario sin email, no se enviará notificación de pago.");
                return;
            }

            NotiDTO noti = new NotiDTO();
            noti.setPedidoId(pago.getPedidoId());
            noti.setTipo("PAGO");
            noti.setDestinatario(email);
            noti.setFecha(LocalDateTime.now().format(FORMATO_FECHA));
            noti.setUsuarioId(pago.getUsuarioId());

            restTemplate.postForObject(
                    URL_NOTIFICACIONES + "enviar",
                    noti,
                    Object.class
            );

            logger.info("Notificación de pago enviada para pedido ID: {}", pago.getPedidoId());

        } catch (Exception e) {
            logger.warn("No se pudo enviar la notificación de pago: {}", e.getMessage());
        }
    }

    // Convierte pago a DTO de respuesta
    private PagosResponseDTO mapearAResponse(Pagos pago, String nombreUsuario, String mensaje) {
        return new PagosResponseDTO(
                pago.getId(),
                pago.getMonto(),
                pago.getEstado(),
                pago.getPedidoId(),
                pago.getUsuarioId(),
                nombreUsuario,
                pago.getFechaPago(),
                mensaje
        );
    }

    // Valida estado de pago
    private String validarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ExcepcionPersonalizada("El estado es obligatorio.", HttpStatus.BAD_REQUEST);
        }

        String estadoNormalizado = estado.trim().toUpperCase();

        List<String> estadosPermitidos = List.of("APROBADO", "RECHAZADO", "PENDIENTE");

        if (!estadosPermitidos.contains(estadoNormalizado)) {
            throw new ExcepcionPersonalizada(
                    "Estado de pago no válido: " + estado,
                    HttpStatus.BAD_REQUEST
            );
        }

        return estadoNormalizado;
    }

    // Normaliza objetos a texto
    private String normalizarTexto(Object valor, String valorPorDefecto) {
        if (valor == null || valor.toString().isBlank()) {
            return valorPorDefecto;
        }

        return valor.toString().trim();
    }


}
