package com.pizzas.pedido.service;

import com.pizzas.pedido.dto.PagosDTO;
import com.pizzas.pedido.dto.PedidoRequestDTO;
import com.pizzas.pedido.dto.PedidoResponseDTO;
import com.pizzas.pedido.dto.CarritoDetalleDTO;
import com.pizzas.pedido.dto.InventarioRequestDTO;
import com.pizzas.pedido.dto.NotiDTO;
import com.pizzas.pedido.exception.ExcepcionPersonalizada;
import com.pizzas.pedido.model.Pedido;
import com.pizzas.pedido.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service

public class PedidoService {
    @Autowired
    private PedidoRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url.auth}") private String URL_AUTH;
    @Value("${url.carrito}") private String URL_CARRITO;
    @Value("${url.inventario}") private String URL_INVENTARIO;
    @Value("${url.pagos}") private String URL_PAGOS;
    @Value("${url.notificaciones}") private String URL_NOTIFICACIONES;

    @SuppressWarnings("unchecked")
    public PedidoResponseDTO crearPedido(PedidoRequestDTO request) {
    
    Map<String, Object> usuario = restTemplate.getForObject(URL_AUTH + "usuarios/" + request.getUsuarioId(), Map.class);
    if (usuario == null) throw new ExcepcionPersonalizada("Usuario no encontrado");

    
    ResponseEntity<List<CarritoDetalleDTO>> responseCarrito = restTemplate.exchange(
        URL_CARRITO + "usuario/" + request.getUsuarioId(),
        HttpMethod.GET, null, new ParameterizedTypeReference<List<CarritoDetalleDTO>>() {}
    );
    List<CarritoDetalleDTO> items = responseCarrito.getBody();
    if (items == null || items.isEmpty()) throw new ExcepcionPersonalizada("El carrito está vacío");

    
    List<InventarioRequestDTO> listaParaInventario = new java.util.ArrayList<>();
    int total = 0;
    int cantidadTotal = 0;

    
    for (CarritoDetalleDTO item : items) {
        InventarioRequestDTO dto = new InventarioRequestDTO();
        dto.setCatalogoId(item.getCatalogoId());
        dto.setCantidad(item.getCantidad());
        listaParaInventario.add(dto);

        total += item.getPrecioTotal();
        cantidadTotal += item.getCantidad();
    }

    restTemplate.exchange(URL_INVENTARIO + "descontar", HttpMethod.POST, new HttpEntity<>(listaParaInventario), Void.class);

    
    Pedido pedido = new Pedido();
    pedido.setUsuarioId(request.getUsuarioId());
    pedido.setNombreCliente(usuario.get("nombre") + " " + usuario.get("apellido"));
    pedido.setEmailCliente((String) usuario.get("email"));
    pedido.setMontoTotal(total);
    pedido.setCantidadTotalItems(cantidadTotal);
    pedido.setEstado("PENDIENTE");
    pedido.setMetodoPago(request.getMetodoPago());
    pedido.setFechaPedido(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    Pedido guardado = repository.save(pedido);

    
    PagosDTO pagosDTO = new PagosDTO();
    pagosDTO.setMonto(guardado.getMontoTotal());
    pagosDTO.setPedidoId(guardado.getId());
    pagosDTO.setUsuarioId(guardado.getUsuarioId());
    
    Map<String, Object> respuestaPago = restTemplate.postForObject(URL_PAGOS + "procesar", pagosDTO, Map.class);
    
    if (respuestaPago == null || !"APROBADO".equals(respuestaPago.get("status"))) {
        throw new ExcepcionPersonalizada("El pago fue rechazado.");
    }

    
    guardado.setEstado("PAGADO");
    repository.save(guardado);


    try {
        NotiDTO notiDTO = new NotiDTO();
        notiDTO.setPedidoId(guardado.getId());
        notiDTO.setUsuarioId(guardado.getUsuarioId());
        notiDTO.setTipo("PAGO");
        notiDTO.setDestinatario(guardado.getEmailCliente());
        notiDTO.setFecha(guardado.getFechaPedido());

        restTemplate.postForObject(URL_NOTIFICACIONES + "enviar", notiDTO, Object.class);
    } catch (Exception e) {
        System.err.println("Error enviando notificación: " + e.getMessage());
    }

        return new PedidoResponseDTO(guardado.getId(), guardado.getNombreCliente(), total, "PAGADO", guardado.getFechaPedido(), "¡Pedido completado!");
    }

    
    public List<Pedido> listarPedidos() {
        return repository.findAll();
    }

    
    public Pedido buscarPorId(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ExcepcionPersonalizada("Pedido no encontrado con ID: " + id));
    }

    
    public List<Pedido> buscarPorUsuario(Integer usuarioId) {

        List<Pedido> pedidos = repository.findByUsuarioId(usuarioId);
        
        if (pedidos == null || pedidos.isEmpty()) {
            
            throw new ExcepcionPersonalizada("No existen pedidos para el usuario con ID: " + usuarioId);
        }
        
        return pedidos;
    }

    
    public Pedido actualizarPedido(Integer id, String nuevoEstado) {
        Pedido p = buscarPorId(id);
        p.setEstado(nuevoEstado);
        return repository.save(p);
    }

    
    public void eliminarPedido(Integer id) {
        if (!repository.existsById(id)) throw new ExcepcionPersonalizada("No existe el pedido para eliminar");
        repository.deleteById(id);
    }
}
