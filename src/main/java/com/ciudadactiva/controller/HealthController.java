package com.ciudadactiva.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Monitoreo del estado y disponibilidad del servicio backend")
public class HealthController {

    @GetMapping
    @Operation(summary = "Verificar Salud del Backend", description = "Retorna el estado de disponibilidad del servicio para Render, Docker y clientes móviles.")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "CiudadActiva Backend API",
                "version", "1.0.0",
                "environment", "Production / Render / Supabase Ready",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
