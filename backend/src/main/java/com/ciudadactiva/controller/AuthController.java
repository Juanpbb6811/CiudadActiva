package com.ciudadactiva.controller;

import com.ciudadactiva.dto.auth.AuthResponse;
import com.ciudadactiva.dto.auth.LoginRequest;
import com.ciudadactiva.dto.auth.RegisterRequest;
import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro ciudadano e inicio de sesión con JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/register", "/registro"})
    @Operation(summary = "Registro de Ciudadano", description = "Permite a cualquier habitante registrarse en la aplicación con rol CIUDADANO.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.ok("Ciudadano registrado exitosamente", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicio de Sesión", description = "Autentica usuarios (Ciudadano, Gestor, Operador, Administrador) y retorna token JWT con perfil.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Inicio de sesión exitoso", response));
    }
}
