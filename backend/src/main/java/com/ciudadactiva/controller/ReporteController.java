package com.ciudadactiva.controller;

import com.ciudadactiva.dto.asignacion.AsignacionCreateRequest;
import com.ciudadactiva.dto.asignacion.AsignacionResponse;
import com.ciudadactiva.dto.comentario.ComentarioRequest;
import com.ciudadactiva.dto.comentario.ComentarioResponse;
import com.ciudadactiva.dto.common.ApiResponse;
import com.ciudadactiva.dto.reporte.*;
import com.ciudadactiva.dto.seguimiento.SeguimientoCreateRequest;
import com.ciudadactiva.dto.seguimiento.SeguimientoResponse;
import com.ciudadactiva.entity.EstadoReporte;
import com.ciudadactiva.entity.PrioridadReporte;
import com.ciudadactiva.service.AsignacionService;
import com.ciudadactiva.service.ComentarioService;
import com.ciudadactiva.service.ReporteService;
import com.ciudadactiva.service.SeguimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes de Incidencias", description = "Endpoints centrales para ciclo de vida de incidencias, fotos tomadas con celular, validación, asignación y cierre")
@SecurityRequirement(name = "BearerAuth")
public class ReporteController {

    private final ReporteService reporteService;
    private final AsignacionService asignacionService;
    private final SeguimientoService seguimientoService;
    private final ComentarioService comentarioService;

    public ReporteController(
            ReporteService reporteService,
            AsignacionService asignacionService,
            SeguimientoService seguimientoService,
            ComentarioService comentarioService
    ) {
        this.reporteService = reporteService;
        this.asignacionService = asignacionService;
        this.seguimientoService = seguimientoService;
        this.comentarioService = comentarioService;
    }

