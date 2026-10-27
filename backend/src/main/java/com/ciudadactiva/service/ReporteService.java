package com.ciudadactiva.service;

import com.ciudadactiva.dto.asignacion.AsignacionResponse;
import com.ciudadactiva.dto.comentario.ComentarioResponse;
import com.ciudadactiva.dto.common.SemaforoDTO;
import com.ciudadactiva.dto.reporte.*;
import com.ciudadactiva.dto.seguimiento.SeguimientoResponse;
import com.ciudadactiva.entity.*;
import com.ciudadactiva.exception.BadRequestException;
import com.ciudadactiva.exception.BusinessRuleException;
import com.ciudadactiva.exception.ResourceNotFoundException;
import com.ciudadactiva.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
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

    // ================== RF02: REGISTRO DE REPORTES (JSON / BASE64) ==================
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
        PrioridadReporte prioridadCalculada = calcularPrioridadPorProximidad(reporte.getLatitud(), reporte.getLongitud(), categoria.getId(), null);
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setPrioridad(prioridadCalculada);

        // Procesar foto si viene en formato Base64 directamente desde la cámara móvil
        procesarFotoBase64(reporte, request.getImagenUrl());

        Reporte guardado = reporteRepository.save(reporte);
        escalarPrioridadCluster(guardado.getLatitud(), guardado.getLongitud(), categoria.getId(), guardado.getId(), prioridadCalculada);

        // Si se procesaron bytes en BD, fijar URL al endpoint local de descarga
        if (guardado.getFotoBytes() != null && guardado.getFotoBytes().length > 0) {
            guardado.setImagenUrl("/api/reportes/" + guardado.getId() + "/foto");
            guardado = reporteRepository.save(guardado);
        }

        // Regla 5: Trazabilidad inmutable - Registro inicial en Seguimiento
        Seguimiento primerSeguimiento = new Seguimiento(
                guardado,
                ciudadano,
                null,
                EstadoReporte.PENDIENTE,
                "CREACION_REPORTE",
                "Incidencia registrada por el ciudadano con dirección: " + (request.getDireccion() != null ? request.getDireccion() : "Sin dirección específica"),
                guardado.getImagenUrl()
        );
        seguimientoRepository.save(primerSeguimiento);

        return mapearReporteResponse(guardado);
    }

    // ================== RF02: REGISTRO DE REPORTES CON FOTO DIRECTA (MULTIPART / CÁMARA MÓVIL) ==================
    @Transactional
    public ReporteResponse crearReporteConFoto(
            String emailCiudadano,
            String titulo,
            String descripcion,
            Long categoriaId,
            Double latitud,
            Double longitud,
            String direccion,
            MultipartFile foto
    ) throws IOException {
        Usuario ciudadano = usuarioRepository.findByEmail(emailCiudadano)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario ciudadano no encontrado: " + emailCiudadano));

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + categoriaId));

        Reporte reporte = new Reporte(
                titulo,
                descripcion,
                latitud,
                longitud,
                direccion,
                null,
                categoria,
                ciudadano
        );
        PrioridadReporte prioridadCalculada = calcularPrioridadPorProximidad(reporte.getLatitud(), reporte.getLongitud(), categoria.getId(), null);
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setPrioridad(prioridadCalculada);

        if (foto != null && !foto.isEmpty()) {
            validarTipoImagen(foto.getContentType());
            reporte.setFotoBytes(foto.getBytes());
            reporte.setFotoTipo(foto.getContentType() != null ? foto.getContentType() : "image/jpeg");
        }

        Reporte guardado = reporteRepository.save(reporte);
        escalarPrioridadCluster(guardado.getLatitud(), guardado.getLongitud(), categoria.getId(), guardado.getId(), prioridadCalculada);

        if (guardado.getFotoBytes() != null) {
            guardado.setImagenUrl("/api/reportes/" + guardado.getId() + "/foto");
            guardado = reporteRepository.save(guardado);
        }

        Seguimiento primerSeguimiento = new Seguimiento(
                guardado,
                ciudadano,
                null,
                EstadoReporte.PENDIENTE,
                "CREACION_REPORTE",
                "Incidencia registrada con fotografía tomada directamente desde la cámara del móvil",
                guardado.getImagenUrl()
        );
        seguimientoRepository.save(primerSeguimiento);

        return mapearReporteResponse(guardado);
    }

    // ================== SUBIDA DE FOTO A REPORTE EXISTENTE (CÁMARA MÓVIL) ==================
    @Transactional
    public ReporteResponse subirFotoReporte(Long reporteId, String emailUsuario, MultipartFile foto) throws IOException {
        Reporte reporte = buscarPorId(reporteId);
        validarPermisoModificacion(reporte, emailUsuario);

        if (foto == null || foto.isEmpty()) {
            throw new BusinessRuleException("El archivo de fotografía no puede estar vacío");
        }

        validarTipoImagen(foto.getContentType());
        reporte.setFotoBytes(foto.getBytes());
        reporte.setFotoTipo(foto.getContentType() != null ? foto.getContentType() : "image/jpeg");
        reporte.setImagenUrl("/api/reportes/" + reporte.getId() + "/foto");

        Reporte guardado = reporteRepository.save(reporte);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElse(null);
        Seguimiento seguimiento = new Seguimiento(
                guardado,
                usuario,
                guardado.getEstado(),
                guardado.getEstado(),
                "FOTO_ADJUNTADA",
                "Fotografía de evidencia tomada desde el dispositivo móvil adjuntada al reporte",
                guardado.getImagenUrl()
        );
        seguimientoRepository.save(seguimiento);

        return mapearReporteResponse(guardado);
    }

    // ================== SUBIDA DE FOTO DE SOLUCIÓN (OPERADOR EN CAMPO) ==================
    @Transactional
    public ReporteResponse subirFotoSolucion(Long reporteId, String emailUsuario, MultipartFile foto, String observaciones) throws IOException {
        Reporte reporte = buscarPorId(reporteId);
        Usuario operador = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Operador no encontrado: " + emailUsuario));

        if (foto == null || foto.isEmpty()) {
            throw new BusinessRuleException("Debe adjuntar la fotografía tomada como evidencia de la solución en campo");
        }

        validarTipoImagen(foto.getContentType());
        reporte.setFotoSolucionBytes(foto.getBytes());
        reporte.setFotoSolucionTipo(foto.getContentType() != null ? foto.getContentType() : "image/jpeg");
        reporte.setEvidenciaCierreUrl("/api/reportes/" + reporte.getId() + "/foto-solucion");

        Reporte guardado = reporteRepository.save(reporte);

        Seguimiento seg = new Seguimiento(
                guardado,
                operador,
                guardado.getEstado(),
                guardado.getEstado(),
                "EVIDENCIA_SOLUCION_ADJUNTADA",
                (observaciones != null && !observaciones.isBlank()) ? observaciones : "Evidencia visual de solución en campo adjuntada",
                guardado.getEvidenciaCierreUrl()
        );
        seguimientoRepository.save(seg);

        return mapearReporteResponse(guardado);
    }

    // ================== CONSULTAR FOTOS BINARIAS ==================
    @Transactional(readOnly = true)
    public FotoData obtenerFotoReporte(Long reporteId) {
        Reporte reporte = buscarPorId(reporteId);
        if (reporte.getFotoBytes() == null || reporte.getFotoBytes().length == 0) {
            throw new ResourceNotFoundException("El reporte ID " + reporteId + " no tiene una fotografía almacenada");
        }
        String contentType = reporte.getFotoTipo() != null ? reporte.getFotoTipo() : "image/jpeg";
        return new FotoData(reporte.getFotoBytes(), contentType);
    }

    @Transactional(readOnly = true)
    public FotoData obtenerFotoSolucion(Long reporteId) {
        Reporte reporte = buscarPorId(reporteId);
        if (reporte.getFotoSolucionBytes() == null || reporte.getFotoSolucionBytes().length == 0) {
            throw new ResourceNotFoundException("El reporte ID " + reporteId + " no tiene fotografía de solución almacenada");
        }
        String contentType = reporte.getFotoSolucionTipo() != null ? reporte.getFotoSolucionTipo() : "image/jpeg";
        return new FotoData(reporte.getFotoSolucionBytes(), contentType);
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

        // Si la evidencia viene en base64, procesarla para guardarla en BD
        if (request.getEvidenciaCierreUrl().startsWith("data:image/") || request.getEvidenciaCierreUrl().length() > 500) {
            procesarFotoSolucionBase64(reporte, request.getEvidenciaCierreUrl());
            reporte.setEvidenciaCierreUrl("/api/reportes/" + reporte.getId() + "/foto-solucion");
        } else {
            reporte.setEvidenciaCierreUrl(request.getEvidenciaCierreUrl());
        }

        EstadoReporte estadoAnterior = reporte.getEstado();
        reporte.setEstado(EstadoReporte.CERRADO);
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
                reporte.getEvidenciaCierreUrl()
        );
        seguimientoRepository.save(segCierre);

        // Desactivar asignación activa
        asignacionRepository.findByReporte_IdAndActivaTrue(id).ifPresent(asig -> {
            asig.setActiva(false);
            asignacionRepository.save(asig);
        });

        return mapearReporteResponse(reporteRepository.save(reporte));
    }

    private void validarPermisoModificacion(Reporte reporte, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        boolean esDuenio = reporte.getCiudadano() != null && reporte.getCiudadano().getEmail().equalsIgnoreCase(emailUsuario);
        boolean esStaff = usuario.getRol().getNombre() == RolNombre.ROLE_GESTOR || usuario.getRol().getNombre() == RolNombre.ROLE_ADMIN;

        if (!esDuenio && !esStaff) {
            throw new BusinessRuleException("No tiene permisos para modificar la fotografía de este reporte.");
        }
    }

    private void validarTipoImagen(String contentType) {
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
            throw new BusinessRuleException("Formato no válido. Debe ser un archivo de imagen (JPEG, PNG, WEBP, etc.)");
        }
    }

    private void procesarFotoBase64(Reporte reporte, String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return;
        }
        try {
            String tipo = "image/jpeg";
            String rawBase64 = base64Data;
            if (base64Data.startsWith("data:")) {
                int commaIndex = base64Data.indexOf(",");
                if (commaIndex != -1) {
                    String prefix = base64Data.substring(5, commaIndex);
                    if (prefix.contains(";")) {
                        tipo = prefix.split(";")[0];
                    }
                    rawBase64 = base64Data.substring(commaIndex + 1);
                }
            }
            byte[] bytes = Base64.getDecoder().decode(rawBase64.trim());
            if (bytes.length > 0) {
                reporte.setFotoBytes(bytes);
                reporte.setFotoTipo(tipo);
            }
        } catch (IllegalArgumentException e) {
            // Si no es Base64 válido, se preserva como URL de texto normal
        }
    }

    private void procesarFotoSolucionBase64(Reporte reporte, String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return;
        }
        try {
            String tipo = "image/jpeg";
            String rawBase64 = base64Data;
            if (base64Data.startsWith("data:")) {
                int commaIndex = base64Data.indexOf(",");
                if (commaIndex != -1) {
                    String prefix = base64Data.substring(5, commaIndex);
                    if (prefix.contains(";")) {
                        tipo = prefix.split(";")[0];
                    }
                    rawBase64 = base64Data.substring(commaIndex + 1);
                }
            }
            byte[] bytes = Base64.getDecoder().decode(rawBase64.trim());
            if (bytes.length > 0) {
                reporte.setFotoSolucionBytes(bytes);
                reporte.setFotoSolucionTipo(tipo);
            }
        } catch (IllegalArgumentException e) {
            // URL ordinaria
        }
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

    /**
     * Calcula la distancia en metros entre dos coordenadas geográficas usando la fórmula de Haversine.
     */
    public static double calcularDistanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radio de la Tierra en metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Calcula dinámicamente la prioridad según el número de reportes acumulados
     * de la MISMA categoría en un radio de proximidad geográfica de 100 metros:
     * - Categorías de alto riesgo físico (Alumbrado, Señalización, Alcantarillado):
     *     * 1 reporte: MEDIA (Atención preventiva por riesgo físico)
     *     * 2-3 reportes: ALTA (Intervención urgente en sector)
     *     * 4+ reportes: CRITICA (Emergencia comunitaria en infraestructura de riesgo)
     * - Categorías de malla vial y pavimento:
     *     * 1 reporte: BAJA (Daño puntual)
     *     * 2-4 reportes: MEDIA (Deterioro moderado acumulado)
     *     * 5-7 reportes: ALTA (Afectación vial notable)
     *     * 8+ reportes: CRITICA (Tramo intransitable)
     * - Categorías de aseo, basuras y parques:
     *     * 1-2 reportes: BAJA
     *     * 3-5 reportes: MEDIA
     *     * 6-8 reportes: ALTA
     *     * 9+ reportes: CRITICA
     */
    public PrioridadReporte calcularPrioridadPorProximidad(Double latitud, Double longitud, Long categoriaId, Long excluirReporteId) {
        if (latitud == null || longitud == null) {
            return PrioridadReporte.MEDIA;
        }

        List<Reporte> activos = reporteRepository.findAll().stream()
                .filter(r -> (excluirReporteId == null || !r.getId().equals(excluirReporteId))
                        && r.getLatitud() != null && r.getLongitud() != null
                        && r.getCategoria() != null
                        && (categoriaId == null || r.getCategoria().getId().equals(categoriaId))
                        && r.getEstado() != EstadoReporte.RECHAZADO
                        && r.getEstado() != EstadoReporte.CERRADO
                        && calcularDistanciaMetros(latitud, longitud, r.getLatitud(), r.getLongitud()) <= 100.0)
                .collect(Collectors.toList());

        int totalCluster = activos.size() + 1; // Contando el nuevo reporte

        Categoria cat = categoriaId != null ? categoriaRepository.findById(categoriaId).orElse(null) : null;
        String catNombre = cat != null ? cat.getNombre().toLowerCase() : "";

        // 1. Categorías de Alto Riesgo Inmediato: Alumbrado / Riesgo Eléctrico / Señalización / Alcantarillado
        if (catNombre.contains("alumbrado") || catNombre.contains("luz") || catNombre.contains("poste") ||
            catNombre.contains("señal") || catNombre.contains("semaforo") || catNombre.contains("semáforo") ||
            catNombre.contains("alcantarill") || catNombre.contains("acueducto")) {

            if (totalCluster <= 1) {
                return PrioridadReporte.MEDIA;
            } else if (totalCluster <= 3) {
                return PrioridadReporte.ALTA;
            } else {
                return PrioridadReporte.CRITICA;
            }
        }

        // 2. Malla Vial y Pavimento (Baches, hundimientos)
        if (catNombre.contains("vía") || catNombre.contains("via") || catNombre.contains("pavimento") || catNombre.contains("calzada")) {
            if (totalCluster <= 1) {
                return PrioridadReporte.BAJA;
            } else if (totalCluster <= 4) {
                return PrioridadReporte.MEDIA;
            } else if (totalCluster <= 7) {
                return PrioridadReporte.ALTA;
            } else {
                return PrioridadReporte.CRITICA;
            }
        }

        // 3. Basuras, Aseo y Parques
        if (totalCluster <= 2) {
            return PrioridadReporte.BAJA;
        } else if (totalCluster <= 5) {
            return PrioridadReporte.MEDIA;
        } else if (totalCluster <= 8) {
            return PrioridadReporte.ALTA;
        } else {
            return PrioridadReporte.CRITICA;
        }
    }

    private void escalarPrioridadCluster(Double latitud, Double longitud, Long categoriaId, Long reporteId, PrioridadReporte nuevaPrioridad) {
        if (latitud == null || longitud == null) {
            return;
        }
        reporteRepository.findAll().stream()
                .filter(r -> !r.getId().equals(reporteId)
                        && r.getLatitud() != null && r.getLongitud() != null
                        && r.getCategoria() != null
                        && (categoriaId == null || r.getCategoria().getId().equals(categoriaId))
                        && r.getEstado() != EstadoReporte.RECHAZADO
                        && r.getEstado() != EstadoReporte.CERRADO
                        && calcularDistanciaMetros(latitud, longitud, r.getLatitud(), r.getLongitud()) <= 100.0)
                .forEach(cercano -> {
                    if (cercano.getPrioridad().ordinal() < nuevaPrioridad.ordinal()) {
                        cercano.setPrioridad(nuevaPrioridad);
                        reporteRepository.save(cercano);
                    }
                });
    }

    // ================== RETIRAR / ELIMINAR REPORTE PENDIENTE ==================
    @Transactional
    public void eliminarReportePendiente(Long reporteId, String userEmail) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + reporteId));

        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        boolean esAutor = reporte.getCiudadano() != null && reporte.getCiudadano().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRol() != null && "ROLE_ADMIN".equals(usuario.getRol().getNombre().name());

        if (!esAutor && !esAdmin) {
            throw new BadRequestException("No tienes permisos para retirar este reporte. Solo el ciudadano autor o un administrador pueden retirarlo.");
        }

        if (reporte.getEstado() != EstadoReporte.PENDIENTE) {
            throw new BadRequestException("Solo se pueden retirar o eliminar reportes que se encuentren en estado PENDIENTE.");
        }

        // 1. Eliminar comentarios asociados
        List<Comentario> comentarios = comentarioRepository.findByReporte_IdOrderByCreadoEnAsc(reporteId);
        if (!comentarios.isEmpty()) {
            comentarioRepository.deleteAll(comentarios);
        }

        // 2. Eliminar seguimientos asociados
        List<Seguimiento> seguimientos = seguimientoRepository.findByReporte_IdOrderByCreadoEnAsc(reporteId);
        if (!seguimientos.isEmpty()) {
            seguimientoRepository.deleteAll(seguimientos);
        }

        // 3. Eliminar asignaciones asociadas si existieran
        List<Asignacion> asignaciones = asignacionRepository.findByReporte_IdOrderByCreadoEnDesc(reporteId);
        if (!asignaciones.isEmpty()) {
            asignacionRepository.deleteAll(asignaciones);
        }

        // 4. Eliminar el reporte
        reporteRepository.delete(reporte);
    }

    public ReporteResponse mapearReporteResponse(Reporte reporte) {
        ReporteResponse response = new ReporteResponse(reporte);
        asignacionRepository.findByReporte_IdAndActivaTrue(reporte.getId())
                .ifPresent(asig -> {
                    if (asig.getOperador() != null) {
                        response.setOperadorAsignadoNombre(asig.getOperador().getNombre());
                    }
                });

        // Calcular conteo de reportes en la misma zona geográfica y semáforo dentro de la MISMA categoría
        if (reporte.getLatitud() != null && reporte.getLongitud() != null && reporte.getCategoria() != null) {
            Long catId = reporte.getCategoria().getId();
            String catNombre = reporte.getCategoria().getNombre();
            List<Reporte> cercanos = reporteRepository.findAll().stream()
                    .filter(r -> r.getLatitud() != null && r.getLongitud() != null
                            && r.getCategoria() != null && r.getCategoria().getId().equals(catId)
                            && r.getEstado() != EstadoReporte.RECHAZADO
                            && r.getEstado() != EstadoReporte.CERRADO
                            && calcularDistanciaMetros(reporte.getLatitud(), reporte.getLongitud(), r.getLatitud(), r.getLongitud()) <= 100.0)
                    .collect(Collectors.toList());
            int clusterCount = Math.max(1, cercanos.size());
            SemaforoDTO sem = SemaforoDTO.calcularPorCategoria(clusterCount, catNombre);
            response.setReportesCercanos(clusterCount);
            response.setSemaforoColorHex(sem.getColorHex());
            response.setSemaforoNivel(sem.getNivelAlerta());
            response.setSemaforoDescripcion(sem.getDescripcion());
        } else {
            response.setReportesCercanos(1);
            response.setSemaforoColorHex("#4CAF50");
            response.setSemaforoNivel("BAJO");
            response.setSemaforoDescripcion("1 reporte: Nivel bajo");
        }

        return response;
    }
}
