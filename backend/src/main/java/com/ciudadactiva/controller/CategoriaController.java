package com.ciudadactiva.controller;

import com.ciudadactiva.dto.categoria.CategoriaRequest;
import com.ciudadactiva.dto.categoria.CategoriaResponse;
import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías de Incidencias", description = "Endpoints para configuración y consulta de categorías de reporte urbano")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar Categorías Activas", description = "Retorna las categorías activas para que los ciudadanos seleccionen al reportar.")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarActivas() {
        List<CategoriaResponse> categorias = categoriaService.listarActivas();
        return ResponseEntity.ok(ApiResponse.ok("Categorías activas obtenidas", categorias));
    }

    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Listar Todas las Categorías (Admin)", description = "Retorna categorías activas e inactivas.")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarTodas() {
        List<CategoriaResponse> categorias = categoriaService.listarTodas();
        return ResponseEntity.ok(ApiResponse.ok("Todas las categorías", categorias));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener Detalle de Categoría", description = "Retorna una categoría por su ID.")
    public ResponseEntity<ApiResponse<CategoriaResponse>> obtenerPorId(@PathVariable Long id) {
        CategoriaResponse response = categoriaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Crear Categoría (Admin)", description = "Permite registrar una nueva categoría de incidencia.")
    public ResponseEntity<ApiResponse<CategoriaResponse>> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = categoriaService.crearCategoria(request);
        return new ResponseEntity<>(ApiResponse.ok("Categoría creada con éxito", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Actualizar Categoría (Admin)", description = "Permite modificar nombre, icono o descripción de una categoría.")
    public ResponseEntity<ApiResponse<CategoriaResponse>> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request
    ) {
        CategoriaResponse response = categoriaService.actualizarCategoria(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Categoría actualizada con éxito", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Eliminar o Inactivar Categoría (Admin)", description = "Elimina físicamente si no tiene reportes o la desactiva para mantener integridad.")
    public ResponseEntity<ApiResponse<Void>> eliminarCategoria(@PathVariable Long id) {
        categoriaService.eliminarODesactivarCategoria(id);
        return ResponseEntity.ok(ApiResponse.ok("Categoría eliminada o desactivada con éxito", null));
    }
}
