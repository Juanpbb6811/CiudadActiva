package com.ciudadactiva.controller;

import com.ciudadactiva.dto.asignacion.AsignacionResponse;
import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.service.AsignacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Asignaciones de Trabajo", description = "Endpoints para la gestión y consulta de asignaciones operativas")
@SecurityRequirement(name = "BearerAuth")
public class AsignacionController {

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @GetMapping("/asignaciones")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Listar Asignaciones Activas (Gestor)", description = "Permite al gestor monitorear todos los reportes actualmente asignados a operadores.")
    public ResponseEntity<ApiResponse<List<AsignacionResponse>>> listarAsignacionesActivas() {
        List<AsignacionResponse> asignaciones = asignacionService.listarAsignacionesActivas();
        return ResponseEntity.ok(ApiResponse.ok("Asignaciones activas obtenidas", asignaciones));
    }

    @GetMapping({"/asignaciones/mis-asignaciones", "/reportes/asignados"})
    @PreAuthorize("hasAnyRole('OPERADOR', 'GESTOR', 'ADMIN')")
    @Operation(summary = "Consultar Asignaciones de Trabajo (Operador)", description = "Permite al operador consultar las incidencias activas asignadas a él.")
    public ResponseEntity<ApiResponse<List<AsignacionResponse>>> listarMisAsignaciones(Principal principal) {
        List<AsignacionResponse> asignaciones = asignacionService.listarMisAsignaciones(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Mis asignaciones de trabajo obtenidas con éxito", asignaciones));
    }
}
