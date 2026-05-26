package com.pizzas.resenas.service;

import com.pizzas.resenas.dto.ResenaRequestDTO;
import com.pizzas.resenas.dto.ResenaResponseDTO;
import com.pizzas.resenas.dto.ResenaUpdateDTO;
import com.pizzas.resenas.exception.ExcepcionPersonalizada;
import com.pizzas.resenas.model.Resena;
import com.pizzas.resenas.repository.ResenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url.auth}") private String URL_AUTH;
    @Value("${url.catalogo}") private String URL_CATALOGO;
    @Value("${url.pedidos}") private String URL_PEDIDOS;

    // Listar todas las reseñas
    public List<ResenaResponseDTO> listarTodas() {
        List<Resena> listaResenas = repository.findAll();
        List<ResenaResponseDTO> listaDTO = new ArrayList<>();
        
        for (Resena resena : listaResenas) {
            listaDTO.add(convertirAResponse(resena));
        }
        return listaDTO;
    }

    // Buscar reseñas por usuario
    public List<ResenaResponseDTO> buscarPorUsuario(Integer usuarioId) {
        List<Resena> listaResenas = repository.findByUsuarioId(usuarioId);
        if (listaResenas.isEmpty()) throw new ExcepcionPersonalizada("No hay reseñas para este usuario");
        
        List<ResenaResponseDTO> listaDTO = new ArrayList<>();
        for (Resena resena : listaResenas) {
            listaDTO.add(convertirAResponse(resena));
        }
        return listaDTO;
    }

    // Buscar reseñas por producto (catálogo)
    public List<ResenaResponseDTO> buscarPorCatalogo(Integer catalogoId) {
        List<Resena> listaResenas = repository.findByCatalogoId(catalogoId);
        if (listaResenas.isEmpty()) throw new ExcepcionPersonalizada("No hay reseñas para este producto");
        
        List<ResenaResponseDTO> listaDTO = new ArrayList<>();
        for (Resena resena : listaResenas) {
            listaDTO.add(convertirAResponse(resena));
        }
        return listaDTO;
    }

    // Buscar reseñas por pedido
    public List<ResenaResponseDTO> buscarPorPedido(Integer pedidoId) {
        List<Resena> listaResenas = repository.findByPedidoId(pedidoId);
        if (listaResenas.isEmpty()) throw new ExcepcionPersonalizada("No hay reseñas para este pedido");
        
        List<ResenaResponseDTO> listaDTO = new ArrayList<>();
        for (Resena resena : listaResenas) {
            listaDTO.add(convertirAResponse(resena));
        }
        return listaDTO;
    }

    // Guardar una nueva reseña
    public ResenaResponseDTO guardarResena(ResenaRequestDTO request) {
        // 1. NUESTRO WALKIE-TALKIE CON EL AUTH
        // Validamos si el usuario existe antes de permitir la reseña
        try {
            restTemplate.getForObject(URL_AUTH + "usuarios/" + request.getUsuarioId(), Map.class);
        } catch (Exception e) {
            throw new ExcepcionPersonalizada("Error: El usuario indicado no existe.");
        }

        // 2. NUESTRO WALKIE-TALKIE CON LOS PEDIDOS
        // Validamos si el pedido existe Y si le pertenece al usuario
        try {
            Map<String, Object> pedido = restTemplate.getForObject(URL_PEDIDOS + request.getPedidoId(), Map.class);
            
            if (pedido == null) {
                throw new ExcepcionPersonalizada("Error: El pedido no existe.");
            }
            
            // Verificamos que el usuario del pedido coincida con el usuario de la reseña
            // Asegúrate que "usuarioId" coincida exactamente con el nombre de tu campo en Pedidos
            Integer usuarioDelPedido = (Integer) pedido.get("usuarioId");
            
            if (usuarioDelPedido == null || !usuarioDelPedido.equals(request.getUsuarioId())) {
                throw new ExcepcionPersonalizada("Error: Este pedido no le pertenece al usuario indicado.");
            }
        } catch (ExcepcionPersonalizada e) {
            throw e; // Relanzamos nuestra propia excepción para que el manejador la capture
        } catch (Exception e) {
            throw new ExcepcionPersonalizada("Error: El pedido no es válido.");
        }

        // 3. NUESTRO WALKIE-TALKIE CON EL CATÁLOGO
        // Validamos si la pizza existe antes de guardar
        try {
            restTemplate.getForObject(URL_CATALOGO + "pizzas/" + request.getCatalogoId(), Map.class);
        } catch (Exception e) {
            throw new ExcepcionPersonalizada("Error: No puedes dejar una reseña porque esa pizza no existe en el catálogo.");
        }

        // 4. GUARDAR EN BASE DE DATOS
        Resena resena = new Resena();
        resena.setUsuarioId(request.getUsuarioId());
        resena.setPedidoId(request.getPedidoId());
        resena.setCatalogoId(request.getCatalogoId());
        resena.setComentario(request.getComentario());
        resena.setEstrellas(request.getEstrellas());

        return convertirAResponse(repository.save(resena));
    }

    // ELIMINAR
    public void eliminarResena(Integer id) {
        if (!repository.existsById(id)) throw new ExcepcionPersonalizada("No se pudo eliminar: Reseña no encontrada");
        repository.deleteById(id);
    }

    public ResenaResponseDTO actualizarResena(Integer id, ResenaUpdateDTO request) {
    // 1. Buscamos la entidad original
    Resena resenaExistente = repository.findById(id)
        .orElseThrow(() -> new ExcepcionPersonalizada("No se puede actualizar: Reseña no encontrada"));

    // 2. Actualizamos SOLO si los datos no son nulos (Actualización parcial)
    if (request.getComentario() != null) {
        resenaExistente.setComentario(request.getComentario());
    }
    if (request.getEstrellas() != null) {
        resenaExistente.setEstrellas(request.getEstrellas());
    }

    // 3. Guardamos y convertimos a DTO de respuesta
    return convertirAResponse(repository.save(resenaExistente));
}

    // LÓGICA DE CONVERSIÓN ENRIQUECIDA
    // Este método toma los IDs de la base de datos y consulta a otros microservicios 
    // para obtener los nombres reales del usuario y la pizza.
    private ResenaResponseDTO convertirAResponse(Resena resena) {
        // Llamada a servicios externos para obtener nombres reales
        Map<String, Object> usuario = restTemplate.getForObject(URL_AUTH + "usuarios/" + resena.getUsuarioId(), Map.class);
        Map<String, Object> producto = restTemplate.getForObject(URL_CATALOGO + "pizzas/" + resena.getCatalogoId(), Map.class);

        String nombreCliente = (usuario != null) ? (String) usuario.get("nombre") : "Usuario Desconocido";
        String nombrePizza = (producto != null) ? (String) producto.get("nombre") : "Producto Desconocido";

        return new ResenaResponseDTO(nombreCliente, nombrePizza, resena.getComentario(), resena.getEstrellas());
    }
}