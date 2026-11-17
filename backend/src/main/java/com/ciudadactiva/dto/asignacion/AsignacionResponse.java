package com.ciudadactiva.dto.asignacion;

import com.ciudadactiva.entity.Asignacion;
import java.time.LocalDateTime;

public class AsignacionResponse {

    private Long id;
    private Long reporteId;
    private String reporteTitulo;
    private String reporteCodigo;
    private String reporteEstado;
    private String reportePrioridad;
    private String categoriaNombre;
    private String reporteDireccion;
    private Long operadorId;
    private String operadorNombre;
    private String operadorEmail;
    private Long gestorId;
    private String gestorNombre;
    private String notas;
    private Boolean activa;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public AsignacionResponse() {
    }

    public AsignacionResponse(Asignacion asignacion) {
        if (asignacion != null) {
            this.id = asignacion.getId();
            if (asignacion.getReporte() != null) {
                this.reporteId = asignacion.getReporte().getId();
                this.reporteTitulo = asignacion.getReporte().getTitulo();
                this.reporteCodigo = asignacion.getReporte().getCodigoSeguimiento();
                this.reporteEstado = asignacion.getReporte().getEstado() != null ? asignacion.getReporte().getEstado().name() : null;
                this.reportePrioridad = asignacion.getReporte().getPrioridad() != null ? asignacion.getReporte().getPrioridad().name() : null;
                this.reporteDireccion = asignacion.getReporte().getDireccion();
                if (asignacion.getReporte().getCategoria() != null) {
                    this.categoriaNombre = asignacion.getReporte().getCategoria().getNombre();
                }
            }
            if (asignacion.getOperador() != null) {
                this.operadorId = asignacion.getOperador().getId();
                this.operadorNombre = asignacion.getOperador().getNombre();
                this.operadorEmail = asignacion.getOperador().getEmail();
            }
            if (asignacion.getGestor() != null) {
                this.gestorId = asignacion.getGestor().getId();
                this.gestorNombre = asignacion.getGestor().getNombre();
            }
            this.notas = asignacion.getNotas();
            this.activa = asignacion.getActiva();
            this.creadoEn = asignacion.getCreadoEn();
            this.actualizadoEn = asignacion.getActualizadoEn();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReporteId() {
        return reporteId;
    }

    public void setReporteId(Long reporteId) {
        this.reporteId = reporteId;
    }

    public String getReporteTitulo() {
        return reporteTitulo;
    }

    public void setReporteTitulo(String reporteTitulo) {
        this.reporteTitulo = reporteTitulo;
    }

    public Long getOperadorId() {
        return operadorId;
    }

    public void setOperadorId(Long operadorId) {
        this.operadorId = operadorId;
    }

    public String getOperadorNombre() {
        return operadorNombre;
    }

    public void setOperadorNombre(String operadorNombre) {
        this.operadorNombre = operadorNombre;
    }

    public String getOperadorEmail() {
        return operadorEmail;
    }

    public void setOperadorEmail(String operadorEmail) {
        this.operadorEmail = operadorEmail;
    }

    public Long getGestorId() {
        return gestorId;
    }

    public void setGestorId(Long gestorId) {
        this.gestorId = gestorId;
    }

    public String getGestorNombre() {
        return gestorNombre;
    }

    public void setGestorNombre(String gestorNombre) {
        this.gestorNombre = gestorNombre;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public String getReporteCodigo() { return reporteCodigo; }
    public void setReporteCodigo(String reporteCodigo) { this.reporteCodigo = reporteCodigo; }

    public String getReporteEstado() { return reporteEstado; }
    public void setReporteEstado(String reporteEstado) { this.reporteEstado = reporteEstado; }

    public String getReportePrioridad() { return reportePrioridad; }
    public void setReportePrioridad(String reportePrioridad) { this.reportePrioridad = reportePrioridad; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public String getReporteDireccion() { return reporteDireccion; }
    public void setReporteDireccion(String reporteDireccion) { this.reporteDireccion = reporteDireccion; }
}
