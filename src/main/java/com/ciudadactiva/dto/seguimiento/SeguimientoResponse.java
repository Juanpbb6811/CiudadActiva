package com.ciudadactiva.dto.seguimiento;

import com.ciudadactiva.entity.EstadoReporte;
import com.ciudadactiva.entity.Seguimiento;

import java.time.LocalDateTime;

public class SeguimientoResponse {

    private Long id;
    private Long reporteId;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioRol;
    private EstadoReporte estadoAnterior;
    private EstadoReporte estadoNuevo;
    private String accion;
    private String observaciones;
    private String evidenciaUrl;
    private LocalDateTime creadoEn;

    public SeguimientoResponse() {
    }

    public SeguimientoResponse(Seguimiento seguimiento) {
        if (seguimiento != null) {
            this.id = seguimiento.getId();
            if (seguimiento.getReporte() != null) {
                this.reporteId = seguimiento.getReporte().getId();
            }
            if (seguimiento.getUsuario() != null) {
                this.usuarioId = seguimiento.getUsuario().getId();
                this.usuarioNombre = seguimiento.getUsuario().getNombre();
                if (seguimiento.getUsuario().getRol() != null) {
                    this.usuarioRol = seguimiento.getUsuario().getRol().getNombre().name();
                }
            }
            this.estadoAnterior = seguimiento.getEstadoAnterior();
            this.estadoNuevo = seguimiento.getEstadoNuevo();
            this.accion = seguimiento.getAccion();
            this.observaciones = seguimiento.getObservaciones();
            this.evidenciaUrl = seguimiento.getEvidenciaUrl();
            this.creadoEn = seguimiento.getCreadoEn();
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsuarioRol() {
        return usuarioRol;
    }

    public void setUsuarioRol(String usuarioRol) {
        this.usuarioRol = usuarioRol;
    }

    public EstadoReporte getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoReporte estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoReporte getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoReporte estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEvidenciaUrl() {
        return evidenciaUrl;
    }

    public void setEvidenciaUrl(String evidenciaUrl) {
        this.evidenciaUrl = evidenciaUrl;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
