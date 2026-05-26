package com.pizzas.autenticacion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.autenticacion.model.Usuario;

@Repository

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
    Optional<Usuario> findByEmail(String email);

}
