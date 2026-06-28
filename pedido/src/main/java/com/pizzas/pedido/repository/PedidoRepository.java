package com.pizzas.pedido.repository;

// Permite trabajar con listas de pedidos
import java.util.List;

// JpaRepository entrega CRUD automático
import org.springframework.data.jpa.repository.JpaRepository;

// Marca esta interfaz como repositorio de Spring
import org.springframework.stereotype.Repository;

// Importa el modelo Pedido
import com.pizzas.pedido.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer>{
    // Lista todos los pedidos realizados por un usuario
    List<Pedido> findByUsuarioId(Integer usuarioId);

    // Lista pedidos según su estado, por ejemplo PAGADO, ENTREGADO o CANCELADO
    List<Pedido> findByEstado(String estado);

    // Lista pedidos de un usuario filtrando por estado
    List<Pedido> findByUsuarioIdAndEstado(Integer usuarioId, String estado);

    // Verifica si un usuario tiene pedidos registrados
    boolean existsByUsuarioId(Integer usuarioId);


}
