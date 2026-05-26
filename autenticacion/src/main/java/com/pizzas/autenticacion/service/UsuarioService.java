package com.pizzas.autenticacion.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pizzas.autenticacion.dto.FormularioIngresoDTO;
import com.pizzas.autenticacion.dto.PerfilUsuarioPublicoDTO;
import com.pizzas.autenticacion.exception.ExcepcionPersonalizada;
import com.pizzas.autenticacion.model.Usuario;
import com.pizzas.autenticacion.repository.UsuarioRepository;

@Service

public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> registrar(Usuario usuario) {
    if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
        throw new ExcepcionPersonalizada("El correo electrónico ya se encuentra registrado.", HttpStatus.BAD_REQUEST);
    }

    usuario.setRol("CLIENTE");
    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

    Usuario usuarioGuardado = usuarioRepository.save(usuario);

    Map<String, Object> respuesta = new LinkedHashMap<>();
    respuesta.put("usuario", usuarioGuardado);

    try {
        Map<String, Object> peticionNoti = new HashMap<>();
        peticionNoti.put("tipo", "REGISTRO");
        peticionNoti.put("destinatario", usuarioGuardado.getEmail());
        peticionNoti.put("usuarioId", usuarioGuardado.getId());
        peticionNoti.put("pedidoId", 0);
        String urlNotificaciones = "http://localhost:8086/notificaciones/enviar";
        
        Map<String, Object> respuestaNoti = restTemplate.postForObject(urlNotificaciones, peticionNoti, Map.class);
        
        if (respuestaNoti != null && respuestaNoti.get("mensaje") != null) {
            respuesta.put("notificacion_sistema", respuestaNoti.get("mensaje"));
        } else {
            respuesta.put("notificacion_sistema", "BIENVENIDO A LAS GORDAS PIZZAS");
        }
        
    } catch (Exception e) {
        System.err.println("Error real en la comunicacion: " + e.getMessage());
        respuesta.put("notificacion_sistema", "Alerta: MS Notificaciones fuera de línea.");
    }

    return respuesta;

    }

    public PerfilUsuarioPublicoDTO iniciarSesion(FormularioIngresoDTO formulario) {
        Usuario usuarioExistente = usuarioRepository.findByEmail(formulario.getEmail())
                .orElseThrow(() -> new ExcepcionPersonalizada("El correo o la contraseña son incorrectos.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(formulario.getPassword(), usuarioExistente.getPassword())) {
            throw new ExcepcionPersonalizada("El correo o la contraseña son incorrectos.", HttpStatus.UNAUTHORIZED);
        }

        return new PerfilUsuarioPublicoDTO(
            usuarioExistente.getId(), usuarioExistente.getNombre(), usuarioExistente.getApellido(),
            usuarioExistente.getEmail(), usuarioExistente.getRol()
        );
    }

    public List<PerfilUsuarioPublicoDTO> listarUsuarios() {
        List<Usuario> listaUsuarios = usuarioRepository.findAll();
        List<PerfilUsuarioPublicoDTO> listaDtos = new ArrayList<>();
        
        for (Usuario u : listaUsuarios) {
            listaDtos.add(new PerfilUsuarioPublicoDTO(u.getId(), u.getNombre(), u.getApellido(), u.getEmail(), u.getRol()));
        }
        return listaDtos;
    }

    public PerfilUsuarioPublicoDTO buscarPorId(Integer id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada("El usuario con el ID especificado no existe.", HttpStatus.NOT_FOUND));
        return new PerfilUsuarioPublicoDTO(u.getId(), u.getNombre(), u.getApellido(), u.getEmail(), u.getRol());
    }

    public Usuario actualizar(Integer id, Usuario datosNuevos) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ExcepcionPersonalizada("No se puede actualizar. El usuario no existe.", HttpStatus.NOT_FOUND));

        u.setNombre(datosNuevos.getNombre());
        u.setApellido(datosNuevos.getApellido());
        u.setEmail(datosNuevos.getEmail());

        if (datosNuevos.getPassword() != null && !datosNuevos.getPassword().isEmpty()) {
            u.setPassword(passwordEncoder.encode(datosNuevos.getPassword()));
        }
        return usuarioRepository.save(u);
    }

    public void eliminar(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ExcepcionPersonalizada("No se puede eliminar. El usuario no existe.", HttpStatus.NOT_FOUND);
        }
        usuarioRepository.deleteById(id);
    }

}
