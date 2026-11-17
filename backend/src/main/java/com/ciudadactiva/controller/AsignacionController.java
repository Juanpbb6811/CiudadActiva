package com.ciudadactiva.controller;

import com.ciudadactiva.dto.asignacion.AsignacionCreateRequest;
import com.ciudadactiva.dto.asignacion.AsignacionResponse;
import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.service.AsignacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PatchMapping("/asignaciones/{asignacionId}/reasignar")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Reasignar Operador (Gestor)",
            description = "Desactiva lógicamente la asignación actual (activa=false) y crea una nueva con otro operador, sin borrar historial (RN05 / RNF08).")
    public ResponseEntity<ApiResponse<AsignacionResponse>> reasignar(
            @PathVariable Long asignacionId,
            Principal principal,
            @Valid @RequestBody AsignacionCreateRequest request
    ) {
        AsignacionResponse response = asignacionService.reasignar(asignacionId, principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Reporte reasignado exitosamente", response));
    }

    @GetMapping("/asignaciones/historial")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Historial de Asignaciones de un Reporte", description = "Lista todas las asignaciones (activas e inactivas) de un reporte.")
    public ResponseEntity<ApiResponse<List<AsignacionResponse>>> historial(@RequestParam Long reporteId) {
        return ResponseEntity.ok(ApiResponse.ok("Historial de asignaciones", asignacionService.historialPorReporte(reporteId)));
    }
}
