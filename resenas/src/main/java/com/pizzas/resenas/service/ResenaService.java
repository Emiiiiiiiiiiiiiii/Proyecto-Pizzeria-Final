package com.pizzas.resenas.service;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.pizzas.resenas.dto.NotiDTO;
import com.pizzas.resenas.dto.ResenaRequestDTO;
import com.pizzas.resenas.dto.ResenaResponseDTO;
import com.pizzas.resenas.dto.ResenaUpdateDTO;
import com.pizzas.resenas.exception.ExcepcionPersonalizada;
import com.pizzas.resenas.model.Resena;
import com.pizzas.resenas.repository.ResenaRepository;

// Service con la lógica del microservicio reseñas
@Service
public class ResenaService {

     private static final Logger logger = LoggerFactory.getLogger(ResenaService.class);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ResenaRepository repository;
    private final RestTemplate restTemplate;

    @Value("${url.pedidos}")
    private String URL_PEDIDOS;

    @Value("${url.auth}")
    private String URL_AUTH;

    @Value("${url.notificaciones}")
    private String URL_NOTIFICACIONES;

    // Constructor para inyectar dependencias
    public ResenaService(ResenaRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // Crea una reseña validando pedido y usuario
    @Transactional
    public ResenaResponseDTO crearResena(ResenaRequestDTO dto) {
        if (repository.existsByPedidoId(dto.getPedidoId())) {
            throw new ExcepcionPersonalizada(
                    "El pedido con ID " + dto.getPedidoId() + " ya tiene una reseña registrada.",
                    HttpStatus.CONFLICT
            );
        }

        Map<String, Object> pedido = obtenerPedido(dto.getPedidoId());

        validarPedidoParaResena(pedido, dto.getUsuarioId());

        String clienteNombre = normalizarTexto(pedido.get("nombreCliente"), null);
        String emailCliente = normalizarTexto(pedido.get("emailCliente"), null);

        if (clienteNombre == null || emailCliente == null) {
            Map<String, Object> usuario = obtenerUsuario(dto.getUsuarioId());

            if (clienteNombre == null) {
                clienteNombre = construirNombreUsuario(usuario);
            }

            if (emailCliente == null) {
                emailCliente = normalizarTexto(usuario.get("email"), "cliente@mail.com");
            }
        }

        String nombreProducto = normalizarTexto(
                pedido.get("detalleProductos"),
                "Pedido #" + dto.getPedidoId()
        );

        Resena resena = new Resena();
        resena.setPedidoId(dto.getPedidoId());
        resena.setUsuarioId(dto.getUsuarioId());
        resena.setClienteNombre(clienteNombre);
        resena.setNombreProducto(nombreProducto);
        resena.setComentario(dto.getComentario().trim());
        resena.setEstrellas(dto.getEstrellas());
        resena.setFechaResena(LocalDateTime.now());

        Resena guardada = repository.save(resena);

        enviarNotificacionResena(guardada, emailCliente);

        logger.info("Reseña creada correctamente para pedido ID: {}", guardada.getPedidoId());

        return mapearAResponse(guardada);
    }

    // Lista todas las reseñas
    public List<ResenaResponseDTO> listarTodas() {
        List<Resena> resenas = repository.findAll();
        List<ResenaResponseDTO> respuesta = new ArrayList<>();

        for (Resena resena : resenas) {
            respuesta.add(mapearAResponse(resena));
        }

        logger.info("Listado de reseñas obtenido. Total: {}", respuesta.size());

        return respuesta;
    }

    // Busca reseña por ID
    public ResenaResponseDTO buscarPorId(Integer id) {
        Resena resena = repository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada(
                        "Reseña no encontrada con ID: " + id,
                        HttpStatus.NOT_FOUND
                ));

        return mapearAResponse(resena);
    }

    // Busca reseñas por pedido, esta ruta la usa certificación
    public List<ResenaResponseDTO> buscarPorPedido(Integer pedidoId) {
        List<ResenaResponseDTO> respuesta = new ArrayList<>();

        repository.findByPedidoId(pedidoId)
                .ifPresent(resena -> respuesta.add(mapearAResponse(resena)));

        return respuesta;
    }

