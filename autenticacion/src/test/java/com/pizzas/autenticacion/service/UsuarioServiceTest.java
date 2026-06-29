package com.pizzas.autenticacion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.pizzas.autenticacion.dto.FormularioIngresoDTO;
import com.pizzas.autenticacion.dto.PerfilUsuarioPublicoDTO;
import com.pizzas.autenticacion.dto.UsuarioRegistroDTO;
import com.pizzas.autenticacion.exception.ExcepcionPersonalizada;
import com.pizzas.autenticacion.model.Usuario;
import com.pizzas.autenticacion.repository.UsuarioRepository;

// Pruebas unitarias para UsuarioService usando JUnit y Mockito
@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail("juan@mail.com");
        usuario.setPassword("passwordEncriptada");
        usuario.setRol("CLIENTE");
    }

    @Test
    void registrar_CuandoEmailNoExiste_DebeRegistrarUsuario() {
        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setNombre(" Juan ");
        dto.setApellido(" Perez ");
        dto.setEmail(" JUAN@MAIL.COM ");
        dto.setPassword("123456");

        when(usuarioRepository.existsByEmail("juan@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("passwordEncriptada");

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioGuardado = invocation.getArgument(0);
            usuarioGuardado.setId(1);
            return usuarioGuardado;
        });

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("mensaje", "Notificación enviada"));

        Map<String, Object> respuesta = usuarioService.registrar(dto);

        assertEquals("Usuario registrado correctamente", respuesta.get("mensaje"));
        assertNotNull(respuesta.get("usuario"));
        assertEquals("Notificación enviada", respuesta.get("notificacion_sistema"));

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void registrar_CuandoEmailYaExiste_DebeLanzarExcepcion() {
        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setNombre("Juan");
        dto.setApellido("Perez");
        dto.setEmail("juan@mail.com");
        dto.setPassword("123456");

        when(usuarioRepository.existsByEmail("juan@mail.com")).thenReturn(true);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> usuarioService.registrar(dto)
        );

        assertEquals("El correo electrónico ya se encuentra registrado.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void iniciarSesion_CuandoCredencialesCorrectas_DebeRetornarPerfilPublico() {
        FormularioIngresoDTO formulario = new FormularioIngresoDTO();
        formulario.setEmail(" JUAN@MAIL.COM ");
        formulario.setPassword("123456");

        when(usuarioRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", "passwordEncriptada")).thenReturn(true);

        PerfilUsuarioPublicoDTO respuesta = usuarioService.iniciarSesion(formulario);

        assertEquals(1, respuesta.getId());
        assertEquals("Juan", respuesta.getNombre());
        assertEquals("Perez", respuesta.getApellido());
        assertEquals("juan@mail.com", respuesta.getEmail());
        assertEquals("CLIENTE", respuesta.getRol());
    }

    @Test
    void iniciarSesion_CuandoPasswordIncorrecta_DebeLanzarExcepcion() {
        FormularioIngresoDTO formulario = new FormularioIngresoDTO();
        formulario.setEmail("juan@mail.com");
        formulario.setPassword("incorrecta");

        when(usuarioRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "passwordEncriptada")).thenReturn(false);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> usuarioService.iniciarSesion(formulario)
        );

        assertEquals("El correo o la contraseña son incorrectos.", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void iniciarSesion_CuandoEmailNoExiste_DebeLanzarExcepcion() {
        FormularioIngresoDTO formulario = new FormularioIngresoDTO();
        formulario.setEmail("noexiste@mail.com");
        formulario.setPassword("123456");

        when(usuarioRepository.findByEmail("noexiste@mail.com")).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> usuarioService.iniciarSesion(formulario)
        );

        assertEquals("El correo o la contraseña son incorrectos.", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void listarUsuarios_DebeRetornarListaDeUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<PerfilUsuarioPublicoDTO> respuesta = usuarioService.listarUsuarios();

        assertEquals(1, respuesta.size());
        assertEquals("Juan", respuesta.get(0).getNombre());
        assertEquals("juan@mail.com", respuesta.get(0).getEmail());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarUsuario() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        PerfilUsuarioPublicoDTO respuesta = usuarioService.buscarPorId(1);

        assertEquals(1, respuesta.getId());
        assertEquals("Juan", respuesta.getNombre());
        assertEquals("CLIENTE", respuesta.getRol());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> usuarioService.buscarPorId(99)
        );

        assertEquals("El usuario con el ID especificado no existe.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void actualizar_CuandoUsuarioExiste_DebeActualizarDatos() {
        Usuario datosNuevos = new Usuario();
        datosNuevos.setNombre("Pedro");
        datosNuevos.setApellido("Gomez");
        datosNuevos.setEmail("pedro@mail.com");
        datosNuevos.setPassword("nueva123");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailAndIdNot("pedro@mail.com", 1)).thenReturn(false);
        when(passwordEncoder.encode("nueva123")).thenReturn("nuevaEncriptada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerfilUsuarioPublicoDTO respuesta = usuarioService.actualizar(1, datosNuevos);

        assertEquals("Pedro", respuesta.getNombre());
        assertEquals("Gomez", respuesta.getApellido());
        assertEquals("pedro@mail.com", respuesta.getEmail());

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void eliminar_CuandoUsuarioExiste_DebeEliminarUsuario() {
        when(usuarioRepository.existsById(1)).thenReturn(true);

        usuarioService.eliminar(1);

        verify(usuarioRepository).deleteById(1);
    }

    @Test
    void eliminar_CuandoUsuarioNoExiste_DebeLanzarExcepcion() {
        when(usuarioRepository.existsById(99)).thenReturn(false);

        ExcepcionPersonalizada ex = assertThrows(
                ExcepcionPersonalizada.class,
                () -> usuarioService.eliminar(99)
        );

        assertEquals("No se puede eliminar. El usuario no existe.", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        verify(usuarioRepository, never()).deleteById(99);
    }

}
