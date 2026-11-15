package com.ciudadactiva.controller;

import com.ciudadactiva.dto.auth.*;
import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro ciudadano, verificación con OTP de 6 dígitos, recuperación y cambio de contraseñas")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/register", "/registro"})
    @Operation(summary = "Registro de Ciudadano", description = "Permite a cualquier habitante registrarse en la aplicación con rol CIUDADANO y genera un código OTP para verificación.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.ok("Ciudadano registrado exitosamente. Se ha generado un código de verificación.", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicio de Sesión", description = "Autentica usuarios (Ciudadano, Gestor, Operador, Administrador) y retorna token JWT con perfil.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Inicio de sesión exitoso", response));
    }

    @PostMapping("/google")
    @Operation(summary = "Acceso con Google Vinculado", description = "Autentica un ciudadano que ya tenga vinculada su cuenta de Google y emite token JWT.")
    public ResponseEntity<ApiResponse<AuthResponse>> loginConGoogle(@RequestBody GoogleAuthRequest request) {
        AuthResponse response = authService.loginConGoogle(request);
        return ResponseEntity.ok(ApiResponse.ok("Autenticación con Google exitosa", response));
    }

    @PostMapping("/completar-datos-legales")
    @Operation(summary = "Completar Registro Legal de Ciudadano", description = "Permite a un usuario existente completar sus datos oficiales de ciudadanía colombiana.")
    public ResponseEntity<ApiResponse<AuthResponse>> completarDatosLegales(
            Principal principal,
            @Valid @RequestBody com.ciudadactiva.dto.auth.CompletarDatosLegalesRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Debe autenticarse para completar sus datos legales"));
        }
        AuthResponse response = authService.completarDatosLegales(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Datos legales registrados con éxito. Ahora es Ciudadano formal.", response));
    }

    @PostMapping("/vincular-google")
    @Operation(summary = "Vincular Cuenta de Google", description = "Asocia el correo/id de Google a un ciudadano autenticado.")
    public ResponseEntity<ApiResponse<String>> vincularGoogle(
            Principal principal,
            @RequestBody java.util.Map<String, String> body
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Debe autenticarse para vincular Google"));
        }
        String googleId = body.getOrDefault("googleId", body.get("email"));
        if (googleId == null || googleId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("El identificador o correo de Google es requerido"));
        }
        String msg = authService.vincularCuentaGoogle(principal.getName(), googleId);
        return ResponseEntity.ok(ApiResponse.ok(msg, msg));
    }

    @PostMapping("/verificar-codigo")
    @Operation(summary = "Verificación de Cuenta con Código OTP", description = "Valida el código de 6 dígitos generado al azar para activar la cuenta de usuario.")
    public ResponseEntity<ApiResponse<AuthResponse>> verificarCodigo(@Valid @RequestBody VerificarCodigoRequest request) {
        AuthResponse response = authService.verificarCodigo(request);
        return ResponseEntity.ok(ApiResponse.ok("Cuenta verificada y activada con éxito", response));
    }

    @PostMapping("/reenviar-codigo")
    @Operation(summary = "Reenviar Código OTP", description = "Genera y reenvía un nuevo código aleatorio de 6 dígitos para verificación o recuperación.")
    public ResponseEntity<ApiResponse<String>> reenviarCodigo(@Valid @RequestBody ReenviarCodigoRequest request) {
        String mensaje = authService.reenviarCodigo(request);
        return ResponseEntity.ok(ApiResponse.ok(mensaje, mensaje));
    }

    @PostMapping({"/recuperar-password", "/forgot-password"})
    @Operation(summary = "Solicitar Recuperación de Contraseña", description = "Genera un código OTP de 6 dígitos para recuperación de contraseña olvidada.")
    public ResponseEntity<ApiResponse<String>> solicitarRecuperacion(@Valid @RequestBody ForgotPasswordRequest request) {
        String mensaje = authService.solicitarRecuperacionPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(mensaje, mensaje));
    }

    @PostMapping({"/reset-password", "/restablecer-password"})
    @Operation(summary = "Restablecer Contraseña con Código OTP", description = "Permite cambiar la contraseña suministrando el código de 6 dígitos recibido.")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String mensaje = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(mensaje, mensaje));
    }

    @PostMapping("/cambiar-password")
    @Operation(summary = "Cambiar Contraseña (Usuario Autenticado)", description = "Permite a un usuario autenticado cambiar su contraseña actual por una nueva.")
    public ResponseEntity<ApiResponse<String>> cambiarPassword(
            Principal principal,
            @Valid @RequestBody CambiarPasswordRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Debe iniciar sesión para cambiar la contraseña"));
        }
        String mensaje = authService.cambiarPassword(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok(mensaje, mensaje));
    }
}

