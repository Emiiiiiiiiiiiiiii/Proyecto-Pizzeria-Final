package com.pizzas.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pizzas.catalogo.model.Catalogo;

// Repository para consultar pizzas del catálogo
@Repository
public interface CatalogoRepository extends JpaRepository<Catalogo, Integer>{
    // Busca pizzas por nombre exacto ignorando mayúsculas
    List<Catalogo> findByNombreIgnoreCase(String nombre);

    // Busca pizzas que contengan parte del nombre
    List<Catalogo> findByNombreContainingIgnoreCase(String nombre);

    // Lista pizzas por tipo
    List<Catalogo> findByTipoIgnoreCase(String tipo);

    // Lista pizzas por tamaño
    List<Catalogo> findByTamanioIgnoreCase(String tamanio);

    // Evita cargar pizzas repetidas con el mismo nombre y tamaño
    boolean existsByNombreIgnoreCaseAndTamanioIgnoreCase(String nombre, String tamanio);
}
