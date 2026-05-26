package com.pizzas.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.catalogo.model.Catalogo;


@Repository

public interface CatalogoRepository extends JpaRepository<Catalogo, Integer>{
    List<Catalogo> findByNombreIgnoreCase(String nombre);
    List<Catalogo> findByTipoIgnoreCase(String tipo);
}
