package com.ciudadactiva.service;

import com.ciudadactiva.config.JwtTokenProvider;
import com.ciudadactiva.dto.auth.*;
import com.ciudadactiva.dto.usuario.UsuarioResponse;
import com.ciudadactiva.entity.PasswordResetToken;
import com.ciudadactiva.entity.Rol;
import com.ciudadactiva.entity.RolNombre;
import com.ciudadactiva.entity.TipoDocumento;
import com.ciudadactiva.entity.Usuario;
import com.ciudadactiva.exception.BadRequestException;
import com.ciudadactiva.exception.UnauthorizedException;
import com.ciudadactiva.repository.PasswordResetTokenRepository;
import com.ciudadactiva.repository.RolRepository;
import com.ciudadactiva.repository.UsuarioRepository;
import com.ciudadactiva.util.TokenCryptoUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenCryptoUtil tokenCryptoUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmailService emailService,
            PasswordResetTokenRepository passwordResetTokenRepository,
            TokenCryptoUtil tokenCryptoUtil
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.tokenCryptoUtil = tokenCryptoUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String emailLimpio = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailLimpio)) {
            throw new BadRequestException("El correo electrónico ya se encuentra registrado");
        }

        // Asignación de rol ciudadano por regla de negocio
        Rol rolCiudadano = rolRepository.findByNombre(RolNombre.ROLE_CIUDADANO)
                .orElseGet(() -> rolRepository.save(new Rol(RolNombre.ROLE_CIUDADANO, "Rol Ciudadano")));

        TipoDocumento tipoDoc = request.getTipoDocumento() != null ? request.getTipoDocumento() : TipoDocumento.CC;

        Usuario usuario = new Usuario(
                request.getPrimerNombre(),
                request.getSegundoNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido(),
                tipoDoc,
                request.getNumeroDocumento(),
                emailLimpio,
                passwordEncoder.encode(request.getPassword()),
                request.getTelefono(),
                request.getDireccion(),
                rolCiudadano
        );

        if (request.getNombre() != null && !request.getNombre().isBlank() && (request.getPrimerNombre() == null || request.getPrimerNombre().isBlank())) {
            usuario.setNombre(request.getNombre().trim());
        }

        // Cumpliendo el requerimiento: los ciudadanos quedan activos y verificados de inmediato
        usuario.setActivo(true);
        usuario.setVerificado(true);

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtTokenProvider.generarToken(guardado);

        AuthResponse response = new AuthResponse(token, new UsuarioResponse(guardado));
        response.setRequiereVerificacion(false);
        response.setCodigoGeneradoDemo(null);
        return response;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas: correo no registrado"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("La cuenta se encuentra desactivada. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas: contraseña incorrecta");
        }

        String token = jwtTokenProvider.generarToken(usuario);
        AuthResponse response = new AuthResponse(token, new UsuarioResponse(usuario));
        response.setRequiereVerificacion(false);
        response.setCodigoGeneradoDemo(null);

        boolean esDemoAna = "ciudadano@ciudadactiva.gov.co".equalsIgnoreCase(usuario.getEmail());
        boolean esStaff = usuario.getRol() != null && usuario.getRol().getNombre() != RolNombre.ROLE_CIUDADANO;
        boolean legalCompleto = esDemoAna || esStaff || esCiudadanoLegalCompleto(usuario);
        response.setRequiereCompletarDatosLegales(!legalCompleto);

        return response;
    }

    private boolean esCiudadanoLegalCompleto(Usuario u) {
        if (u == null) return false;
        String pNom = u.getPrimerNombre();
        String pApe = u.getPrimerApellido();
        String nom = u.getNombre();
        boolean tieneIdentidad = (pNom != null && !pNom.isBlank() && pApe != null && !pApe.isBlank()) || (nom != null && !nom.isBlank());
        String doc = u.getNumeroDocumento();
        String tel = u.getTelefono();
        String dir = u.getDireccion();
        return tieneIdentidad &&
               doc != null && !doc.isBlank() && !doc.contains("PENDIENTE") &&
               tel != null && !tel.isBlank() &&
               dir != null && !dir.isBlank();
    }

    @Transactional
    public AuthResponse loginConGoogle(GoogleAuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<Usuario> existenteOpt = usuarioRepository.findByEmail(email);

        if (existenteOpt.isEmpty()) {
            throw new UnauthorizedException("La cuenta de Google (" + email + ") no se encuentra vinculada a ningún ciudadano registrado. Por favor regístrese formalmente con sus documentos oficiales de Colombia.");
        }

        Usuario usuario = existenteOpt.get();
        if (request.getGoogleId() != null && (usuario.getGoogleId() == null || usuario.getGoogleId().isBlank())) {
            usuario.setGoogleId(request.getGoogleId());
            usuarioRepository.save(usuario);
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("La cuenta se encuentra desactivada. Contacte al administrador.");
        }

        String token = jwtTokenProvider.generarToken(usuario);
        AuthResponse response = new AuthResponse(token, new UsuarioResponse(usuario));
        response.setRequiereVerificacion(false);

        boolean esDemoAna = "ciudadano@ciudadactiva.gov.co".equalsIgnoreCase(usuario.getEmail());
        boolean esStaff = usuario.getRol() != null && usuario.getRol().getNombre() != RolNombre.ROLE_CIUDADANO;
        boolean legalCompleto = esDemoAna || esStaff || esCiudadanoLegalCompleto(usuario);
        response.setRequiereCompletarDatosLegales(!legalCompleto);

        return response;
    }

    @Transactional
    public AuthResponse completarDatosLegales(String email, com.ciudadactiva.dto.auth.CompletarDatosLegalesRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        usuario.setPrimerNombre(req.getPrimerNombre().trim());
        usuario.setSegundoNombre(req.getSegundoNombre() != null && !req.getSegundoNombre().isBlank() ? req.getSegundoNombre().trim() : null);
        usuario.setPrimerApellido(req.getPrimerApellido().trim());
        usuario.setSegundoApellido(req.getSegundoApellido() != null && !req.getSegundoApellido().isBlank() ? req.getSegundoApellido().trim() : null);
        usuario.setTipoDocumento(req.getTipoDocumento());
        usuario.setNumeroDocumento(req.getNumeroDocumento().trim());
        usuario.setTelefono(req.getTelefono().trim());
        usuario.setDireccion(req.getDireccion().trim());
        
        StringBuilder sb = new StringBuilder(req.getPrimerNombre().trim());
        if (req.getSegundoNombre() != null && !req.getSegundoNombre().isBlank()) sb.append(" ").append(req.getSegundoNombre().trim());
        sb.append(" ").append(req.getPrimerApellido().trim());
        if (req.getSegundoApellido() != null && !req.getSegundoApellido().isBlank()) sb.append(" ").append(req.getSegundoApellido().trim());
        usuario.setNombre(sb.toString());

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtTokenProvider.generarToken(guardado);
        AuthResponse response = new AuthResponse(token, new UsuarioResponse(guardado));
        response.setRequiereVerificacion(false);
        response.setRequiereCompletarDatosLegales(false);
        return response;
    }

    @Transactional
    public String vincularCuentaGoogle(String email, String googleId) {
        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
        usuario.setGoogleId(googleId.trim());
        usuarioRepository.save(usuario);
        return "Cuenta de Google vinculada y sincronizada exitosamente";
    }

    /**
     * Solicitud neutra de recuperación con Token Hasheado SHA-256 (Estándar OWASP)
     */
    @Transactional
    public String processForgotPasswordOwasp(String email) {
        if (email == null || email.isBlank()) {
            return "Si el correo existe en nuestro sistema, recibirás las instrucciones de recuperación.";
        }

        String emailLimpio = email.trim().toLowerCase();
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(emailLimpio);
        if (userOpt.isEmpty()) {
            // Respuesta silenciosa/neutra por estándar OWASP para prevenir enumeración de usuarios
            return "Si el correo existe en nuestro sistema, recibirás las instrucciones de recuperación.";
        }

        Usuario user = userOpt.get();
        passwordResetTokenRepository.deleteByUser(user); // Limpia solicitudes previas

        String rawToken = tokenCryptoUtil.generateRawToken();
        String tokenHash = tokenCryptoUtil.hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken(
                tokenHash,
                user,
                LocalDateTime.now().plusMinutes(15)
        );
        passwordResetTokenRepository.save(resetToken);

        emailService.enviarTokenRecuperacionOwasp(user.getEmail(), user.getNombre(), rawToken);

        return "Si el correo existe en nuestro sistema, recibirás las instrucciones de recuperación.";
    }

    /**
     * Validación y cambio de contraseña con Token Criptográfico (Estándar OWASP)
     */
    @Transactional
    public String resetPasswordOwasp(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank() || newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("Token inválido o la contraseña debe tener al menos 6 caracteres.");
        }

        String tokenHash = tokenCryptoUtil.hashToken(rawToken.trim());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Token de recuperación inválido o inexistente."));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("El token de recuperación ha expirado. Por favor solicita uno nuevo.");
        }

        Usuario user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(user);

        // Eliminación inmediata del token tras su uso único
        passwordResetTokenRepository.delete(resetToken);

        // Envío de correo de alerta de seguridad tras cambio exitoso
        emailService.enviarConfirmacionCambioPassword(user.getEmail(), user.getNombre());

        return "Contraseña actualizada exitosamente. Ya puedes iniciar sesión.";
    }

    @Transactional
    public AuthResponse verificarCodigo(VerificarCodigoRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("No se encontró usuario con el correo especificado"));

        usuario.setVerificado(true);
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoVerificacionExpira(null);
        Usuario guardado = usuarioRepository.save(usuario);

        String token = jwtTokenProvider.generarToken(guardado);
        return new AuthResponse(token, new UsuarioResponse(guardado));
    }

    @Transactional
    public String solicitarRecuperacionPassword(ForgotPasswordRequest request) {
        return processForgotPasswordOwasp(request.getEmail());
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        return resetPasswordOwasp(request.getCodigo(), request.getNuevaPassword());
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
        return processForgotPasswordOwasp(request.getEmail());
    }
}
