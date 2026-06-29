package com.pizzas.autenticacion.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzas.autenticacion.dto.FormularioIngresoDTO;
import com.pizzas.autenticacion.dto.PerfilUsuarioPublicoDTO;
import com.pizzas.autenticacion.dto.UsuarioRegistroDTO;
import com.pizzas.autenticacion.exception.ExcepcionPersonalizada;
import com.pizzas.autenticacion.exception.ManejadorErrores;
import com.pizzas.autenticacion.model.Usuario;
import com.pizzas.autenticacion.service.UsuarioService;

// Pruebas unitarias del controller usando MockMvc
@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
public class UsuarioControllerTest {

     @Autowired
    private MockMvc mockMvc;

    // Se crea manualmente para evitar error de bean ObjectMapper en el test
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void registrar_CuandoDatosValidos_DebeResponderCreated() throws Exception {
        PerfilUsuarioPublicoDTO perfil = new PerfilUsuarioPublicoDTO(
                1,
                "Juan",
                "Perez",
                "juan@mail.com",
                "CLIENTE"
        );

        Map<String, Object> respuestaService = new LinkedHashMap<>();
        respuestaService.put("mensaje", "Usuario registrado correctamente");
        respuestaService.put("usuario", perfil);
        respuestaService.put("notificacion_sistema", "Notificación enviada");

        when(usuarioService.registrar(any(UsuarioRegistroDTO.class))).thenReturn(respuestaService);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Juan");
        body.put("apellido", "Perez");
        body.put("email", "juan@mail.com");
        body.put("password", "123456");

        mockMvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Usuario registrado correctamente"))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.usuario.email").value("juan@mail.com"))
                .andExpect(jsonPath("$.notificacion_sistema").value("Notificación enviada"));

        verify(usuarioService).registrar(any(UsuarioRegistroDTO.class));
    }

    @Test
    void registrar_CuandoEmailInvalido_DebeResponderBadRequest() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Juan");
        body.put("apellido", "Perez");
        body.put("email", "correo-malo");
        body.put("password", "123456");

        mockMvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).registrar(any(UsuarioRegistroDTO.class));
    }

    @Test
    void login_CuandoCredencialesCorrectas_DebeResponderOk() throws Exception {
        PerfilUsuarioPublicoDTO perfil = new PerfilUsuarioPublicoDTO(
                1,
                "Juan",
                "Perez",
                "juan@mail.com",
                "CLIENTE"
        );

        when(usuarioService.iniciarSesion(any(FormularioIngresoDTO.class))).thenReturn(perfil);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", "juan@mail.com");
        body.put("password", "123456");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.usuario.email").value("juan@mail.com"));

        verify(usuarioService).iniciarSesion(any(FormularioIngresoDTO.class));
    }

    @Test
    void login_CuandoCredencialesIncorrectas_DebeResponderUnauthorized() throws Exception {
        when(usuarioService.iniciarSesion(any(FormularioIngresoDTO.class)))
                .thenThrow(new ExcepcionPersonalizada(
                        "El correo o la contraseña son incorrectos.",
                        HttpStatus.UNAUTHORIZED
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", "juan@mail.com");
        body.put("password", "incorrecta");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El correo o la contraseña son incorrectos."));
    }

    @Test
    void listar_DebeResponderUsuarios() throws Exception {
        PerfilUsuarioPublicoDTO perfil = new PerfilUsuarioPublicoDTO(
                1,
                "Juan",
                "Perez",
                "juan@mail.com",
                "CLIENTE"
        );

        when(usuarioService.listarUsuarios()).thenReturn(List.of(perfil));

        mockMvc.perform(get("/auth/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuarios obtenidos correctamente"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.usuarios[0].email").value("juan@mail.com"));

        verify(usuarioService).listarUsuarios();
    }

    @Test
    void buscarPorId_CuandoExiste_DebeResponderUsuario() throws Exception {
        PerfilUsuarioPublicoDTO perfil = new PerfilUsuarioPublicoDTO(
                1,
                "Juan",
                "Perez",
                "juan@mail.com",
                "CLIENTE"
        );

        when(usuarioService.buscarPorId(1)).thenReturn(perfil);

        mockMvc.perform(get("/auth/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario encontrado correctamente"))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.usuario.email").value("juan@mail.com"));

        verify(usuarioService).buscarPorId(1);
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeResponderNotFound() throws Exception {
        when(usuarioService.buscarPorId(99))
                .thenThrow(new ExcepcionPersonalizada(
                        "El usuario con el ID especificado no existe.",
                        HttpStatus.NOT_FOUND
                ));

        mockMvc.perform(get("/auth/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El usuario con el ID especificado no existe."));
    }

    @Test
    void actualizar_CuandoExiste_DebeResponderOk() throws Exception {
        PerfilUsuarioPublicoDTO perfilActualizado = new PerfilUsuarioPublicoDTO(
                1,
                "Pedro",
                "Gomez",
                "pedro@mail.com",
                "CLIENTE"
        );

        when(usuarioService.actualizar(eq(1), any(Usuario.class))).thenReturn(perfilActualizado);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Pedro");
        body.put("apellido", "Gomez");
        body.put("email", "pedro@mail.com");
        body.put("password", "nueva123");
        body.put("rol", "CLIENTE");

        mockMvc.perform(put("/auth/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario actualizado correctamente"))
                .andExpect(jsonPath("$.usuario.nombre").value("Pedro"))
                .andExpect(jsonPath("$.usuario.email").value("pedro@mail.com"));

        verify(usuarioService).actualizar(eq(1), any(Usuario.class));
    }

    @Test
    void eliminar_CuandoExiste_DebeResponderOk() throws Exception {
        doNothing().when(usuarioService).eliminar(1);

        mockMvc.perform(delete("/auth/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario eliminado correctamente"));

        verify(usuarioService).eliminar(1);
    }

}
