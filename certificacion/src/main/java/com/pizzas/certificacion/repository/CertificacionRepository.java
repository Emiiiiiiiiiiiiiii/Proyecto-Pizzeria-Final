package com.pizzas.certificacion.repository;

// Permite trabajar con listas de certificaciones
import java.util.List;

// Permite manejar resultados que pueden existir o no
import java.util.Optional;

// JpaRepository entrega CRUD automático
import org.springframework.data.jpa.repository.JpaRepository;

// Marca esta interfaz como repositorio de Spring
import org.springframework.stereotype.Repository;

// Importa el modelo Certificacion
import com.pizzas.certificacion.model.Certificacion;

@Repository
public interface CertificacionRepository extends JpaRepository<Certificacion, Integer> {
// Busca una certificación por el ID del pedido
    Optional<Certificacion> findByPedidoId(Integer pedidoId);

    // Lista certificaciones asociadas a un usuario
    List<Certificacion> findByUsuarioId(Integer usuarioId);

    // Lista certificaciones según el estado del pedido
    List<Certificacion> findByEstadoPedido(String estadoPedido);

    // Lista certificaciones que tienen o no tienen reseña
    List<Certificacion> findByTieneResena(Boolean tieneResena);

    // Verifica si ya existe una certificación para un pedido
    boolean existsByPedidoId(Integer pedidoId);

}
