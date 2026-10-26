package com.ciudadactiva.service;

import com.ciudadactiva.dto.usuario.CambiarRolRequest;
import com.ciudadactiva.dto.usuario.UsuarioCreateRequest;
import com.ciudadactiva.dto.usuario.UsuarioResponse;
import com.ciudadactiva.dto.usuario.UsuarioUpdateRequest;
import com.ciudadactiva.entity.Rol;
import com.ciudadactiva.entity.RolNombre;
import com.ciudadactiva.entity.Usuario;
import com.ciudadactiva.exception.BadRequestException;
import com.ciudadactiva.exception.ResourceNotFoundException;
import com.ciudadactiva.repository.AsignacionRepository;
import com.ciudadactiva.repository.RolRepository;
import com.ciudadactiva.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AsignacionRepository asignacionRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, AsignacionRepository asignacionRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.asignacionRepository = asignacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioActual(String email) {
        Usuario usuario = buscarPorEmail(email);
        return new UsuarioResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(String email, UsuarioUpdateRequest request) {
        Usuario usuario = buscarPorEmail(email);
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getDireccion() != null) {
            usuario.setDireccion(request.getDireccion());
        }
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    // ================== MÓDULO DE ADMINISTRACIÓN (CASO DE USO ADMIN) ==================

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios(RolNombre rolFiltro) {
        List<Usuario> usuarios;
        if (rolFiltro != null) {
            usuarios = usuarioRepository.findByRol_Nombre(rolFiltro);
        } else {
            usuarios = usuarioRepository.findAllByOrderByCreadoEnDesc();
        }
        return usuarios.stream().map(u -> {
            UsuarioResponse res = new UsuarioResponse(u);
            if (u.getRol() != null && u.getRol().getNombre() == RolNombre.ROLE_OPERADOR) {
                res.setLaboresActivas(asignacionRepository.countByOperador_IdAndActivaTrue(u.getId()));
            }
            return res;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarOperadoresParaGestor() {
        return usuarioRepository.findByRol_Nombre(RolNombre.ROLE_OPERADOR).stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .map(u -> {
                    UsuarioResponse res = new UsuarioResponse(u);
                    res.setLaboresActivas(asignacionRepository.countByOperador_IdAndActivaTrue(u.getId()));
                    return res;
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return new UsuarioResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crearUsuarioAdministrativo(UsuarioCreateRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El correo ya está registrado");
        }

        Rol rol = rolRepository.findByNombre(request.getRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + request.getRol()));

        Usuario usuario = new Usuario(
                request.getNombre(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getTelefono(),
                request.getDireccion(),
                rol
        );

        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getDireccion() != null) {
            usuario.setDireccion(request.getDireccion());
        }
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarRol(Long id, CambiarRolRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        Rol rol = rolRepository.findByNombre(request.getRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + request.getRol()));

        usuario.setRol(rol);
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminarODesactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        // Desactivación lógica para preservar integridad referencial y trazabilidad
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }
}
