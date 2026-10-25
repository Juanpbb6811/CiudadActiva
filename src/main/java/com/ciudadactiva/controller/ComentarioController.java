package com.ciudadactiva.controller;

import com.ciudadactiva.dto.comentario.ComentarioRequest;
import com.ciudadactiva.dto.comentario.ComentarioResponse;
import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/comentarios")
@Tag(name = "Comentarios de Incidencias", description = "Endpoints para editar y eliminar comentarios propios sobre reportes")
@SecurityRequirement(name = "BearerAuth")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar Comentario Realizado", description = "Permite al autor editar el contenido de su comentario.")
    public ResponseEntity<ApiResponse<ComentarioResponse>> modificarComentario(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody ComentarioRequest request
    ) {
        ComentarioResponse response = comentarioService.modificarComentario(id, principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Comentario modificado con éxito", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Comentario Propio", description = "Permite al autor o al administrador eliminar un comentario.")
    public ResponseEntity<ApiResponse<Void>> eliminarComentario(
            @PathVariable Long id,
            Principal principal
    ) {
        comentarioService.eliminarComentario(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Comentario eliminado con éxito", null));
    }
}
