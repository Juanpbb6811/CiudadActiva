package com.ciudadactiva.dto.comentario;

import com.ciudadactiva.entity.Comentario;
import java.time.LocalDateTime;

public class ComentarioResponse {

    private Long id;
    private Long reporteId;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioRol;
    private String contenido;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public ComentarioResponse() {
    }

    public ComentarioResponse(Comentario comentario) {
        if (comentario != null) {
            this.id = comentario.getId();
            if (comentario.getReporte() != null) {
                this.reporteId = comentario.getReporte().getId();
            }
            if (comentario.getUsuario() != null) {
                this.usuarioId = comentario.getUsuario().getId();
                this.usuarioNombre = comentario.getUsuario().getNombre();
                if (comentario.getUsuario().getRol() != null) {
                    this.usuarioRol = comentario.getUsuario().getRol().getNombre().name();
                }
            }
            this.contenido = comentario.getContenido();
            this.creadoEn = comentario.getCreadoEn();
            this.actualizadoEn = comentario.getActualizadoEn();
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

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
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
}
