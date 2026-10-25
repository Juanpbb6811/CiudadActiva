package com.ciudadactiva.service;

import com.ciudadactiva.dto.asignacion.AsignacionResponse;
import com.ciudadactiva.dto.comentario.ComentarioResponse;
import com.ciudadactiva.dto.reporte.*;
import com.ciudadactiva.dto.seguimiento.SeguimientoResponse;
import com.ciudadactiva.entity.*;
import com.ciudadactiva.exception.BusinessRuleException;
import com.ciudadactiva.exception.ResourceNotFoundException;
import com.ciudadactiva.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final AsignacionRepository asignacionRepository;
    private final ComentarioRepository comentarioRepository;

    public ReporteService(
            ReporteRepository reporteRepository,
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository,
            SeguimientoRepository seguimientoRepository,
            AsignacionRepository asignacionRepository,
            ComentarioRepository comentarioRepository
    ) {
        this.reporteRepository = reporteRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.asignacionRepository = asignacionRepository;
        this.comentarioRepository = comentarioRepository;
    }

    // ================== RF02: REGISTRO DE REPORTES ==================
    @Transactional
    public ReporteResponse crearReporte(String emailCiudadano, ReporteCreateRequest request) {
        Usuario ciudadano = usuarioRepository.findByEmail(emailCiudadano)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario ciudadano no encontrado: " + emailCiudadano));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + request.getCategoriaId()));

        Reporte reporte = new Reporte(
                request.getTitulo(),
                request.getDescripcion(),
                request.getLatitud(),
                request.getLongitud(),
                request.getDireccion(),
                request.getImagenUrl(),
                categoria,
                ciudadano
        );
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setPrioridad(PrioridadReporte.MEDIA);

        Reporte guardado = reporteRepository.save(reporte);

        // Regla 5: Trazabilidad inmutable - Registro inicial en Seguimiento
        Seguimiento primerSeguimiento = new Seguimiento(
                guardado,
                ciudadano,
                null,
                EstadoReporte.PENDIENTE,
                "CREACION_REPORTE",
                "Incidencia registrada por el ciudadano con dirección: " + (request.getDireccion() != null ? request.getDireccion() : "Sin dirección específica"),
                request.getImagenUrl()
        );
        seguimientoRepository.save(primerSeguimiento);

        return mapearReporteResponse(guardado);
    }

    // ================== RF03: CONSULTA Y FILTROS ==================
    @Transactional(readOnly = true)
    public List<ReporteResponse> filtrarReportes(Long categoriaId, EstadoReporte estado, PrioridadReporte prioridad, Long ciudadanoId) {
        return reporteRepository.filtrarReportes(categoriaId, estado, prioridad, ciudadanoId).stream()
                .map(this::mapearReporteResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> obtenerMisReportes(String emailCiudadano) {
        Usuario ciudadano = usuarioRepository.findByEmail(emailCiudadano)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailCiudadano));

        return reporteRepository.findByCiudadano_IdOrderByCreadoEnDesc(ciudadano.getId()).stream()
                .map(this::mapearReporteResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReporteDetalleResponse obtenerDetalle(Long id) {
        Reporte reporte = buscarPorId(id);
        ReporteDetalleResponse response = new ReporteDetalleResponse(reporte);

        // Cargar asignación activa si existe
        asignacionRepository.findByReporte_IdAndActivaTrue(id)
                .ifPresent(asig -> {
                    response.setAsignacionActual(new AsignacionResponse(asig));
                    if (asig.getOperador() != null) {
                        response.setOperadorAsignadoNombre(asig.getOperador().getNombre());
                    }
                });

        // Cargar línea de tiempo (historial cronológico de seguimiento)
        List<SeguimientoResponse> lineaTiempo = seguimientoRepository.findByReporte_IdOrderByCreadoEnAsc(id).stream()
                .map(SeguimientoResponse::new)
                .collect(Collectors.toList());
        response.setLineaDeTiempo(lineaTiempo);

        // Cargar comentarios
        List<ComentarioResponse> comentarios = comentarioRepository.findByReporte_IdOrderByCreadoEnAsc(id).stream()
                .map(ComentarioResponse::new)
                .collect(Collectors.toList());
        response.setComentarios(comentarios);

        return response;
    }

    // ================== RF04: VALIDACIÓN DE REPORTES (GESTOR) ==================
    @Transactional
    public ReporteResponse validarReporte(Long id, String emailGestor, ValidarReporteRequest request) {
        Reporte reporte = buscarPorId(id);
        Usuario gestor = usuarioRepository.findByEmail(emailGestor)
                .orElseThrow(() -> new ResourceNotFoundException("Gestor no encontrado: " + emailGestor));

        EstadoReporte estadoAnterior = reporte.getEstado();

        // Regla 1: Todo reporte rechazado DEBE incluir una justificación clara y visible
        if (Boolean.FALSE.equals(request.getAdmitido())) {
            if (request.getJustificacion() == null || request.getJustificacion().trim().isEmpty()) {
                throw new BusinessRuleException("Regla de Negocio: Todo reporte rechazado debe incluir una justificación clara y visible para el ciudadano.");
            }
            reporte.setEstado(EstadoReporte.RECHAZADO);
            reporte.setJustificacionRechazo(request.getJustificacion());

            // Trazabilidad
            Seguimiento seguimiento = new Seguimiento(
                    reporte,
                    gestor,
                    estadoAnterior,
                    EstadoReporte.RECHAZADO,
                    "RECHAZO_REPORTE",
                    "Reporte rechazado por el gestor. Motivo: " + request.getJustificacion(),
                    null
            );
            seguimientoRepository.save(seguimiento);

        } else {
            // Reporte admitido
            reporte.setEstado(EstadoReporte.ADMITIDO);
            reporte.setJustificacionRechazo(null);

            if (request.getPrioridad() != null) {
                reporte.setPrioridad(request.getPrioridad());
            }

            String nota = request.getJustificacion() != null && !request.getJustificacion().isBlank()
                    ? request.getJustificacion()
                    : "Reporte validado y admitido para programación operativa.";

            Seguimiento seguimiento = new Seguimiento(
                    reporte,
                    gestor,
                    estadoAnterior,
                    EstadoReporte.ADMITIDO,
                    "VALIDACION_ADMITIDA",
                    nota + " [Prioridad asignada: " + reporte.getPrioridad() + "]",
                    null
            );
            seguimientoRepository.save(seguimiento);
        }

        return mapearReporteResponse(reporteRepository.save(reporte));
    }

    // ================== MODIFICAR PRIORIDAD (GESTOR) ==================
    @Transactional
    public ReporteResponse cambiarPrioridad(Long id, String emailGestor, CambiarPrioridadRequest request) {
        Reporte reporte = buscarPorId(id);
        Usuario gestor = usuarioRepository.findByEmail(emailGestor)
                .orElseThrow(() -> new ResourceNotFoundException("Gestor no encontrado: " + emailGestor));

        PrioridadReporte prioridadAnterior = reporte.getPrioridad();
        reporte.setPrioridad(request.getPrioridad());

        // Regla 2 y Regla 5: Registrar cambio de prioridad en seguimiento
        String obs = (request.getObservaciones() != null && !request.getObservaciones().isBlank())
                ? request.getObservaciones()
                : "Prioridad modificada por el gestor de " + prioridadAnterior + " a " + request.getPrioridad();

        Seguimiento seg = new Seguimiento(
                reporte,
                gestor,
                reporte.getEstado(),
                reporte.getEstado(),
                "CAMBIO_PRIORIDAD",
                obs,
                null
        );
        seguimientoRepository.save(seg);

        return mapearReporteResponse(reporteRepository.save(reporte));
    }

    // ================== ACTUALIZAR ESTADO DEL REPORTE (GESTOR / ADMIN) ==================
    @Transactional
    public ReporteResponse cambiarEstado(Long id, String emailUsuario, CambiarEstadoRequest request) {
        Reporte reporte = buscarPorId(id);
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        EstadoReporte estadoAnterior = reporte.getEstado();
        EstadoReporte nuevoEstado = request.getEstado();

        // Regla 4: Validar flujo de estados
        validarTransicionEstado(estadoAnterior, nuevoEstado);

        reporte.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoReporte.CERRADO && reporte.getFechaCierre() == null) {
            reporte.setFechaCierre(LocalDateTime.now());
        }

        String obs = (request.getObservaciones() != null && !request.getObservaciones().isBlank())
                ? request.getObservaciones()
                : "Cambio de estado de " + estadoAnterior + " a " + nuevoEstado;

        Seguimiento seg = new Seguimiento(
                reporte,
                usuario,
                estadoAnterior,
                nuevoEstado,
                "CAMBIO_ESTADO",
                obs,
                null
        );
        seguimientoRepository.save(seg);

        return mapearReporteResponse(reporteRepository.save(reporte));
    }

    // ================== RF08: CIERRE DE REPORTES (OPERADOR / GESTOR / ADMIN) ==================
    @Transactional
    public ReporteResponse cerrarReporte(Long id, String emailUsuario, CerrarReporteRequest request) {
        Reporte reporte = buscarPorId(id);
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        // Regla 3: Un reporte NO PUEDE cerrarse si no registra previamente al menos una acción de atención por parte del operador
        long accionesOperador = seguimientoRepository.countByReporte_IdAndUsuario_Rol_Nombre(id, RolNombre.ROLE_OPERADOR);
        if (accionesOperador == 0) {
            throw new BusinessRuleException("Regla de Negocio: Un reporte no puede cerrarse si no registra previamente al menos una acción de atención por parte del operador.");
        }

        if (request.getEvidenciaCierreUrl() == null || request.getEvidenciaCierreUrl().isBlank()) {
            throw new BusinessRuleException("Regla de Negocio: Debe adjuntar evidencia visual de la solución para proceder al cierre del reporte.");
        }

        EstadoReporte estadoAnterior = reporte.getEstado();
        reporte.setEstado(EstadoReporte.CERRADO);
        reporte.setEvidenciaCierreUrl(request.getEvidenciaCierreUrl());
        reporte.setFechaCierre(LocalDateTime.now());

        String observaciones = (request.getObservaciones() != null && !request.getObservaciones().isBlank())
                ? request.getObservaciones()
                : "Incidencia solventada y cerrada satisfactoriamente con evidencia de soporte.";

        Seguimiento segCierre = new Seguimiento(
                reporte,
                usuario,
                estadoAnterior,
                EstadoReporte.CERRADO,
                "CIERRE_REPORTE",
                observaciones,
                request.getEvidenciaCierreUrl()
        );
        seguimientoRepository.save(segCierre);

        // Desactivar asignación activa
        asignacionRepository.findByReporte_IdAndActivaTrue(id).ifPresent(asig -> {
            asig.setActiva(false);
            asignacionRepository.save(asig);
        });

        return mapearReporteResponse(reporteRepository.save(reporte));
    }

    private void validarTransicionEstado(EstadoReporte actual, EstadoReporte nuevo) {
        if (actual == nuevo) return;

        if (actual == EstadoReporte.CERRADO) {
            throw new BusinessRuleException("Un reporte cerrado no puede cambiar de estado directamente.");
        }
        if (actual == EstadoReporte.RECHAZADO && nuevo != EstadoReporte.PENDIENTE && nuevo != EstadoReporte.EN_REVISION) {
            throw new BusinessRuleException("Un reporte rechazado solo puede ser reactivado a revisión.");
        }
    }

    public Reporte buscarPorId(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + id));
    }

    public ReporteResponse mapearReporteResponse(Reporte reporte) {
        ReporteResponse response = new ReporteResponse(reporte);
        asignacionRepository.findByReporte_IdAndActivaTrue(reporte.getId())
                .ifPresent(asig -> {
                    if (asig.getOperador() != null) {
                        response.setOperadorAsignadoNombre(asig.getOperador().getNombre());
                    }
                });
        return response;
    }
}
