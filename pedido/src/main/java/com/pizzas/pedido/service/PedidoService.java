package com.pizzas.pedido.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.pizzas.pedido.dto.CarritoDetalleDTO;
import com.pizzas.pedido.dto.InventarioRequestDTO;
import com.pizzas.pedido.dto.NotiDTO;
import com.pizzas.pedido.dto.PagosDTO;
import com.pizzas.pedido.dto.PedidoRequestDTO;
import com.pizzas.pedido.dto.PedidoResponseDTO;
import com.pizzas.pedido.dto.UsuarioDTO;
import com.pizzas.pedido.exception.ExcepcionPersonalizada;
import com.pizzas.pedido.model.Pedido;
import com.pizzas.pedido.repository.PedidoRepository;

@Service

public class PedidoService {
    // Logger para registrar eventos importantes del pedido
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    // Formato usado para guardar la fecha del pedido
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Repository del pedido
    private final PedidoRepository repository;

    // Cliente para comunicarse con otros microservicios
    private final RestTemplate restTemplate;

    @Value("${url.auth}")
    private String URL_AUTH;

    @Value("${url.carrito}")
    private String URL_CARRITO;

    @Value("${url.inventario}")
    private String URL_INVENTARIO;

    @Value("${url.pagos}")
    private String URL_PAGOS;

    @Value("${url.notificaciones}")
    private String URL_NOTIFICACIONES;

