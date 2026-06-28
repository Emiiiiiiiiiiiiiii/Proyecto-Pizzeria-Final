package com.pizzas.carrito.repository;

// Permite devolver resultados que pueden existir o no
import java.util.Optional;

// Permite trabajar con listas de carritos
import java.util.List;

// JpaRepository entrega CRUD automático
import org.springframework.data.jpa.repository.JpaRepository;

// Marca esta interfaz como repositorio de Spring
import org.springframework.stereotype.Repository;

// Importa el modelo Carrito
import com.pizzas.carrito.model.Carrito;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer>{
    // Busca un producto específico dentro del carrito de un usuario
    Optional<Carrito> findByUsuarioIdAndCatalogoId(Integer usuarioId, Integer catalogoId);

    // Lista todos los productos del carrito de un usuario
    List<Carrito> findByUsuarioId(Integer usuarioId);

    // Verifica si un usuario ya tiene un producto específico en el carrito
    boolean existsByUsuarioIdAndCatalogoId(Integer usuarioId, Integer catalogoId);

    // Elimina todos los productos del carrito de un usuario
    void deleteByUsuarioId(Integer usuarioId);
}
