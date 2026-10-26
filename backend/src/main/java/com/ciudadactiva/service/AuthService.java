package com.ciudadactiva.service;

import com.ciudadactiva.config.JwtTokenProvider;
import com.ciudadactiva.dto.auth.AuthResponse;
import com.ciudadactiva.dto.auth.LoginRequest;
import com.ciudadactiva.dto.auth.RegisterRequest;
import com.ciudadactiva.dto.usuario.UsuarioResponse;
import com.ciudadactiva.entity.Rol;
import com.ciudadactiva.entity.RolNombre;
import com.ciudadactiva.entity.Usuario;
import com.ciudadactiva.exception.BadRequestException;
import com.ciudadactiva.exception.UnauthorizedException;
import com.ciudadactiva.repository.RolRepository;
import com.ciudadactiva.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El correo electrónico ya se encuentra registrado");
        }

        // Por regla de negocio, el autoregistro ciudadano siempre asigna ROLE_CIUDADANO
        Rol rolCiudadano = rolRepository.findByNombre(RolNombre.ROLE_CIUDADANO)
                .orElseGet(() -> rolRepository.save(new Rol(RolNombre.ROLE_CIUDADANO, "Rol Ciudadano")));

        Usuario usuario = new Usuario(
                request.getNombre(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getTelefono(),
                request.getDireccion(),
                rolCiudadano
        );

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtTokenProvider.generarToken(guardado);

        return new AuthResponse(token, new UsuarioResponse(guardado));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas: correo no registrado"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("La cuenta se encuentra desactivada. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas: contraseña incorrecta");
        }

        String token = jwtTokenProvider.generarToken(usuario);
        return new AuthResponse(token, new UsuarioResponse(usuario));
    }
}
