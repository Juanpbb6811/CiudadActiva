package com.ciudadactiva.dto.reporte;

import com.ciudadactiva.entity.EstadoReporte;
import com.ciudadactiva.entity.PrioridadReporte;
import com.ciudadactiva.entity.Reporte;

import java.time.LocalDateTime;

public class ReporteResponse {

    private Long id;
    private String codigoSeguimiento;
    private String titulo;
    private String descripcion;
    private Long categoriaId;
    private String categoriaNombre;
    private String categoriaIcono;
    private Long ciudadanoId;
    private String ciudadanoNombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private String imagenUrl;
    private Boolean tieneFoto;
    private String fotoTipo;
    private EstadoReporte estado;
    private PrioridadReporte prioridad;
    private String justificacionRechazo;
    private String evidenciaCierreUrl;
    private Boolean tieneFotoSolucion;
    private String fotoSolucionTipo;
    private String operadorAsignadoNombre;
    private Integer reportesCercanos = 1;
    private String semaforoColorHex = "#4CAF50";
    private String semaforoNivel = "BAJO";
    private String semaforoDescripcion = "1 reporte: Nivel bajo, situación bajo control";
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private LocalDateTime fechaCierre;

    public ReporteResponse() {
    }

    public ReporteResponse(Reporte reporte) {
        if (reporte != null) {
            this.id = reporte.getId();
            this.codigoSeguimiento = reporte.getCodigoSeguimiento();
            this.titulo = reporte.getTitulo();
            this.descripcion = reporte.getDescripcion();
            if (reporte.getCategoria() != null) {
                this.categoriaId = reporte.getCategoria().getId();
                this.categoriaNombre = reporte.getCategoria().getNombre();
                this.categoriaIcono = reporte.getCategoria().getIcono();
            }
            if (reporte.getCiudadano() != null) {
                this.ciudadanoId = reporte.getCiudadano().getId();
                this.ciudadanoNombre = reporte.getCiudadano().getNombre();
            }
            this.direccion = reporte.getDireccion();
            this.latitud = reporte.getLatitud();
            this.longitud = reporte.getLongitud();
            this.imagenUrl = reporte.getImagenUrl();
            this.tieneFoto = (reporte.getFotoBytes() != null && reporte.getFotoBytes().length > 0);
            this.fotoTipo = reporte.getFotoTipo();
            this.estado = reporte.getEstado();
            this.prioridad = reporte.getPrioridad();
            this.justificacionRechazo = reporte.getJustificacionRechazo();
            this.evidenciaCierreUrl = reporte.getEvidenciaCierreUrl();
            this.tieneFotoSolucion = (reporte.getFotoSolucionBytes() != null && reporte.getFotoSolucionBytes().length > 0);
            this.fotoSolucionTipo = reporte.getFotoSolucionTipo();
            this.creadoEn = reporte.getCreadoEn();
            this.actualizadoEn = reporte.getActualizadoEn();
            this.fechaCierre = reporte.getFechaCierre();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoSeguimiento() {
        return codigoSeguimiento;
    }

    public void setCodigoSeguimiento(String codigoSeguimiento) {
        this.codigoSeguimiento = codigoSeguimiento;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getCategoriaIcono() {
        return categoriaIcono;
    }

    public void setCategoriaIcono(String categoriaIcono) {
        this.categoriaIcono = categoriaIcono;
    }

    public Long getCiudadanoId() {
        return ciudadanoId;
    }

    public void setCiudadanoId(Long ciudadanoId) {
        this.ciudadanoId = ciudadanoId;
    }

    public String getCiudadanoNombre() {
        return ciudadanoNombre;
    }

    public void setCiudadanoNombre(String ciudadanoNombre) {
        this.ciudadanoNombre = ciudadanoNombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Boolean getTieneFoto() {
        return tieneFoto;
    }

    public void setTieneFoto(Boolean tieneFoto) {
        this.tieneFoto = tieneFoto;
    }

    public String getFotoTipo() {
        return fotoTipo;
    }

    public void setFotoTipo(String fotoTipo) {
        this.fotoTipo = fotoTipo;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    public void setEstado(EstadoReporte estado) {
        this.estado = estado;
    }

    public PrioridadReporte getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadReporte prioridad) {
        this.prioridad = prioridad;
    }

    public String getJustificacionRechazo() {
        return justificacionRechazo;
    }

    public void setJustificacionRechazo(String justificacionRechazo) {
        this.justificacionRechazo = justificacionRechazo;
    }

    public String getEvidenciaCierreUrl() {
        return evidenciaCierreUrl;
    }

    public void setEvidenciaCierreUrl(String evidenciaCierreUrl) {
        this.evidenciaCierreUrl = evidenciaCierreUrl;
    }

    public Boolean getTieneFotoSolucion() {
        return tieneFotoSolucion;
    }

    public void setTieneFotoSolucion(Boolean tieneFotoSolucion) {
        this.tieneFotoSolucion = tieneFotoSolucion;
    }

    public String getFotoSolucionTipo() {
        return fotoSolucionTipo;
    }

    public void setFotoSolucionTipo(String fotoSolucionTipo) {
        this.fotoSolucionTipo = fotoSolucionTipo;
    }

    public String getOperadorAsignadoNombre() {
        return operadorAsignadoNombre;
    }

    public void setOperadorAsignadoNombre(String operadorAsignadoNombre) {
        this.operadorAsignadoNombre = operadorAsignadoNombre;
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

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Integer getReportesCercanos() {
        return reportesCercanos;
    }

    public void setReportesCercanos(Integer reportesCercanos) {
        this.reportesCercanos = reportesCercanos;
    }

    public String getSemaforoColorHex() {
        return semaforoColorHex;
    }

    public void setSemaforoColorHex(String semaforoColorHex) {
        this.semaforoColorHex = semaforoColorHex;
    }

    public String getSemaforoNivel() {
        return semaforoNivel;
    }

    public void setSemaforoNivel(String semaforoNivel) {
        this.semaforoNivel = semaforoNivel;
    }

    public String getSemaforoDescripcion() {
        return semaforoDescripcion;
    }

    public void setSemaforoDescripcion(String semaforoDescripcion) {
        this.semaforoDescripcion = semaforoDescripcion;
    }
}
