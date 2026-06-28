package com.pizzas.autenticacion.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pizzas.autenticacion.dto.FormularioIngresoDTO;
import com.pizzas.autenticacion.dto.PerfilUsuarioPublicoDTO;
import com.pizzas.autenticacion.dto.UsuarioRegistroDTO;
import com.pizzas.autenticacion.exception.ExcepcionPersonalizada;
import com.pizzas.autenticacion.model.Usuario;
import com.pizzas.autenticacion.repository.UsuarioRepository;

@Service
public class UsuarioService {
    // Permite registrar eventos importantes del microservicio
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    // Repositorio para acceder a la tabla usuarios
    private final UsuarioRepository usuarioRepository;

    // Encoder para encriptar y comparar contraseñas
    private final PasswordEncoder passwordEncoder;

    // Cliente usado para comunicarse con otros microservicios
    private final RestTemplate restTemplate;

    // URL del microservicio notificaciones desde application.properties
    @Value("${url.notificaciones}")
    private String URL_NOTIFICACIONES;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }

    // Registra un usuario nuevo y lo guarda con rol CLIENTE
    public Map<String, Object> registrar(UsuarioRegistroDTO dto) {

        String emailNormalizado = dto.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            logger.warn("Intento de registro con correo ya existente: {}", emailNormalizado);
            throw new ExcepcionPersonalizada("El correo electrónico ya se encuentra registrado.", HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setApellido(dto.getApellido().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol("CLIENTE");

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        logger.info("Usuario registrado correctamente con ID: {}", usuarioGuardado.getId());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Usuario registrado correctamente");
        respuesta.put("usuario", convertirAPerfilPublico(usuarioGuardado));

        try {
            Map<String, Object> peticionNoti = new HashMap<>();
            peticionNoti.put("tipo", "REGISTRO");
            peticionNoti.put("destinatario", usuarioGuardado.getEmail());
            peticionNoti.put("usuarioId", usuarioGuardado.getId());
            peticionNoti.put("pedidoId", 0);

            Map<String, Object> respuestaNoti = restTemplate.postForObject(
                    URL_NOTIFICACIONES + "enviar",
                    peticionNoti,
                    Map.class
            );

            if (respuestaNoti != null && respuestaNoti.get("mensaje") != null) {
                respuesta.put("notificacion_sistema", respuestaNoti.get("mensaje"));
            } else {
                respuesta.put("notificacion_sistema", "BIENVENIDO A LAS GORDAS PIZZAS");
            }

            logger.info("Notificación de registro enviada para usuario ID: {}", usuarioGuardado.getId());

        } catch (Exception e) {
            logger.warn("No se pudo comunicar con MS Notificaciones: {}", e.getMessage());
            respuesta.put("notificacion_sistema", "Alerta: MS Notificaciones fuera de línea.");
        }

        return respuesta;
    }

    // Valida email y contraseña para iniciar sesión
    public PerfilUsuarioPublicoDTO iniciarSesion(FormularioIngresoDTO formulario) {

        String emailNormalizado = formulario.getEmail().trim().toLowerCase();

        Usuario usuarioExistente = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> {
                    logger.warn("Intento de login con correo no registrado: {}", emailNormalizado);
                    return new ExcepcionPersonalizada("El correo o la contraseña son incorrectos.", HttpStatus.UNAUTHORIZED);
                });

        if (!passwordEncoder.matches(formulario.getPassword(), usuarioExistente.getPassword())) {
            logger.warn("Contraseña incorrecta para usuario ID: {}", usuarioExistente.getId());
            throw new ExcepcionPersonalizada("El correo o la contraseña son incorrectos.", HttpStatus.UNAUTHORIZED);
        }

        logger.info("Login exitoso para usuario ID: {}", usuarioExistente.getId());

        return convertirAPerfilPublico(usuarioExistente);
    }

    // Lista todos los usuarios sin mostrar contraseñas
    public List<PerfilUsuarioPublicoDTO> listarUsuarios() {

        List<Usuario> listaUsuarios = usuarioRepository.findAll();
        List<PerfilUsuarioPublicoDTO> listaDtos = new ArrayList<>();

        for (Usuario usuario : listaUsuarios) {
            listaDtos.add(convertirAPerfilPublico(usuario));
        }

        logger.info("Listado de usuarios obtenido correctamente. Total: {}", listaDtos.size());

        return listaDtos;
    }

    // Busca un usuario por ID y devuelve solo sus datos públicos
    public PerfilUsuarioPublicoDTO buscarPorId(Integer id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado con ID: {}", id);
                    return new ExcepcionPersonalizada("El usuario con el ID especificado no existe.", HttpStatus.NOT_FOUND);
                });

        return convertirAPerfilPublico(usuario);
    }

    // Actualiza datos básicos del usuario
    public PerfilUsuarioPublicoDTO actualizar(Integer id, Usuario datosNuevos) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Intento de actualizar usuario inexistente con ID: {}", id);
                    return new ExcepcionPersonalizada("No se puede actualizar. El usuario no existe.", HttpStatus.NOT_FOUND);
                });

        String emailNormalizado = datosNuevos.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmailAndIdNot(emailNormalizado, id)) {
            logger.warn("Intento de actualizar usuario ID {} con email ya usado: {}", id, emailNormalizado);
            throw new ExcepcionPersonalizada("El correo electrónico ya está siendo usado por otro usuario.", HttpStatus.BAD_REQUEST);
        }

        usuario.setNombre(datosNuevos.getNombre().trim());
        usuario.setApellido(datosNuevos.getApellido().trim());
        usuario.setEmail(emailNormalizado);

        if (datosNuevos.getPassword() != null && !datosNuevos.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(datosNuevos.getPassword()));
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        logger.info("Usuario actualizado correctamente con ID: {}", usuarioActualizado.getId());

        return convertirAPerfilPublico(usuarioActualizado);
    }

    // Elimina un usuario si existe
    public void eliminar(Integer id) {

        if (!usuarioRepository.existsById(id)) {
            logger.warn("Intento de eliminar usuario inexistente con ID: {}", id);
            throw new ExcepcionPersonalizada("No se puede eliminar. El usuario no existe.", HttpStatus.NOT_FOUND);
        }

        usuarioRepository.deleteById(id);

        logger.info("Usuario eliminado correctamente con ID: {}", id);
    }

    // Convierte Usuario a DTO público para no exponer la contraseña
    private PerfilUsuarioPublicoDTO convertirAPerfilPublico(Usuario usuario) {
        return new PerfilUsuarioPublicoDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }

}