    // Busca reseñas por usuario
    public List<ResenaResponseDTO> buscarPorUsuario(Integer usuarioId) {
        List<Resena> resenas = repository.findByUsuarioId(usuarioId);

        if (resenas.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "No existen reseñas para el usuario con ID: " + usuarioId,
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAResponse(resenas);
    }

    // Busca reseñas por estrellas
    public List<ResenaResponseDTO> buscarPorEstrellas(Integer estrellas) {
        validarEstrellas(estrellas);

        List<Resena> resenas = repository.findByEstrellas(estrellas);

        if (resenas.isEmpty()) {
            throw new ExcepcionPersonalizada(
                    "No existen reseñas con " + estrellas + " estrellas.",
                    HttpStatus.NOT_FOUND
            );
        }

        return mapearListaAResponse(resenas);
    }

    // Actualiza comentario y estrellas de una reseña
    @Transactional
    public ResenaResponseDTO actualizar(Integer id, ResenaUpdateDTO dto) {
        Resena resena = repository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada(
                        "Reseña no encontrada con ID: " + id,
                        HttpStatus.NOT_FOUND
                ));

        resena.setComentario(dto.getComentario().trim());
        resena.setEstrellas(dto.getEstrellas());

        Resena actualizada = repository.save(resena);

        logger.info("Reseña actualizada correctamente con ID: {}", id);

        return mapearAResponse(actualizada);
    }

    // Elimina reseña por ID
    public void eliminar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ExcepcionPersonalizada(
                    "No existe la reseña para eliminar.",
                    HttpStatus.NOT_FOUND
            );
        }

        repository.deleteById(id);

        logger.info("Reseña eliminada correctamente con ID: {}", id);
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

    // Valida que el pedido pueda recibir reseña
    private void validarPedidoParaResena(Map<String, Object> pedido, Integer usuarioIdRequest) {
        Integer usuarioIdPedido = convertirAEntero(pedido.get("usuarioId"));

        if (usuarioIdPedido != null && !usuarioIdPedido.equals(usuarioIdRequest)) {
            throw new ExcepcionPersonalizada(
                    "El usuario enviado no coincide con el usuario del pedido.",
                    HttpStatus.BAD_REQUEST
            );
        }

        String estadoPedido = normalizarTexto(pedido.get("estado"), "");

        if ("CANCELADO".equalsIgnoreCase(estadoPedido)
                || "RECHAZADO".equalsIgnoreCase(estadoPedido)
                || "PENDIENTE".equalsIgnoreCase(estadoPedido)) {

            throw new ExcepcionPersonalizada(
                    "No se puede crear reseña para un pedido en estado: " + estadoPedido,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // Envía notificación sin romper la reseña si falla
    private void enviarNotificacionResena(Resena resena, String emailCliente) {
        try {
            NotiDTO noti = new NotiDTO();
            noti.setUsuarioId(resena.getUsuarioId());
            noti.setPedidoId(resena.getPedidoId());
            noti.setTipo("RESENA");
            noti.setDestinatario(emailCliente);
            noti.setMensaje("Tu reseña del pedido #" + resena.getPedidoId() + " fue registrada correctamente.");
            noti.setFecha(LocalDateTime.now().format(FORMATO_FECHA));

            restTemplate.postForObject(URL_NOTIFICACIONES + "enviar", noti, Object.class);

            logger.info("Notificación de reseña enviada para pedido ID: {}", resena.getPedidoId());

        } catch (Exception e) {
            logger.warn("No se pudo enviar notificación de reseña: {}", e.getMessage());
        }
    }

    // Convierte entidad a DTO de respuesta
    private ResenaResponseDTO mapearAResponse(Resena resena) {
        return new ResenaResponseDTO(
                resena.getId(),
                resena.getPedidoId(),
                resena.getUsuarioId(),
                resena.getClienteNombre(),
                resena.getNombreProducto(),
                resena.getComentario(),
                resena.getEstrellas(),
                resena.getFechaResena()
        );
    }

    // Convierte lista de entidades a DTOs
    private List<ResenaResponseDTO> mapearListaAResponse(List<Resena> resenas) {
        List<ResenaResponseDTO> respuesta = new ArrayList<>();

        for (Resena resena : resenas) {
            respuesta.add(mapearAResponse(resena));
        }

        return respuesta;
    }

    // Construye nombre desde usuario
    private String construirNombreUsuario(Map<String, Object> usuario) {
        String nombre = normalizarTexto(usuario.get("nombre"), "");
        String apellido = normalizarTexto(usuario.get("apellido"), "");

        String nombreCompleto = (nombre + " " + apellido).trim();

        if (nombreCompleto.isBlank()) {
            return "Cliente sin nombre";
        }

        return nombreCompleto;
    }

    // Valida estrellas en path
    private void validarEstrellas(Integer estrellas) {
        if (estrellas == null || estrellas < 1 || estrellas > 5) {
            throw new ExcepcionPersonalizada(
                    "Las estrellas deben estar entre 1 y 5.",
                    HttpStatus.BAD_REQUEST
            );
        }
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