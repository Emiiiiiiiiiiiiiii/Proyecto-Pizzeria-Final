package com.pizzas.catalogo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzas.catalogo.model.Catalogo;
import com.pizzas.catalogo.service.CatalogoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/catalogo")

public class CatalogoController {
    @Autowired
    private CatalogoService catalogoService;

    @GetMapping
    public List<Catalogo> listar() {
        return catalogoService.listar();
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<List<Catalogo>> buscarPorNombre(@PathVariable String nombre) {
        List<Catalogo> pizzas = catalogoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(pizzas);
    }

    @GetMapping("/tipo/{tipo}")
    public List<Catalogo> buscarPorTipo(@PathVariable String tipo) {
        return catalogoService.listarPorTipo(tipo);
    }

    @GetMapping("/pizzas/{id}")
    public Catalogo buscarPorId(@PathVariable Integer id) {
        return catalogoService.buscarPorId(id);
    }

    @PostMapping("/agregar")
    public ResponseEntity<String> agregar(@Valid @RequestBody Catalogo catalogo) {
        catalogoService.guardarPizzas(catalogo);
        return ResponseEntity.status(HttpStatus.CREATED).body("¡Pizza agregada correctamente al catálogo!");
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Integer id) {
        catalogoService.eliminarPizzas(id);
        return ResponseEntity.ok("¡Pizza eliminada correctamente!");
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizar(@PathVariable Integer id, @Valid @RequestBody Catalogo catalogo) {
        catalogoService.actualizarPizzas(id, catalogo);
        return ResponseEntity.ok("¡Pizza actualizada correctamente!");
    }

}
