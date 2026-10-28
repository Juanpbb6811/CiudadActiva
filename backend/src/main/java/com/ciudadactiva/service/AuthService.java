package com.ciudadactiva.service;

import com.ciudadactiva.config.JwtTokenProvider;
import com.ciudadactiva.dto.auth.*;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

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

        // Generar código OTP aleatorio de 6 dígitos para verificación de cuenta
        String codigoOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
        usuario.setVerificado(false);
        usuario.setCodigoVerificacion(codigoOtp);
        usuario.setCodigoVerificacionExpira(LocalDateTime.now().plusMinutes(15));

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtTokenProvider.generarToken(guardado);

        AuthResponse response = new AuthResponse(token, new UsuarioResponse(guardado));
        response.setRequiereVerificacion(true);
        response.setCodigoGeneradoDemo(codigoOtp);
        return response;
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
        AuthResponse response = new AuthResponse(token, new UsuarioResponse(usuario));
        response.setRequiereVerificacion(!Boolean.TRUE.equals(usuario.getVerificado()));
        response.setCodigoGeneradoDemo(usuario.getCodigoVerificacion());
        return response;
    }

    @Transactional
    public AuthResponse verificarCodigo(VerificarCodigoRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("No se encontró usuario con el correo especificado"));

        if (Boolean.TRUE.equals(usuario.getVerificado())) {
            String token = jwtTokenProvider.generarToken(usuario);
            return new AuthResponse(token, new UsuarioResponse(usuario));
        }

        if (usuario.getCodigoVerificacion() == null || usuario.getCodigoVerificacionExpira() == null) {
            throw new BadRequestException("No hay ningún código de verificación activo. Por favor solicita uno nuevo.");
        }

        if (LocalDateTime.now().isAfter(usuario.getCodigoVerificacionExpira())) {
            throw new BadRequestException("El código de verificación ha expirado. Por favor solicita uno nuevo.");
        }

        if (!usuario.getCodigoVerificacion().trim().equals(request.getCodigo().trim())) {
            throw new BadRequestException("El código de verificación ingresado es incorrecto.");
        }

        // Código válido: cuenta verificada
        usuario.setVerificado(true);
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoVerificacionExpira(null);
        Usuario guardado = usuarioRepository.save(usuario);

        String token = jwtTokenProvider.generarToken(guardado);
        return new AuthResponse(token, new UsuarioResponse(guardado));
    }

    @Transactional
    public String solicitarRecuperacionPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            return "Si el correo está registrado, se ha generado y enviado un código de recuperación de 6 dígitos.";
        }

        String codigoOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
        usuario.setCodigoRecuperacion(codigoOtp);
        usuario.setCodigoRecuperacionExpira(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        return "Código de recuperación generado con éxito: " + codigoOtp;
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado con el correo suministrado."));

        if (usuario.getCodigoRecuperacion() == null || usuario.getCodigoRecuperacionExpira() == null) {
            throw new BadRequestException("No hay una solicitud de recuperación activa o el código ya fue utilizado.");
        }

        if (LocalDateTime.now().isAfter(usuario.getCodigoRecuperacionExpira())) {
            throw new BadRequestException("El código de recuperación ha expirado. Por favor solicita uno nuevo.");
        }

        if (!usuario.getCodigoRecuperacion().trim().equals(request.getCodigo().trim())) {
            throw new BadRequestException("El código de recuperación ingresado es incorrecto.");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuario.setCodigoRecuperacion(null);
        usuario.setCodigoRecuperacionExpira(null);
        usuarioRepository.save(usuario);

        return "Contraseña actualizada exitosamente. Ya puedes iniciar sesión con tu nueva contraseña.";
    }

    @Transactional
    public String cambiarPassword(String email, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado."));

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta.");
        }

        usuario.setPassword(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
        return "Contraseña actualizada con éxito.";
    }

    @Transactional
    public String reenviarCodigo(ReenviarCodigoRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado con el correo suministrado."));

        String codigoOtp = String.format("%06d", secureRandom.nextInt(1_000_000));

        if ("RECUPERACION".equalsIgnoreCase(request.getTipo())) {
            usuario.setCodigoRecuperacion(codigoOtp);
            usuario.setCodigoRecuperacionExpira(LocalDateTime.now().plusMinutes(15));
            usuarioRepository.save(usuario);
            return "Nuevo código de recuperación generado: " + codigoOtp;
        } else {
            usuario.setCodigoVerificacion(codigoOtp);
            usuario.setCodigoVerificacionExpira(LocalDateTime.now().plusMinutes(15));
            usuarioRepository.save(usuario);
            return "Nuevo código de verificación generado: " + codigoOtp;
        }
    }
}

