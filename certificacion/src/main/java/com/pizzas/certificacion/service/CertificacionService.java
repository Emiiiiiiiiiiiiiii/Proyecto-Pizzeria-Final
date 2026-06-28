package com.pizzas.certificacion.service;

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

import com.pizzas.certificacion.dto.CertificacionRequestDTO;
import com.pizzas.certificacion.dto.CertificacionResponseDTO;
import com.pizzas.certificacion.dto.PedidoDTO;
import com.pizzas.certificacion.dto.RepartoDTO;
import com.pizzas.certificacion.dto.ResenaDTO;
import com.pizzas.certificacion.exception.ExcepcionPersonalizada;
import com.pizzas.certificacion.model.Certificacion;
import com.pizzas.certificacion.repository.CertificacionRepository;

@Service
public class CertificacionService {

    // Logger para registrar eventos importantes de certificación
    private static final Logger logger = LoggerFactory.getLogger(CertificacionService.class);

    // Formato usado para comparar y guardar fechas
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Repository de certificación
    private final CertificacionRepository repository;

    // Cliente para comunicarse con otros microservicios
    private final RestTemplate restTemplate;

    @Value("${url.pedidos}")
    private String URL_PEDIDOS;

    @Value("${url.reparto}")
    private String URL_REPARTO;

    @Value("${url.resenas}")
    private String URL_RESENAS;

    // Constructor para inyectar dependencias
    public CertificacionService(CertificacionRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // Genera una certificación usando datos de pedido, reparto y reseñas
    public CertificacionResponseDTO generarCertificacion(CertificacionRequestDTO request) {

        Integer pedidoId = request.getPedidoId();

        if (repository.existsByPedidoId(pedidoId)) {
            logger.warn("Intento de duplicar certificación para pedido ID: {}", pedidoId);
            throw new ExcepcionPersonalizada(
                    "Ya existe una certificación para el pedido con ID: " + pedidoId,
                    HttpStatus.CONFLICT
            );
        }

        PedidoDTO pedido = obtenerPedido(pedidoId);
        validarPedidoCertificable(pedido);

        RepartoDTO reparto = obtenerRepartoPorPedido(pedidoId);
        List<ResenaDTO> resenas = obtenerResenasPorPedido(pedidoId);

        String nombreRepartidor = obtenerNombreRepartidor(reparto);
        String horaEntrega = obtenerHoraEntrega(reparto);
        String estadoPuntualidad = calcularEstadoPuntualidad(pedido.getFechaPedido(), horaEntrega);

        Boolean tieneResena = !resenas.isEmpty();
        String comentarioResena = construirComentarioResena(resenas);
        Integer estrellasResena = calcularEstrellasResena(resenas);

        Certificacion certificacion = new Certificacion();
        certificacion.setPedidoId(pedido.getId());
        certificacion.setUsuarioId(pedido.getUsuarioId());
        certificacion.setNombreUsuario(normalizarTexto(pedido.getNombreCliente(), "Cliente sin nombre"));
        certificacion.setEmailUsuario(normalizarTexto(pedido.getEmailCliente(), "sin-email@correo.cl"));
        certificacion.setFechaPedido(normalizarTexto(pedido.getFechaPedido(), LocalDateTime.now().format(FORMATO_FECHA)));
        certificacion.setDetalleProductos(normalizarTexto(pedido.getDetalleProductos(), "Sin detalle de productos"));
        certificacion.setCantidadTotalItems(pedido.getCantidadTotalItems());
        certificacion.setMontoTotal(pedido.getMontoTotal());
        certificacion.setMetodoPago(normalizarTexto(pedido.getMetodoPago(), "EFECTIVO"));
        certificacion.setEstadoPedido(normalizarTexto(pedido.getEstado(), "PENDIENTE"));
        certificacion.setNombreRepartidor(nombreRepartidor);
        certificacion.setHoraEntrega(horaEntrega);
        certificacion.setEstadoPuntualidad(estadoPuntualidad);
        certificacion.setTieneResena(tieneResena);
        certificacion.setComentarioResena(comentarioResena);
        certificacion.setEstrellasResena(estrellasResena);
        certificacion.setFechaEmision(LocalDateTime.now().format(FORMATO_FECHA));

        Certificacion guardada = repository.save(certificacion);

        logger.info("Certificación generada correctamente para pedido ID: {}", pedidoId);

        return mapearAResponse(guardada, "Certificación generada correctamente");
    }

    // Lista todas las certificaciones
    public List<CertificacionResponseDTO> listarTodas() {
        List<Certificacion> certificaciones = repository.findAll();
        List<CertificacionResponseDTO> respuestas = new ArrayList<>();

        for (Certificacion certificacion : certificaciones) {
            respuestas.add(mapearAResponse(certificacion, "Certificación encontrada"));
        }

        logger.info("Listado de certificaciones obtenido. Total: {}", respuestas.size());

        return respuestas;
    }

    // Busca una certificación por ID
    public CertificacionResponseDTO buscarPorId(Integer id) {
        Certificacion certificacion = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Certificación no encontrada con ID: {}", id);
                    return new ExcepcionPersonalizada(
                            "Certificación no encontrada con ID: " + id,
                            HttpStatus.NOT_FOUND
                    );
                });

