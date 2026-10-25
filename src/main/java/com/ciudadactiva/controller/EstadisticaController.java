package com.ciudadactiva.controller;

import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.dto.estadistica.EstadisticasResponse;
import com.ciudadactiva.service.EstadisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@Tag(name = "Estadísticas e Indicadores", description = "Endpoints para generación de indicadores de gestión, reportes y tiempos")
@SecurityRequirement(name = "BearerAuth")
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    public EstadisticaController(EstadisticaService estadisticaService) {
        this.estadisticaService = estadisticaService;
    }

    // ================== RF09: INDICADORES ESTADÍSTICOS ==================
    @GetMapping("/reportes")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Consultar Indicadores Estadísticos", description = "Genera indicadores agrupados por estado, categoría, prioridad y tiempo promedio de atención en horas.")
    public ResponseEntity<ApiResponse<EstadisticasResponse>> obtenerEstadisticas() {
        EstadisticasResponse response = estadisticaService.obtenerEstadisticas();
        return ResponseEntity.ok(ApiResponse.ok("Indicadores estadísticos generados", response));
    }
}
