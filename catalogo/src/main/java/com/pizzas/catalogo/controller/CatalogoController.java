package com.pizzas.catalogo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pizzas.catalogo.dto.CatalogoRequestDTO;
import com.pizzas.catalogo.dto.CatalogoResponseDTO;
import com.pizzas.catalogo.service.CatalogoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controlador REST para gestionar el catálogo de pizzas
@RestController
@RequestMapping("/catalogo")
@Tag(name = "Catálogo", description = "Endpoints para gestionar pizzas del catálogo")

public class CatalogoController {
    private final CatalogoService catalogoService;

    // Constructor para inyectar service
    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    // GET http://localhost:8081/catalogo
    @Operation(
            summary = "Listar catálogo",
            description = "Obtiene todas las pizzas registradas en el catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de catálogo obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<CatalogoResponseDTO> pizzas = catalogoService.listar();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de catálogo obtenido correctamente");
        respuesta.put("total", pizzas.size());
        respuesta.put("pizzas", pizzas);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8081/catalogo/pizzas
    @Operation(
            summary = "Listar pizzas",
            description = "Ruta alternativa para obtener todas las pizzas del catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de pizzas obtenido correctamente")
    })
    @GetMapping("/pizzas")
    public ResponseEntity<Map<String, Object>> listarPizzas() {
        List<CatalogoResponseDTO> pizzas = catalogoService.listar();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Listado de pizzas obtenido correctamente");
        respuesta.put("total", pizzas.size());
        respuesta.put("pizzas", pizzas);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8081/catalogo/pizzas/{id}
    @Operation(
            summary = "Buscar pizza por ID",
            description = "Obtiene una pizza específica según su ID. Esta ruta es usada por carrito."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pizza encontrada correctamente"),
            @ApiResponse(responseCode = "404", description = "Pizza no encontrada")
    })
    @GetMapping("/pizzas/{id}")
    public ResponseEntity<CatalogoResponseDTO> buscarPorId(
            @Parameter(description = "ID de la pizza en catálogo", example = "1")
            @PathVariable Integer id) {

        CatalogoResponseDTO pizza = catalogoService.buscarPorId(id);
        return ResponseEntity.ok(pizza);
    }

    // GET http://localhost:8081/catalogo/nombre/{nombre}
    @Operation(
            summary = "Buscar pizzas por nombre",
            description = "Busca pizzas que contengan el nombre indicado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pizzas encontradas por nombre"),
            @ApiResponse(responseCode = "400", description = "Nombre inválido"),
            @ApiResponse(responseCode = "404", description = "No se encontraron pizzas con ese nombre")
    })
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Map<String, Object>> buscarPorNombre(
            @Parameter(description = "Nombre o parte del nombre de la pizza", example = "Pepperoni")
            @PathVariable String nombre) {

        List<CatalogoResponseDTO> pizzas = catalogoService.buscarPorNombre(nombre);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pizzas encontradas por nombre");
        respuesta.put("total", pizzas.size());
        respuesta.put("pizzas", pizzas);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8081/catalogo/tipo/{tipo}
    @Operation(
            summary = "Buscar pizzas por tipo",
            description = "Lista pizzas según su tipo o categoría."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pizzas encontradas por tipo"),
            @ApiResponse(responseCode = "400", description = "Tipo inválido"),
            @ApiResponse(responseCode = "404", description = "No existen pizzas del tipo indicado")
    })
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<Map<String, Object>> buscarPorTipo(
            @Parameter(description = "Tipo o categoría de pizza", example = "Clasica")
            @PathVariable String tipo) {

        List<CatalogoResponseDTO> pizzas = catalogoService.listarPorTipo(tipo);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pizzas encontradas por tipo");
        respuesta.put("total", pizzas.size());
        respuesta.put("pizzas", pizzas);

        return ResponseEntity.ok(respuesta);
    }

    // GET http://localhost:8081/catalogo/tamanio/{tamanio}
    @Operation(
            summary = "Buscar pizzas por tamaño",
            description = "Lista pizzas según el tamaño indicado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pizzas encontradas por tamaño"),
            @ApiResponse(responseCode = "400", description = "Tamaño inválido"),
            @ApiResponse(responseCode = "404", description = "No existen pizzas del tamaño indicado")
    })
    @GetMapping("/tamanio/{tamanio}")
    public ResponseEntity<Map<String, Object>> buscarPorTamanio(
            @Parameter(description = "Tamaño de la pizza", example = "Familiar")
            @PathVariable String tamanio) {

        List<CatalogoResponseDTO> pizzas = catalogoService.listarPorTamanio(tamanio);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pizzas encontradas por tamaño");
        respuesta.put("total", pizzas.size());
        respuesta.put("pizzas", pizzas);

        return ResponseEntity.ok(respuesta);
    }

    // POST http://localhost:8081/catalogo/agregar
    @Operation(
            summary = "Agregar pizza",
            description = "Registra una nueva pizza en el catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pizza agregada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una pizza con ese nombre y tamaño")
    })
    @PostMapping("/agregar")
    public ResponseEntity<Map<String, Object>> agregar(@Valid @RequestBody CatalogoRequestDTO catalogoDTO) {
        CatalogoResponseDTO pizza = catalogoService.guardarPizza(catalogoDTO);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pizza agregada correctamente al catálogo");
        respuesta.put("pizza", pizza);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // PUT http://localhost:8081/catalogo/actualizar/{id}
    @Operation(
            summary = "Actualizar pizza",
            description = "Actualiza los datos de una pizza existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pizza actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Pizza no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe otra pizza con ese nombre y tamaño")
    })
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @Parameter(description = "ID de la pizza a actualizar", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody CatalogoRequestDTO catalogoDTO) {

        CatalogoResponseDTO pizza = catalogoService.actualizarPizza(id, catalogoDTO);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pizza actualizada correctamente");
        respuesta.put("pizza", pizza);

        return ResponseEntity.ok(respuesta);
    }

    // DELETE http://localhost:8081/catalogo/eliminar/{id}
    @Operation(
            summary = "Eliminar pizza",
            description = "Elimina una pizza del catálogo según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pizza eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Pizza no encontrada")
    })
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(
            @Parameter(description = "ID de la pizza a eliminar", example = "1")
            @PathVariable Integer id) {

        catalogoService.eliminarPizza(id);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pizza eliminada correctamente");
        respuesta.put("idEliminado", id);

        return ResponseEntity.ok(respuesta);
    }

}
