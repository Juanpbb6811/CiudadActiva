package com.ciudadactiva.service;

import com.ciudadactiva.dto.asignacion.AsignacionCreateRequest;
import com.ciudadactiva.dto.asignacion.AsignacionResponse;
import com.ciudadactiva.entity.*;
import com.ciudadactiva.exception.BadRequestException;
import com.ciudadactiva.exception.BusinessRuleException;
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
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SeguimientoRepository seguimientoRepository;

    public AsignacionService(
            AsignacionRepository asignacionRepository,
            ReporteRepository reporteRepository,
            UsuarioRepository usuarioRepository,
            SeguimientoRepository seguimientoRepository
    ) {
        this.asignacionRepository = asignacionRepository;
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    // ================== RF05: ASIGNACIÓN DE RESPONSABLES ==================
    @Transactional
    public AsignacionResponse asignarOperador(Long reporteId, String emailGestor, AsignacionCreateRequest request) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + reporteId));

        // RN09: solo reportes admitidos (o ya en proceso, para reasignar) pueden recibir operador
        if (reporte.getEstado() == EstadoReporte.RECHAZADO || reporte.getEstado() == EstadoReporte.CERRADO) {
            throw new BusinessRuleException("No se puede asignar un operador porque el reporte está " + reporte.getEstado().name().toLowerCase() + ".");
        }
        if (reporte.getEstado() != EstadoReporte.ADMITIDO && reporte.getEstado() != EstadoReporte.EN_PROCESO) {
            throw new BusinessRuleException("Primero admite el reporte para poder asignarle un operador. Estado actual: " + reporte.getEstado().name().replace("_", " ").toLowerCase() + ".");
        }

        Usuario gestor = usuarioRepository.findByEmail(emailGestor)
                .orElseThrow(() -> new ResourceNotFoundException("Gestor no encontrado: " + emailGestor));

        Usuario operador = usuarioRepository.findById(request.getOperadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operador no encontrado con ID: " + request.getOperadorId()));

        if (operador.getRol() == null || operador.getRol().getNombre() != RolNombre.ROLE_OPERADOR) {
            throw new BadRequestException("El usuario seleccionado no tiene el rol de OPERADOR");
        }
        if (!Boolean.TRUE.equals(operador.getActivo())) {
            throw new BusinessRuleException("El operador seleccionado está inactivo y no puede recibir asignaciones.");
        }

        // Si ya tenía asignación activa previa, se desactiva
        Optional<Asignacion> asignacionPrevia = asignacionRepository.findByReporte_IdAndActivaTrue(reporteId);
        boolean esReasignacion = false;
        if (asignacionPrevia.isPresent()) {
            Asignacion previa = asignacionPrevia.get();
            previa.setActiva(false);
            asignacionRepository.save(previa);
            esReasignacion = true;
        }

        Asignacion nuevaAsignacion = new Asignacion(reporte, operador, gestor, request.getNotas());
        Asignacion guardada = asignacionRepository.save(nuevaAsignacion);

        // Transición de estado a EN_PROCESO
        EstadoReporte estadoAnterior = reporte.getEstado();
        if (reporte.getEstado() != EstadoReporte.EN_PROCESO) {
            reporte.setEstado(EstadoReporte.EN_PROCESO);
            reporteRepository.save(reporte);
        }

        // Registro de Trazabilidad inmutable
        String accion = esReasignacion ? "REASIGNACION_OPERADOR" : "ASIGNACION_OPERADOR";
        String detalle = "Reporte asignado al operador " + operador.getNombre() + " (" + operador.getEmail() + ") por el gestor " + gestor.getNombre();
        if (request.getNotas() != null && !request.getNotas().isBlank()) {
            detalle += ". Notas: " + request.getNotas();
        }

        Seguimiento seg = new Seguimiento(
                reporte,
                gestor,
                estadoAnterior,
                EstadoReporte.EN_PROCESO,
                accion,
                detalle,
                null
        );
        seguimientoRepository.save(seg);

        return new AsignacionResponse(guardada);
    }

    // ================== GESTOR: LISTAR ASIGNACIONES ACTIVAS ==================
    @Transactional(readOnly = true)
    public List<AsignacionResponse> listarAsignacionesActivas() {
        return asignacionRepository.findByActivaTrueOrderByCreadoEnDesc().stream()
                .map(AsignacionResponse::new)
                .collect(Collectors.toList());
    }

    // ================== OPERADOR: CONSULTAR ASIGNACIONES DE TRABAJO ==================
    @Transactional(readOnly = true)
    public List<AsignacionResponse> listarMisAsignaciones(String emailOperador) {
        Usuario operador = usuarioRepository.findByEmail(emailOperador)
                .orElseThrow(() -> new ResourceNotFoundException("Operador no encontrado: " + emailOperador));

        return asignacionRepository.findByOperador_IdAndActivaTrueOrderByCreadoEnDesc(operador.getId()).stream()
                .map(AsignacionResponse::new)
                .collect(Collectors.toList());
    }

    // ================== GESTOR: ELIMINAR / CANCELAR ASIGNACIÓN ==================
    @Transactional
    public void cancelarAsignacion(Long reporteId, String emailGestor) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + reporteId));

        Usuario gestor = usuarioRepository.findByEmail(emailGestor)
                .orElseThrow(() -> new ResourceNotFoundException("Gestor no encontrado: " + emailGestor));

        Asignacion activa = asignacionRepository.findByReporte_IdAndActivaTrue(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una asignación activa para este reporte"));

        activa.setActiva(false);
        asignacionRepository.save(activa);

        // Devolver a estado ADMITIDO para permitir nueva asignación
        EstadoReporte anterior = reporte.getEstado();
        reporte.setEstado(EstadoReporte.ADMITIDO);
        reporteRepository.save(reporte);

        Seguimiento seg = new Seguimiento(
                reporte,
                gestor,
                anterior,
                EstadoReporte.ADMITIDO,
                "CANCELACION_ASIGNACION",
                "Asignación del operador " + activa.getOperador().getNombre() + " cancelada por el gestor.",
                null
        );
        seguimientoRepository.save(seg);
    }

    // ================== GESTOR: REASIGNAR (RN05 / RNF08) ==================
    /**
     * Reasigna una asignación activa a otro operador. La asignación previa se desactiva lógicamente
     * (activa = false) y se crea una nueva, conservando el historial completo. Nunca se borra nada.
     */
    @Transactional
    public AsignacionResponse reasignar(Long asignacionId, String emailGestor, AsignacionCreateRequest request) {
        Asignacion actual = asignacionRepository.findById(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!Boolean.TRUE.equals(actual.getActiva())) {
            throw new BusinessRuleException("Solo se pueden reasignar asignaciones activas.");
        }
        if (actual.getOperador() != null && actual.getOperador().getId().equals(request.getOperadorId())) {
            throw new BusinessRuleException("El reporte ya está asignado a ese operador. Selecciona un operador diferente.");
        }
        // asignarOperador desactiva la previa, crea la nueva y registra REASIGNACION_OPERADOR en el seguimiento
        return asignarOperador(actual.getReporte().getId(), emailGestor, request);
    }

    // ================== HISTORIAL DE ASIGNACIONES DE UN REPORTE ==================
    @Transactional(readOnly = true)
    public List<AsignacionResponse> historialPorReporte(Long reporteId) {
        return asignacionRepository.findByReporte_IdOrderByCreadoEnDesc(reporteId).stream()
                .map(AsignacionResponse::new)
                .collect(Collectors.toList());
    }
}
