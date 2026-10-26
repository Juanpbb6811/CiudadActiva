package com.ciudadactiva.controller;

import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.dto.seguimiento.SeguimientoResponse;
import com.ciudadactiva.dto.usuario.CambiarRolRequest;
import com.ciudadactiva.dto.usuario.UsuarioCreateRequest;
import com.ciudadactiva.dto.usuario.UsuarioResponse;
import com.ciudadactiva.dto.usuario.UsuarioUpdateRequest;
import com.ciudadactiva.entity.RolNombre;
import com.ciudadactiva.service.SeguimientoService;
import com.ciudadactiva.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Usuarios y Administración", description = "Gestión de perfiles propios y administración global de usuarios y roles")
@SecurityRequirement(name = "BearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SeguimientoService seguimientoService;

    public UsuarioController(UsuarioService usuarioService, SeguimientoService seguimientoService) {
        this.usuarioService = usuarioService;
        this.seguimientoService = seguimientoService;
    }

    // ================== PERFIL PROPIO ==================
    @GetMapping("/usuarios/me")
    @Operation(summary = "Obtener Perfil Actual", description = "Retorna la información del usuario autenticado actualmente.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerMiPerfil(Principal principal) {
        UsuarioResponse response = usuarioService.obtenerUsuarioActual(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Perfil obtenido con éxito", response));
    }

    @PutMapping("/usuarios/me")
    @Operation(summary = "Actualizar Perfil Propio", description = "Permite modificar el nombre, teléfono y dirección del usuario autenticado.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarMiPerfil(
            Principal principal,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        UsuarioResponse response = usuarioService.actualizarPerfil(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Perfil actualizado con éxito", response));
    }

    // ================== OPERADORES DISPONIBLES (GESTOR Y ADMIN) ==================
    @GetMapping("/operadores")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Listar Operadores para Asignación", description = "Retorna la lista de operadores activos junto con su cantidad de labores activas para asignaciones de campo.")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarOperadoresParaAsignar() {
        List<UsuarioResponse> operadores = usuarioService.listarOperadoresParaGestor();
        return ResponseEntity.ok(ApiResponse.ok("Lista de operadores disponibles obtenida", operadores));
    }

    // ================== AUDITORÍA GLOBAL DE TRAZABILIDAD RUP (ADMIN) ==================
    @GetMapping("/admin/auditoria")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consultar Matriz de Trazabilidad RUP", description = "Retorna todos los eventos y transiciones inmutables del sistema con hash de verificación.")
    public ResponseEntity<ApiResponse<List<SeguimientoResponse>>> consultarAuditoriaTrazabilidad() {
        List<SeguimientoResponse> auditoria = seguimientoService.obtenerTodosLosSeguimientos();
        return ResponseEntity.ok(ApiResponse.ok("Eventos de auditoría de trazabilidad obtenidos con éxito", auditoria));
    }

    // ================== MÓDULO ADMINISTRATIVO (SOLO ADMIN) ==================
    @GetMapping("/admin/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consultar Usuarios del Sistema (Admin)", description = "Permite listar todos los usuarios o filtrar por rol (ROLE_OPERADOR, ROLE_GESTOR, etc.).")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarUsuarios(
            @RequestParam(required = false) RolNombre rol
    ) {
        List<UsuarioResponse> usuarios = usuarioService.listarUsuarios(rol);
        return ResponseEntity.ok(ApiResponse.ok("Lista de usuarios obtenida", usuarios));
    }

    @GetMapping("/admin/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener Usuario por ID (Admin)", description = "Consulta los detalles de un usuario específico.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerUsuarioPorId(@PathVariable Long id) {
        UsuarioResponse response = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/admin/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear Usuario Administrativo (Admin)", description = "Permite al administrador crear usuarios con roles GESTOR, OPERADOR, ADMIN o CIUDADANO.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> crearUsuario(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse response = usuarioService.crearUsuarioAdministrativo(request);
        return new ResponseEntity<>(ApiResponse.ok("Usuario creado exitosamente", response), HttpStatus.CREATED);
    }

    @PutMapping("/admin/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modificar Datos de Usuario (Admin)", description = "Permite actualizar nombre, contacto y estado activo de un usuario.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        UsuarioResponse response = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado con éxito", response));
    }

    @PatchMapping("/admin/usuarios/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Asignar Roles y Permisos (Admin)", description = "Permite cambiar el rol de un usuario existente.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarRol(
            @PathVariable Long id,
            @Valid @RequestBody CambiarRolRequest request
    ) {
        UsuarioResponse response = usuarioService.cambiarRol(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Rol de usuario modificado con éxito", response));
    }

    @DeleteMapping("/admin/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar / Desactivar Usuario (Admin)", description = "Desactiva la cuenta de un usuario preservando la trazabilidad de sus acciones pasadas.")
    public ResponseEntity<ApiResponse<Void>> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarODesactivarUsuario(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario desactivado exitosamente", null));
    }
}

