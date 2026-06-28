package com.pizzas.autenticacion.repository;

// Permite trabajar con Optional cuando puede que un usuario no exista
import java.util.Optional;

// JpaRepository entrega CRUD automático: save, findAll, findById, deleteById, etc.
import org.springframework.data.jpa.repository.JpaRepository;

// Marca esta interfaz como repositorio de Spring
import org.springframework.stereotype.Repository;

// Importa el modelo Usuario
import com.pizzas.autenticacion.model.Usuario;

@Repository

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
    // Busca un usuario por email, útil para login y para validar si ya existe
    Optional<Usuario> findByEmail(String email);

    // Verifica si ya existe un usuario con ese email
    boolean existsByEmail(String email);

    // Verifica si existe otro usuario con el mismo email, pero con distinto ID
    boolean existsByEmailAndIdNot(String email, Integer id);

}