    // ================== RF03: CONSULTAR Y FILTRAR REPORTES ==================
    @GetMapping
    @Operation(summary = "Consultar Reportes con Filtros", description = "Filtra reportes por categoría, estado, prioridad y/o ciudadano.")
    public ResponseEntity<ApiResponse<List<ReporteResponse>>> consultarReportes(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) PrioridadReporte prioridad,
            @RequestParam(required = false) Long ciudadanoId
    ) {
        List<ReporteResponse> reportes = reporteService.filtrarReportes(categoriaId, estado, prioridad, ciudadanoId);
        return ResponseEntity.ok(ApiResponse.ok("Reportes obtenidos exitosamente", reportes));
    }

    // ================== CASO DE USO CIUDADANO: MIS REPORTES ==================
    @GetMapping("/mis-reportes")
    @Operation(summary = "Consultar Reportes Propios", description = "Retorna el historial de reportes creados por el ciudadano autenticado.")
    public ResponseEntity<ApiResponse<List<ReporteResponse>>> obtenerMisReportes(Principal principal) {
        List<ReporteResponse> misReportes = reporteService.obtenerMisReportes(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Mis reportes obtenidos con éxito", misReportes));
    }

    // ================== DETALLE DE REPORTE ==================
    @GetMapping("/{id}")
    @Operation(summary = "Consultar Detalle de Reporte", description = "Retorna información detallada del reporte, incluyendo línea de tiempo y comentarios.")
    public ResponseEntity<ApiResponse<ReporteDetalleResponse>> consultarDetalle(@PathVariable Long id) {
        ReporteDetalleResponse detalle = reporteService.obtenerDetalle(id);
        return ResponseEntity.ok(ApiResponse.ok("Detalle del reporte obtenido", detalle));
    }

    // ================== RF02: REGISTRAR INCIDENCIA (JSON / BASE64) ==================
    @PostMapping
    @Operation(summary = "Registrar Incidencia", description = "Crea un nuevo reporte con categoría, descripción, geolocalización simulada y fotografía opcional (URL o Base64).")
    public ResponseEntity<ApiResponse<ReporteResponse>> crearReporte(
            Principal principal,
            @Valid @RequestBody ReporteCreateRequest request
    ) {
        ReporteResponse response = reporteService.crearReporte(principal.getName(), request);
        return new ResponseEntity<>(ApiResponse.ok("Reporte registrado exitosamente", response), HttpStatus.CREATED);
    }

    // ================== RF02: REGISTRAR INCIDENCIA CON FOTO DIRECTA (MULTIPART / CÁMARA MÓVIL) ==================
    @PostMapping(value = "/con-foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar Incidencia con Fotografía de Cámara", description = "Permite registrar un reporte y subir directamente la foto tomada desde la cámara del celular en una sola petición multipart.")
    public ResponseEntity<ApiResponse<ReporteResponse>> crearReporteConFoto(
            Principal principal,
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("categoriaId") Long categoriaId,
            @RequestParam(value = "latitud", required = false) Double latitud,
            @RequestParam(value = "longitud", required = false) Double longitud,
            @RequestParam(value = "direccion", required = false) String direccion,
            @RequestParam(value = "foto", required = false) MultipartFile foto
    ) throws IOException {
        ReporteResponse response = reporteService.crearReporteConFoto(
                principal.getName(),
                titulo,
                descripcion,
                categoriaId,
                latitud,
                longitud,
                direccion,
                foto
        );
        return new ResponseEntity<>(ApiResponse.ok("Reporte con fotografía de cámara registrado exitosamente", response), HttpStatus.CREATED);
    }

    // ================== SUBIR FOTO DESDE CELULAR A REPORTE EXISTENTE ==================
    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir Fotografía de Cámara a Reporte Existente", description = "Permite al ciudadano o personal subir/actualizar la fotografía tomada con su teléfono para una incidencia existente.")
    public ResponseEntity<ApiResponse<ReporteResponse>> subirFotoReporte(
            @PathVariable Long id,
            Principal principal,
            @RequestParam("foto") MultipartFile foto
    ) throws IOException {
        ReporteResponse response = reporteService.subirFotoReporte(id, principal.getName(), foto);
        return ResponseEntity.ok(ApiResponse.ok("Fotografía tomada con el celular guardada en la base de datos con éxito", response));
    }

    // ================== DESCARGAR / VISUALIZAR FOTO DE LA INCIDENCIA ==================
    @GetMapping("/{id}/foto")
    @Operation(summary = "Obtener Imagen del Reporte", description = "Descarga o visualiza la fotografía original de la incidencia almacenada en la base de datos.")
    public ResponseEntity<byte[]> obtenerFotoReporte(@PathVariable Long id) {
        FotoData foto = reporteService.obtenerFotoReporte(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, foto.contentType())
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(foto.bytes());
    }

    // ================== SUBIR FOTO DE SOLUCIÓN EN CAMPO (OPERADOR) ==================
    @PostMapping(value = "/{id}/foto-solucion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OPERADOR', 'GESTOR', 'ADMIN')")
    @Operation(summary = "Subir Foto de Solución en Campo (Operador)", description = "Permite al operador subir la foto tomada tras realizar el trabajo en campo.")
    public ResponseEntity<ApiResponse<ReporteResponse>> subirFotoSolucion(
            @PathVariable Long id,
            Principal principal,
            @RequestParam("foto") MultipartFile foto,
            @RequestParam(value = "observaciones", required = false) String observaciones
    ) throws IOException {
        ReporteResponse response = reporteService.subirFotoSolucion(id, principal.getName(), foto, observaciones);
        return ResponseEntity.ok(ApiResponse.ok("Fotografía de solución almacenada en la base de datos con éxito", response));
    }

    // ================== DESCARGAR / VISUALIZAR FOTO DE SOLUCIÓN ==================
    @GetMapping("/{id}/foto-solucion")
    @Operation(summary = "Obtener Foto de Solución", description = "Descarga o visualiza la fotografía de evidencia de la solución en campo almacenada en la base de datos.")
    public ResponseEntity<byte[]> obtenerFotoSolucion(@PathVariable Long id) {
        FotoData foto = reporteService.obtenerFotoSolucion(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, foto.contentType())
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(foto.bytes());
    }

    // ================== RF04: VALIDAR O RECHAZAR REPORTE (GESTOR / ADMIN) ==================
    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Validar o Rechazar Reporte (Gestor)", description = "Admite o rechaza un reporte. Si se rechaza, la justificación es obligatoria.")
    public ResponseEntity<ApiResponse<ReporteResponse>> validarReporte(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody ValidarReporteRequest request
    ) {
        ReporteResponse response = reporteService.validarReporte(id, principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Validación del reporte procesada con éxito", response));
    }

    // ================== MODIFICAR PRIORIDAD (GESTOR / ADMIN) ==================
    @PatchMapping("/{id}/prioridad")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Modificar Prioridad del Reporte (Gestor)", description = "Permite al gestor cambiar la prioridad oficial (BAJA, MEDIA, ALTA, CRITICA).")
    public ResponseEntity<ApiResponse<ReporteResponse>> cambiarPrioridad(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody CambiarPrioridadRequest request
    ) {
        ReporteResponse response = reporteService.cambiarPrioridad(id, principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Prioridad actualizada exitosamente", response));
    }

    // ================== CAMBIAR ESTADO DIRECTO (GESTOR / ADMIN) ==================
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Actualizar Estado del Reporte (Gestor)", description = "Permite transicionar manualmente el estado conforme al flujo.")
    public ResponseEntity<ApiResponse<ReporteResponse>> cambiarEstado(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody CambiarEstadoRequest request
    ) {
        ReporteResponse response = reporteService.cambiarEstado(id, principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Estado del reporte actualizado con éxito", response));
    }

    // ================== RF05: ASIGNAR RESPONSABLE (GESTOR / ADMIN) ==================
    @PostMapping("/{id}/asignaciones")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Asignar Operador Responsable (Gestor)", description = "Asigna el reporte admitido a un operador para iniciar labores en campo.")
    public ResponseEntity<ApiResponse<AsignacionResponse>> asignarOperador(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody AsignacionCreateRequest request
    ) {
        AsignacionResponse response = asignacionService.asignarOperador(id, principal.getName(), request);
        return new ResponseEntity<>(ApiResponse.ok("Operador asignado exitosamente al reporte", response), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/asignaciones")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Eliminar / Reasignar Asignación (Gestor)", description = "Cancela la asignación activa actual del reporte.")
    public ResponseEntity<ApiResponse<Void>> cancelarAsignacion(
            @PathVariable Long id,
            Principal principal
    ) {
        asignacionService.cancelarAsignacion(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Asignación cancelada con éxito", null));
    }

    // ================== RF06: REGISTRAR AVANCE OPERATIVO (OPERADOR / GESTOR / ADMIN) ==================
    @PostMapping("/{id}/seguimientos")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GESTOR', 'ADMIN')")
    @Operation(summary = "Registrar Avance Operativo (Operador)", description = "Permite al operador registrar avances, observaciones y evidencias de trabajo.")
    public ResponseEntity<ApiResponse<SeguimientoResponse>> registrarAvance(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody SeguimientoCreateRequest request
    ) {
        SeguimientoResponse response = seguimientoService.registrarAvance(id, principal.getName(), request);
        return new ResponseEntity<>(ApiResponse.ok("Avance operativo registrado con éxito", response), HttpStatus.CREATED);
    }

    // ================== RF07: LÍNEA DE TIEMPO / SEGUIMIENTO ==================
    @GetMapping("/{id}/seguimiento")
    @Operation(summary = "Consultar Línea de Tiempo", description = "Retorna el historial completo e inmutable de eventos y avances del reporte.")
    public ResponseEntity<ApiResponse<List<SeguimientoResponse>>> consultarSeguimiento(@PathVariable Long id) {
        List<SeguimientoResponse> lineaTiempo = seguimientoService.obtenerLineaDeTiempo(id);
        return ResponseEntity.ok(ApiResponse.ok("Línea de tiempo obtenida exitosamente", lineaTiempo));
    }

    // ================== RF08: CERRAR INCIDENTE ==================
    @PatchMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GESTOR', 'ADMIN')")
    @Operation(summary = "Cerrar Reporte con Evidencia de Solución", description = "Cierra la incidencia. Requiere evidencia de solución y valida que el operador haya realizado atención previa.")
    public ResponseEntity<ApiResponse<ReporteResponse>> cerrarReporte(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody CerrarReporteRequest request
    ) {
        ReporteResponse response = reporteService.cerrarReporte(id, principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Incidente cerrado exitosamente", response));
    }

    // ================== CASO DE USO: COMENTARIOS ==================
    @PostMapping("/{id}/comentarios")
    @Operation(summary = "Realizar Comentario en Reporte", description = "Permite a ciudadanos u operadores comentar sobre el estado o solución del reporte.")
    public ResponseEntity<ApiResponse<ComentarioResponse>> agregarComentario(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody ComentarioRequest request
    ) {
        ComentarioResponse response = comentarioService.agregarComentario(id, principal.getName(), request);
        return new ResponseEntity<>(ApiResponse.ok("Comentario agregado con éxito", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/comentarios")
    @Operation(summary = "Listar Comentarios del Reporte", description = "Lista todos los comentarios de un reporte.")
    public ResponseEntity<ApiResponse<List<ComentarioResponse>>> listarComentarios(@PathVariable Long id) {
        List<ComentarioResponse> comentarios = comentarioService.listarPorReporte(id);
        return ResponseEntity.ok(ApiResponse.ok("Comentarios obtenidos", comentarios));
    }
}
