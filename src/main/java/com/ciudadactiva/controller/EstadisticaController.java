package com.ciudadactiva.controller;

import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.dto.common.SemaforoDTO;
import com.ciudadactiva.dto.estadistica.EstadisticasResponse;
import com.ciudadactiva.service.EstadisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/estadisticas")
@Tag(name = "Estadísticas e Indicadores", description = "Endpoints para generación de indicadores de gestión, reportes, semáforo y tiempos")
@SecurityRequirement(name = "BearerAuth")
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    public EstadisticaController(EstadisticaService estadisticaService) {
        this.estadisticaService = estadisticaService;
    }

    // ================== RF09: INDICADORES ESTADÍSTICOS ==================
    @GetMapping("/reportes")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Consultar Indicadores Estadísticos", description = "Genera indicadores agrupados por estado, categoría, prioridad y tiempo promedio de atención en horas, incluyendo semáforos.")
    public ResponseEntity<ApiResponse<EstadisticasResponse>> obtenerEstadisticas() {
        EstadisticasResponse response = estadisticaService.obtenerEstadisticas();
        return ResponseEntity.ok(ApiResponse.ok("Indicadores estadísticos generados", response));
    }

    // ================== FUNCIÓN SEMÁFORO DE REPORTES ==================
    @GetMapping("/semaforo/calcular")
    @Operation(summary = "Calcular Color y Nivel de Semáforo", description = "Calcula el color semáforo (Verde 1 reporte -> Rojo 10 reportes), código Hexadecimal, nivel y porcentaje de saturación según el número de reportes.")
    public ResponseEntity<ApiResponse<SemaforoDTO>> calcularSemaforo(
            @Parameter(description = "Número de reportes a evaluar (1 a 10+)")
            @RequestParam(defaultValue = "1") int cantidad) {
        SemaforoDTO semaforo = estadisticaService.calcularSemaforo(cantidad);
        return ResponseEntity.ok(ApiResponse.ok("Cálculo de semáforo generado con éxito", semaforo));
    }

    @GetMapping("/semaforo")
    @Operation(summary = "Consultar Semáforos Actuales por Categoría y Global", description = "Retorna el semáforo de saturación global y por cada categoría del sistema.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerSemaforosActuales() {
        EstadisticasResponse stats = estadisticaService.obtenerEstadisticas();
        Map<String, Object> data = Map.of(
                "semaforoGlobal", stats.getSemaforoGlobal(),
                "semaforoPorCategoria", stats.getSemaforoPorCategoria(),
                "totalReportes", stats.getTotalReportes(),
                "reportesPendientes", stats.getReportesPendientes()
        );
        return ResponseEntity.ok(ApiResponse.ok("Semáforos actuales consultados", data));
    }
}
