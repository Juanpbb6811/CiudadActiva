package com.ciudadactiva.service;

import com.ciudadactiva.dto.comentario.ComentarioRequest;
import com.ciudadactiva.dto.comentario.ComentarioResponse;
import com.ciudadactiva.entity.*;
import com.ciudadactiva.exception.ForbiddenException;
import com.ciudadactiva.exception.ResourceNotFoundException;
import com.ciudadactiva.repository.ComentarioRepository;
import com.ciudadactiva.repository.ReporteRepository;
import com.ciudadactiva.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            ReporteRepository reporteRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.comentarioRepository = comentarioRepository;
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ================== CASO DE USO: REALIZAR COMENTARIO EN UN REPORTE ==================
    @Transactional
    public ComentarioResponse agregarComentario(Long reporteId, String emailUsuario, ComentarioRequest request) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + reporteId));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        Comentario comentario = new Comentario(reporte, usuario, request.getContenido());
        Comentario guardado = comentarioRepository.save(comentario);

        return new ComentarioResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorReporte(Long reporteId) {
        return comentarioRepository.findByReporte_IdOrderByCreadoEnAsc(reporteId).stream()
                .map(ComentarioResponse::new)
                .collect(Collectors.toList());
    }

    // ================== CASO DE USO: MODIFICAR COMENTARIO REALIZADO ==================
    @Transactional
    public ComentarioResponse modificarComentario(Long comentarioId, String emailUsuario, ComentarioRequest request) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con ID: " + comentarioId));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        // Regla 7: Solo el autor puede modificar su comentario
        if (!comentario.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No puedes modificar un comentario publicado por otro usuario.");
        }

        comentario.setContenido(request.getContenido());
        return new ComentarioResponse(comentarioRepository.save(comentario));
    }

    // ================== CASO DE USO: ELIMINAR COMENTARIO PROPIO ==================
    @Transactional
    public void eliminarComentario(Long comentarioId, String emailUsuario) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con ID: " + comentarioId));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        boolean esAutor = comentario.getUsuario().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRol() != null && usuario.getRol().getNombre() == RolNombre.ROLE_ADMIN;

        // Regla 7: Solo el propio autor o el administrador puede eliminar un comentario
        if (!esAutor && !esAdmin) {
            throw new ForbiddenException("No tienes permisos para eliminar este comentario.");
        }

        comentarioRepository.delete(comentario);
    }
}