    // Constructor para inyectar dependencias
    public PedidoService(PedidoRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // Crea un pedido usando datos de autenticación, carrito, pagos, inventario y notificaciones
    public PedidoResponseDTO crearPedido(PedidoRequestDTO request) {

        UsuarioDTO usuario = obtenerUsuario(request.getUsuarioId());
        List<CarritoDetalleDTO> itemsCarrito = obtenerCarrito(request.getUsuarioId());

        int total = calcularMontoTotal(itemsCarrito);
        int cantidadTotal = calcularCantidadTotal(itemsCarrito);

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(request.getUsuarioId());
        pedido.setNombreCliente(construirNombreCliente(usuario));
        pedido.setEmailCliente(usuario.getEmail());
        pedido.setMontoTotal(total);
        pedido.setCantidadTotalItems(cantidadTotal);
        pedido.setEstado("PENDIENTE");
        pedido.setMetodoPago(request.getMetodoPago().trim().toUpperCase());
        pedido.setFechaPedido(LocalDateTime.now().format(FORMATO_FECHA));

        Pedido guardado = repository.save(pedido);

        logger.info("Pedido creado en estado PENDIENTE con ID: {}", guardado.getId());

        try {
            procesarPago(guardado);
            descontarInventario(itemsCarrito);

            guardado.setEstado("PAGADO");
            guardado = repository.save(guardado);

            enviarNotificacionPago(guardado);
            vaciarCarrito(guardado.getUsuarioId());

            logger.info("Pedido completado correctamente con ID: {}", guardado.getId());

            return new PedidoResponseDTO(
                    guardado.getId(),
                    guardado.getNombreCliente(),
                    guardado.getMontoTotal(),
                    guardado.getEstado(),
                    guardado.getFechaPedido(),
                    "Pedido completado correctamente"
            );

        } catch (ExcepcionPersonalizada e) {
            guardado.setEstado("RECHAZADO");
            repository.save(guardado);

            logger.warn("Pedido rechazado con ID {}: {}", guardado.getId(), e.getMessage());
            throw e;
        }
    }

    // Lista todos los pedidos
    public List<Pedido> listarPedidos() {
        List<Pedido> pedidos = repository.findAll();

        logger.info("Listado de pedidos obtenido. Total: {}", pedidos.size());

        return pedidos;
    }

    // Busca un pedido por ID
    public Pedido buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Pedido no encontrado con ID: {}", id);
                    return new ExcepcionPersonalizada("Pedido no encontrado con ID: " + id, HttpStatus.NOT_FOUND);
                });
    }

    // Busca pedidos por usuario
    public List<Pedido> buscarPorUsuario(Integer usuarioId) {

        List<Pedido> pedidos = repository.findByUsuarioId(usuarioId);

        if (pedidos.isEmpty()) {
            logger.warn("No existen pedidos para el usuario ID: {}", usuarioId);
            throw new ExcepcionPersonalizada(
                    "No existen pedidos para el usuario con ID: " + usuarioId,
                    HttpStatus.NOT_FOUND
            );
        }

        return pedidos;
    }

    // Busca pedidos por estado
    public List<Pedido> buscarPorEstado(String estado) {

        String estadoNormalizado = validarYNormalizarEstado(estado);

        List<Pedido> pedidos = repository.findByEstado(estadoNormalizado);

        if (pedidos.isEmpty()) {
            logger.warn("No existen pedidos con estado: {}", estadoNormalizado);
            throw new ExcepcionPersonalizada(
                    "No existen pedidos con estado: " + estadoNormalizado,
                    HttpStatus.NOT_FOUND
            );
        }

        return pedidos;
    }

    // Actualiza el estado de un pedido
    public Pedido actualizarPedido(Integer id, String nuevoEstado) {

        String estadoNormalizado = validarYNormalizarEstado(nuevoEstado);

        Pedido pedido = buscarPorId(id);
        pedido.setEstado(estadoNormalizado);

        Pedido actualizado = repository.save(pedido);

        logger.info("Estado del pedido {} actualizado a {}", id, estadoNormalizado);

        return actualizado;
    }

    // Elimina un pedido si existe
    public void eliminarPedido(Integer id) {

        if (!repository.existsById(id)) {
            logger.warn("Intento de eliminar pedido inexistente con ID: {}", id);
            throw new ExcepcionPersonalizada("No existe el pedido para eliminar.", HttpStatus.NOT_FOUND);
        }

        repository.deleteById(id);

        logger.info("Pedido eliminado correctamente con ID: {}", id);
    }

    // Obtiene el usuario desde el microservicio autenticación
    @SuppressWarnings("unchecked")
    private UsuarioDTO obtenerUsuario(Integer usuarioId) {
        try {
            Map<String, Object> respuesta = restTemplate.getForObject(URL_AUTH + "usuarios/" + usuarioId, Map.class);

            if (respuesta == null) {
                throw new ExcepcionPersonalizada("No se pudo obtener información del usuario.", HttpStatus.BAD_GATEWAY);
            }

            Object datosUsuario = respuesta.containsKey("usuario") ? respuesta.get("usuario") : respuesta;

            if (!(datosUsuario instanceof Map)) {
                throw new ExcepcionPersonalizada(
                        "La respuesta del microservicio autenticación no tiene el formato esperado.",
                        HttpStatus.BAD_GATEWAY
                );
            }

            Map<String, Object> usuarioMap = (Map<String, Object>) datosUsuario;

            UsuarioDTO usuario = new UsuarioDTO();
            usuario.setId(convertirAEntero(usuarioMap.get("id")));
            usuario.setNombre(convertirATexto(usuarioMap.get("nombre")));
            usuario.setApellido(convertirATexto(usuarioMap.get("apellido")));
            usuario.setEmail(convertirATexto(usuarioMap.get("email")));
            usuario.setRol(convertirATexto(usuarioMap.get("rol")));

            return usuario;

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Usuario no encontrado en autenticación. ID: {}", usuarioId);
            throw new ExcepcionPersonalizada("El usuario con ID " + usuarioId + " no existe.", HttpStatus.NOT_FOUND);
        } catch (ResourceAccessException e) {
            logger.error("MS Autenticación no responde: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "No se pudo validar el usuario porque autenticación no responde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con MS Autenticación: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "Ocurrió un error al validar el usuario en autenticación.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Obtiene el carrito del usuario desde el microservicio carrito
    private List<CarritoDetalleDTO> obtenerCarrito(Integer usuarioId) {
        try {
            ResponseEntity<List<CarritoDetalleDTO>> responseCarrito = restTemplate.exchange(
                    URL_CARRITO + "usuario/" + usuarioId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CarritoDetalleDTO>>() {}
            );

            List<CarritoDetalleDTO> items = responseCarrito.getBody();

            if (items == null || items.isEmpty()) {
                throw new ExcepcionPersonalizada("El carrito del usuario está vacío.", HttpStatus.BAD_REQUEST);
            }

            return items;

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Carrito no encontrado para usuario ID: {}", usuarioId);
            throw new ExcepcionPersonalizada("No existen productos en el carrito del usuario.", HttpStatus.NOT_FOUND);
        } catch (ResourceAccessException e) {
            logger.error("MS Carrito no responde: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "No se pudo obtener el carrito porque el microservicio carrito no responde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con MS Carrito: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "Ocurrió un error al obtener el carrito del usuario.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Procesa el pago usando el microservicio pagos
    @SuppressWarnings("unchecked")
    private void procesarPago(Pedido pedido) {
        try {
            PagosDTO pagosDTO = new PagosDTO();
            pagosDTO.setMonto(pedido.getMontoTotal());
            pagosDTO.setPedidoId(pedido.getId());
            pagosDTO.setUsuarioId(pedido.getUsuarioId());

            Map<String, Object> respuestaPago = restTemplate.postForObject(
                    URL_PAGOS + "procesar",
                    pagosDTO,
                    Map.class
            );

            if (respuestaPago == null || !"APROBADO".equals(respuestaPago.get("status"))) {
                throw new ExcepcionPersonalizada("El pago fue rechazado.", HttpStatus.BAD_REQUEST);
            }

            logger.info("Pago aprobado para pedido ID: {}", pedido.getId());

        } catch (ResourceAccessException e) {
            logger.error("MS Pagos no responde: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "No se pudo procesar el pago porque el microservicio pagos no responde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con MS Pagos: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "Ocurrió un error al procesar el pago.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Descuenta stock en el microservicio inventario
    private void descontarInventario(List<CarritoDetalleDTO> itemsCarrito) {
        try {
            List<InventarioRequestDTO> listaParaInventario = new ArrayList<>();

            for (CarritoDetalleDTO item : itemsCarrito) {
                InventarioRequestDTO dto = new InventarioRequestDTO();
                dto.setCatalogoId(item.getCatalogoId());
                dto.setCantidad(item.getCantidad());
                listaParaInventario.add(dto);
            }

            restTemplate.exchange(
                    URL_INVENTARIO + "descontar",
                    HttpMethod.POST,
                    new HttpEntity<>(listaParaInventario),
                    String.class
            );

            logger.info("Inventario descontado correctamente para {} items", listaParaInventario.size());

        } catch (ResourceAccessException e) {
            logger.error("MS Inventario no responde: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "No se pudo descontar inventario porque el microservicio inventario no responde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con MS Inventario: {}", e.getMessage());
            throw new ExcepcionPersonalizada(
                    "Ocurrió un error al descontar inventario.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Envía notificación de pago sin romper el pedido si falla
    private void enviarNotificacionPago(Pedido pedido) {
        try {
            NotiDTO notiDTO = new NotiDTO();
            notiDTO.setPedidoId(pedido.getId());
            notiDTO.setUsuarioId(pedido.getUsuarioId());
            notiDTO.setTipo("PAGO");
            notiDTO.setDestinatario(pedido.getEmailCliente());
            notiDTO.setFecha(pedido.getFechaPedido());

            restTemplate.postForObject(URL_NOTIFICACIONES + "enviar", notiDTO, Object.class);

            logger.info("Notificación enviada para pedido ID: {}", pedido.getId());

        } catch (Exception e) {
            logger.warn("No se pudo enviar notificación del pedido {}: {}", pedido.getId(), e.getMessage());
        }
    }

    // Vacía el carrito después de crear el pedido correctamente
    private void vaciarCarrito(Integer usuarioId) {
        try {
            restTemplate.exchange(
                    URL_CARRITO + "usuario/" + usuarioId + "/vaciar",
                    HttpMethod.DELETE,
                    null,
                    String.class
            );

            logger.info("Carrito vaciado correctamente para usuario ID: {}", usuarioId);

        } catch (Exception e) {
            logger.warn("No se pudo vaciar el carrito del usuario {}: {}", usuarioId, e.getMessage());
        }
    }

    // Calcula el monto total del carrito
    private int calcularMontoTotal(List<CarritoDetalleDTO> items) {
        int total = 0;

        for (CarritoDetalleDTO item : items) {
            if (item.getPrecioTotal() == null || item.getPrecioTotal() < 0) {
                throw new ExcepcionPersonalizada("El carrito contiene un precio inválido.", HttpStatus.BAD_REQUEST);
            }

            total += item.getPrecioTotal();
        }

        return total;
    }

    // Calcula la cantidad total de productos del carrito
    private int calcularCantidadTotal(List<CarritoDetalleDTO> items) {
        int cantidadTotal = 0;

        for (CarritoDetalleDTO item : items) {
            if (item.getCantidad() == null || item.getCantidad() < 1) {
                throw new ExcepcionPersonalizada("El carrito contiene una cantidad inválida.", HttpStatus.BAD_REQUEST);
            }

            cantidadTotal += item.getCantidad();
        }

        return cantidadTotal;
    }

    // Valida y normaliza estados permitidos
    private String validarYNormalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ExcepcionPersonalizada("El estado del pedido es obligatorio.", HttpStatus.BAD_REQUEST);
        }

        String estadoNormalizado = estado.trim().toUpperCase();

        List<String> estadosPermitidos = List.of(
                "PENDIENTE",
                "PAGADO",
                "EN_PREPARACION",
                "EN_REPARTO",
                "ENTREGADO",
                "CANCELADO",
                "RECHAZADO"
        );

        if (!estadosPermitidos.contains(estadoNormalizado)) {
            throw new ExcepcionPersonalizada("Estado de pedido no válido: " + estado, HttpStatus.BAD_REQUEST);
        }

        return estadoNormalizado;
    }

    // Construye el nombre completo del cliente
    private String construirNombreCliente(UsuarioDTO usuario) {
        String nombre = usuario.getNombre() != null ? usuario.getNombre() : "";
        String apellido = usuario.getApellido() != null ? usuario.getApellido() : "";

        return (nombre + " " + apellido).trim();
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
