package com.ciudadactiva.service;

import com.ciudadactiva.dto.seguimiento.SeguimientoCreateRequest;
import com.ciudadactiva.dto.seguimiento.SeguimientoResponse;
import com.ciudadactiva.entity.*;
import com.ciudadactiva.exception.ForbiddenException;
import com.ciudadactiva.exception.ResourceNotFoundException;
import com.ciudadactiva.repository.AsignacionRepository;
import com.ciudadactiva.repository.ReporteRepository;
import com.ciudadactiva.repository.SeguimientoRepository;
import com.ciudadactiva.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionRepository asignacionRepository;

    public SeguimientoService(
            SeguimientoRepository seguimientoRepository,
            ReporteRepository reporteRepository,
            UsuarioRepository usuarioRepository,
            AsignacionRepository asignacionRepository
    ) {
        this.seguimientoRepository = seguimientoRepository;
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.asignacionRepository = asignacionRepository;
    }

    // ================== RF06: REGISTRO DE AVANCES OPERATIVOS ==================
    @Transactional
    public SeguimientoResponse registrarAvance(Long reporteId, String emailUsuario, SeguimientoCreateRequest request) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + reporteId));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        // Regla 8: Si es operador, validar que esté asignado a este reporte
        if (usuario.getRol() != null && usuario.getRol().getNombre() == RolNombre.ROLE_OPERADOR) {
            Optional<Asignacion> asignacion = asignacionRepository.findByReporte_IdAndActivaTrue(reporteId);
            if (asignacion.isEmpty() || !asignacion.get().getOperador().getId().equals(usuario.getId())) {
                throw new ForbiddenException("No puedes registrar avances en un reporte que no te ha sido asignado.");
            }
        }

        EstadoReporte estadoAnterior = reporte.getEstado();
        EstadoReporte estadoNuevo = request.getNuevoEstado() != null ? request.getNuevoEstado() : estadoAnterior;

        if (request.getNuevoEstado() != null && request.getNuevoEstado() != estadoAnterior) {
            reporte.setEstado(request.getNuevoEstado());
            reporteRepository.save(reporte);
        }

        String accion = (request.getAccion() != null && !request.getAccion().isBlank())
                ? request.getAccion()
                : "AVANCE_OPERATIVO";

        Seguimiento nuevoSeguimiento = new Seguimiento(
                reporte,
                usuario,
                estadoAnterior,
                estadoNuevo,
                accion,
                request.getObservaciones(),
                request.getEvidenciaUrl()
        );

        Seguimiento guardado = seguimientoRepository.save(nuevoSeguimiento);
        return new SeguimientoResponse(guardado);
    }

    // ================== RF07: LÍNEA DE TIEMPO / SEGUIMIENTO ==================
    @Transactional(readOnly = true)
    public List<SeguimientoResponse> obtenerLineaDeTiempo(Long reporteId) {
        if (!reporteRepository.existsById(reporteId)) {
            throw new ResourceNotFoundException("Reporte no encontrado con ID: " + reporteId);
        }

        return seguimientoRepository.findByReporte_IdOrderByCreadoEnAsc(reporteId).stream()
                .map(SeguimientoResponse::new)
                .collect(Collectors.toList());
    }

    // ================== AUDITORÍA GLOBAL (ADMIN - RNF04) ==================
    @Transactional(readOnly = true)
    public List<SeguimientoResponse> obtenerTodosLosSeguimientos() {
        return seguimientoRepository.findAllByOrderByCreadoEnDesc().stream()
                .map(SeguimientoResponse::new)
                .collect(Collectors.toList());
    }
}
