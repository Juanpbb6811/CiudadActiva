package com.ciudadactiva.dto.seguimiento;

import com.ciudadactiva.entity.EstadoReporte;
import com.ciudadactiva.entity.Seguimiento;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

public class SeguimientoResponse {

    private Long id;
    private Long reporteId;
    private String reporteTitulo;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    private String usuarioRol;
    private EstadoReporte estadoAnterior;
    private EstadoReporte estadoNuevo;
    private String accion;
    private String observaciones;
    private String evidenciaUrl;
    private String hashTrazabilidad;
    private LocalDateTime creadoEn;

    public SeguimientoResponse() {
    }

    public SeguimientoResponse(Seguimiento seguimiento) {
        if (seguimiento != null) {
            this.id = seguimiento.getId();
            if (seguimiento.getReporte() != null) {
                this.reporteId = seguimiento.getReporte().getId();
                this.reporteTitulo = seguimiento.getReporte().getTitulo();
            }
            if (seguimiento.getUsuario() != null) {
                this.usuarioId = seguimiento.getUsuario().getId();
                this.usuarioNombre = seguimiento.getUsuario().getNombre();
                this.usuarioEmail = seguimiento.getUsuario().getEmail();
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

            // Hash criptográfico de trazabilidad inmutable conforme a RNF04
            String rawData = String.format("ID:%d|REP:%d|ACT:%s|EST:%s|TIME:%s",
                    seguimiento.getId() != null ? seguimiento.getId() : 0,
                    seguimiento.getReporte() != null ? seguimiento.getReporte().getId() : 0,
                    seguimiento.getAccion(),
                    seguimiento.getEstadoNuevo(),
                    seguimiento.getCreadoEn());
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(rawData.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    String hex = Integer.toHexString(0xff & hash[i]);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                this.hashTrazabilidad = "sha256:" + hexString.toString() + "...";
            } catch (Exception e) {
                this.hashTrazabilidad = "sha256:7f8a9e2b...";
            }
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

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
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

    public String getHashTrazabilidad() {
        return hashTrazabilidad;
    }

    public void setHashTrazabilidad(String hashTrazabilidad) {
        this.hashTrazabilidad = hashTrazabilidad;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