        return mapearAResponse(certificacion, "Certificación encontrada");
    }

    // Busca una certificación por ID de pedido
    public CertificacionResponseDTO buscarPorPedido(Integer pedidoId) {
        Certificacion certificacion = repository.findByPedidoId(pedidoId)
                .orElseThrow(() -> {
                    logger.warn("Certificación no encontrada para pedido ID: {}", pedidoId);
                    return new ExcepcionPersonalizada(
                            "No existe certificación para el pedido con ID: " + pedidoId,
                            HttpStatus.NOT_FOUND
                    );
                });

        return mapearAResponse(certificacion, "Certificación encontrada");
    }

    // Busca certificaciones por usuario
    public List<CertificacionResponseDTO> buscarPorUsuario(Integer usuarioId) {
        List<Certificacion> certificaciones = repository.findByUsuarioId(usuarioId);

        if (certificaciones.isEmpty()) {
            logger.warn("No existen certificaciones para usuario ID: {}", usuarioId);
            throw new ExcepcionPersonalizada(
                    "No existen certificaciones para el usuario con ID: " + usuarioId,
                    HttpStatus.NOT_FOUND
            );
        }

        List<CertificacionResponseDTO> respuestas = new ArrayList<>();

        for (Certificacion certificacion : certificaciones) {
            respuestas.add(mapearAResponse(certificacion, "Certificación encontrada"));
        }

        return respuestas;
    }

    // Busca certificaciones por estado del pedido
    public List<CertificacionResponseDTO> buscarPorEstadoPedido(String estadoPedido) {
        String estadoNormalizado = estadoPedido.trim().toUpperCase();

        List<Certificacion> certificaciones = repository.findByEstadoPedido(estadoNormalizado);

        if (certificaciones.isEmpty()) {
            logger.warn("No existen certificaciones con estado de pedido: {}", estadoNormalizado);
            throw new ExcepcionPersonalizada(
                    "No existen certificaciones con estado de pedido: " + estadoNormalizado,
                    HttpStatus.NOT_FOUND
            );
        }

        List<CertificacionResponseDTO> respuestas = new ArrayList<>();

        for (Certificacion certificacion : certificaciones) {
            respuestas.add(mapearAResponse(certificacion, "Certificación encontrada"));
        }

        return respuestas;
    }

    // Busca certificaciones con o sin reseña
    public List<CertificacionResponseDTO> buscarPorTieneResena(Boolean tieneResena) {
        List<Certificacion> certificaciones = repository.findByTieneResena(tieneResena);

        if (certificaciones.isEmpty()) {
            logger.warn("No existen certificaciones con tieneResena: {}", tieneResena);
            throw new ExcepcionPersonalizada(
                    "No existen certificaciones con ese filtro de reseña.",
                    HttpStatus.NOT_FOUND
            );
        }

        List<CertificacionResponseDTO> respuestas = new ArrayList<>();

        for (Certificacion certificacion : certificaciones) {
            respuestas.add(mapearAResponse(certificacion, "Certificación encontrada"));
        }

        return respuestas;
    }

    // Elimina una certificación por ID
    public void eliminar(Integer id) {
        if (!repository.existsById(id)) {
            logger.warn("Intento de eliminar certificación inexistente con ID: {}", id);
            throw new ExcepcionPersonalizada(
                    "No existe la certificación para eliminar.",
                    HttpStatus.NOT_FOUND
            );
        }

        repository.deleteById(id);

        logger.info("Certificación eliminada correctamente con ID: {}", id);
    }

    // Convierte la entidad Certificacion a DTO de respuesta
    private CertificacionResponseDTO mapearAResponse(Certificacion certificacion, String mensaje) {
        return new CertificacionResponseDTO(
                certificacion.getId(),
                certificacion.getPedidoId(),
                certificacion.getUsuarioId(),
                certificacion.getNombreUsuario(),
                certificacion.getEmailUsuario(),
                certificacion.getFechaPedido(),
                certificacion.getDetalleProductos(),
                certificacion.getCantidadTotalItems(),
                certificacion.getMontoTotal(),
                certificacion.getMetodoPago(),
                certificacion.getEstadoPedido(),
                certificacion.getNombreRepartidor(),
                certificacion.getHoraEntrega(),
                certificacion.getEstadoPuntualidad(),
                certificacion.getTieneResena(),
                certificacion.getComentarioResena(),
                certificacion.getEstrellasResena(),
                certificacion.getFechaEmision(),
                mensaje
        );
    }

    // Obtiene el pedido desde el microservicio pedido
    @SuppressWarnings("unchecked")
    private PedidoDTO obtenerPedido(Integer pedidoId) {
        try {
            Map<String, Object> respuesta = restTemplate.getForObject(URL_PEDIDOS + pedidoId, Map.class);

            if (respuesta == null) {
                throw new ExcepcionPersonalizada("No se pudo obtener información del pedido.", HttpStatus.BAD_GATEWAY);
            }

            Object datosPedido = respuesta.containsKey("pedido") ? respuesta.get("pedido") : respuesta;

            if (!(datosPedido instanceof Map)) {
                throw new ExcepcionPersonalizada(
                        "La respuesta del microservicio pedido no tiene el formato esperado.",
                        HttpStatus.BAD_GATEWAY
                );
            }

            Map<String, Object> pedidoMap = (Map<String, Object>) datosPedido;

            PedidoDTO pedido = new PedidoDTO();
            pedido.setId(convertirAEntero(pedidoMap.get("id")));
            pedido.setUsuarioId(convertirAEntero(pedidoMap.get("usuarioId")));
            pedido.setNombreCliente(convertirATexto(pedidoMap.get("nombreCliente")));
            pedido.setEmailCliente(convertirATexto(pedidoMap.get("emailCliente")));
            pedido.setMontoTotal(convertirAEntero(pedidoMap.get("montoTotal")));
            pedido.setCantidadTotalItems(convertirAEntero(pedidoMap.get("cantidadTotalItems")));
            pedido.setDetalleProductos(convertirATexto(pedidoMap.get("detalleProductos")));
            pedido.setEstado(convertirATexto(pedidoMap.get("estado")));
            pedido.setFechaPedido(convertirATexto(pedidoMap.get("fechaPedido")));
            pedido.setMetodoPago(convertirATexto(pedidoMap.get("metodoPago")));

            return pedido;

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Pedido no encontrado en MS pedido. ID: {}", pedidoId);
            throw new ExcepcionPersonalizada(
                    "El pedido con ID " + pedidoId + " no existe.",
                    HttpStatus.NOT_FOUND
            );
        } catch (ResourceAccessException e) {
            logger.error("MS Pedido no responde: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "No se pudo generar la certificación porque pedido no responde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con MS Pedido: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "Ocurrió un error al obtener el pedido.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Obtiene la información de reparto filtrando por pedido
    private RepartoDTO obtenerRepartoPorPedido(Integer pedidoId) {
        try {
            RepartoDTO[] repartos = restTemplate.getForObject(URL_REPARTO + "todos", RepartoDTO[].class);

            if (repartos == null) {
                logger.warn("MS Reparto respondió sin datos.");
                return null;
            }

            for (RepartoDTO reparto : repartos) {
                if (reparto.getPedidoId() != null && reparto.getPedidoId().equals(pedidoId)) {
                    return reparto;
                }
            }

            logger.warn("No se encontró reparto para pedido ID: {}", pedidoId);
            return null;

        } catch (ResourceAccessException e) {
            logger.warn("MS Reparto no responde: {}", e.getMessage());
            return null;
        } catch (RestClientException e) {
            logger.warn("Error al comunicarse con MS Reparto: {}", e.getMessage());
            return null;
        }
    }

    // Obtiene reseñas del pedido desde el microservicio reseñas
    private List<ResenaDTO> obtenerResenasPorPedido(Integer pedidoId) {
        try {
            ResenaDTO[] resenas = restTemplate.getForObject(URL_RESENAS + "pedido/" + pedidoId, ResenaDTO[].class);

            List<ResenaDTO> lista = new ArrayList<>();

            if (resenas != null) {
                for (ResenaDTO resena : resenas) {
                    lista.add(resena);
                }
            }

            return lista;

        } catch (HttpClientErrorException e) {
            logger.info("No hay reseñas registradas para pedido ID: {}", pedidoId);
            return new ArrayList<>();
        } catch (ResourceAccessException e) {
            logger.warn("MS Reseñas no responde: {}", e.getMessage());
            return new ArrayList<>();
        } catch (RestClientException e) {
            logger.warn("Error al comunicarse con MS Reseñas: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // Evita certificar pedidos rechazados o cancelados
    private void validarPedidoCertificable(PedidoDTO pedido) {
        if (pedido.getId() == null) {
            throw new ExcepcionPersonalizada("El pedido recibido no tiene ID válido.", HttpStatus.BAD_GATEWAY);
        }

        String estado = pedido.getEstado() != null ? pedido.getEstado().toUpperCase() : "";

        if ("RECHAZADO".equals(estado) || "CANCELADO".equals(estado)) {
            throw new ExcepcionPersonalizada(
                    "No se puede certificar un pedido en estado: " + estado,
                    HttpStatus.BAD_REQUEST
            );
        }

        if (pedido.getCantidadTotalItems() == null || pedido.getCantidadTotalItems() < 1) {
            throw new ExcepcionPersonalizada(
                    "El pedido no tiene una cantidad de productos válida.",
                    HttpStatus.BAD_GATEWAY
            );
        }

        if (pedido.getMontoTotal() == null || pedido.getMontoTotal() < 0) {
            throw new ExcepcionPersonalizada(
                    "El pedido no tiene un monto válido.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Obtiene nombre del repartidor o deja un valor por defecto
    private String obtenerNombreRepartidor(RepartoDTO reparto) {
        if (reparto == null) {
            return "SIN ASIGNAR";
        }

        if (reparto.getNombreRepartidor() != null && !reparto.getNombreRepartidor().isBlank()) {
            return reparto.getNombreRepartidor();
        }

        if (reparto.getRepartidor() != null && !reparto.getRepartidor().isBlank()) {
            return reparto.getRepartidor();
        }

        return "SIN ASIGNAR";
    }

    // Obtiene hora de entrega o deja un valor por defecto
    private String obtenerHoraEntrega(RepartoDTO reparto) {
        if (reparto == null) {
            return "SIN INFORMACION";
        }

        if (reparto.getFechaEntrega() != null && !reparto.getFechaEntrega().isBlank()) {
            return reparto.getFechaEntrega();
        }

        if (reparto.getHoraEntrega() != null && !reparto.getHoraEntrega().isBlank()) {
            return reparto.getHoraEntrega();
        }

        return "SIN INFORMACION";
    }

    // Calcula si el pedido fue entregado a tiempo o con retraso
    private String calcularEstadoPuntualidad(String fechaPedido, String horaEntrega) {
        try {
            if (horaEntrega == null || horaEntrega.isBlank() || "SIN INFORMACION".equals(horaEntrega)) {
                return "SIN INFORMACION";
            }

            LocalDateTime fechaPedidoConvertida = LocalDateTime.parse(fechaPedido, FORMATO_FECHA);
            LocalDateTime horaEntregaConvertida = LocalDateTime.parse(horaEntrega, FORMATO_FECHA);

            if (!horaEntregaConvertida.isAfter(fechaPedidoConvertida.plusMinutes(45))) {
                return "A TIEMPO";
            }

            return "CON RETRASO";

        } catch (Exception e) {
            logger.warn("No se pudo calcular puntualidad: {}", e.getMessage());
            return "SIN INFORMACION";
        }
    }

    // Construye texto de reseñas encontradas
    private String construirComentarioResena(List<ResenaDTO> resenas) {
        if (resenas.isEmpty()) {
            return null;
        }

        List<String> comentarios = new ArrayList<>();

        for (ResenaDTO resena : resenas) {
            String producto = normalizarTexto(resena.getNombreProducto(), "Producto");
            String comentario = normalizarTexto(resena.getComentario(), "Sin comentario");

            comentarios.add(producto + ": " + comentario);
        }

        return String.join(" | ", comentarios);
    }

    // Calcula promedio de estrellas de las reseñas
    private Integer calcularEstrellasResena(List<ResenaDTO> resenas) {
        if (resenas.isEmpty()) {
            return 0;
        }

        int suma = 0;
        int cantidad = 0;

        for (ResenaDTO resena : resenas) {
            if (resena.getEstrellas() != null) {
                suma += resena.getEstrellas();
                cantidad++;
            }
        }

        if (cantidad == 0) {
            return 0;
        }

        return Math.round((float) suma / cantidad);
    }

    // Normaliza texto para evitar campos vacíos
    private String normalizarTexto(String valor, String valorPorDefecto) {
        if (valor == null || valor.isBlank()) {
            return valorPorDefecto;
        }

        return valor.trim();
    }

    // Convierte objetos numéricos a Integer
    private Integer convertirAEntero(Object valor) {
        if (valor instanceof Integer) {
            return (Integer) valor;
        }

        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }

        return null;
    }

    // Convierte objetos a texto evitando null
    private String convertirATexto(Object valor) {
        return valor != null ? valor.toString() : null;
    }

}
