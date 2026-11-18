package com.ciudadactiva.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(name = "codigo_seguimiento", unique = true, length = 30)
    private String codigoSeguimiento;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    private Double latitud;

    private Double longitud;

    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String barrio;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PrioridadReporte prioridad = PrioridadReporte.MEDIA;

    @Column(name = "justificacion_rechazo", columnDefinition = "TEXT")
    private String justificacionRechazo;

    @Column(name = "evidencia_cierre_url", columnDefinition = "TEXT")
    private String evidenciaCierreUrl;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "foto_bytes")
    private byte[] fotoBytes;

    @Column(name = "foto_tipo", length = 50)
    private String fotoTipo;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "foto_solucion_bytes")
    private byte[] fotoSolucionBytes;

    @Column(name = "foto_solucion_tipo", length = 50)
    private String fotoSolucionTipo;

    @org.hibernate.annotations.Formula("foto_bytes IS NOT NULL")
    private Boolean tieneFoto;

    @org.hibernate.annotations.Formula("foto_solucion_bytes IS NOT NULL")
    private Boolean tieneFotoSolucion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ciudadano_id", nullable = false)
    private Usuario ciudadano;

    @Column(name = "analisis_ia_descripcion", columnDefinition = "TEXT")
    private String analisisIaDescripcion;

    @Column(name = "analisis_ia_categoria_sugerida", length = 100)
    private String analisisIaCategoriaSugerida;

    @Column(name = "analisis_ia_prioridad_sugerida", length = 20)
    private String analisisIaPrioridadSugerida;

    @Column(name = "analisis_ia_nivel_urgencia")
    private Integer analisisIaNivelUrgencia;

    @Column(name = "analisis_ia_fecha")
    private LocalDateTime analisisIaFecha;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    public Reporte() {
    }

    public Reporte(String titulo, String descripcion, Double latitud, Double longitud, String direccion, String imagenUrl, Categoria categoria, Usuario ciudadano) {
        this(titulo, descripcion, latitud, longitud, direccion, "Bogotá D.C.", "Cundinamarca", "Chapinero", imagenUrl, categoria, ciudadano);
    }

    public Reporte(String titulo, String descripcion, Double latitud, Double longitud, String direccion, String ciudad, String departamento, String barrio, String imagenUrl, Categoria categoria, Usuario ciudadano) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.departamento = departamento;
        this.barrio = barrio;
        this.imagenUrl = imagenUrl;
        this.categoria = categoria;
        this.ciudadano = ciudadano;
        this.estado = EstadoReporte.PENDIENTE;
        this.prioridad = PrioridadReporte.MEDIA;
    }

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoReporte.PENDIENTE;
        }
        if (this.prioridad == null) {
            this.prioridad = PrioridadReporte.MEDIA;
        }
        if (this.codigoSeguimiento == null || this.codigoSeguimiento.isBlank()) {
            String randomPart = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
            this.codigoSeguimiento = "RAD-" + java.time.LocalDate.now().getYear() + "-" + randomPart;
        }
    }

    public String getCodigoSeguimiento() {
        if (codigoSeguimiento == null && id != null) {
            return "RAD-" + (creadoEn != null ? creadoEn.getYear() : 2026) + "-" + String.format("%05d", id);
        }
        return codigoSeguimiento;
    }

    public void setCodigoSeguimiento(String codigoSeguimiento) {
        this.codigoSeguimiento = codigoSeguimiento;
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Usuario getCiudadano() {
        return ciudadano;
    }

    public void setCiudadano(Usuario ciudadano) {
        this.ciudadano = ciudadano;
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

    public byte[] getFotoBytes() {
        return fotoBytes;
    }

    public void setFotoBytes(byte[] fotoBytes) {
        this.fotoBytes = fotoBytes;
    }

    public Boolean getTieneFoto() {
        if (tieneFoto != null) return tieneFoto;
        return fotoBytes != null && fotoBytes.length > 0;
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

    public byte[] getFotoSolucionBytes() {
        return fotoSolucionBytes;
    }

    public void setFotoSolucionBytes(byte[] fotoSolucionBytes) {
        this.fotoSolucionBytes = fotoSolucionBytes;
    }

    public Boolean getTieneFotoSolucion() {
        if (tieneFotoSolucion != null) return tieneFotoSolucion;
        return fotoSolucionBytes != null && fotoSolucionBytes.length > 0;
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

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getAnalisisIaDescripcion() {
        return analisisIaDescripcion;
    }

    public void setAnalisisIaDescripcion(String analisisIaDescripcion) {
        this.analisisIaDescripcion = analisisIaDescripcion;
    }

    public String getAnalisisIaCategoriaSugerida() {
        return analisisIaCategoriaSugerida;
    }

    public void setAnalisisIaCategoriaSugerida(String analisisIaCategoriaSugerida) {
        this.analisisIaCategoriaSugerida = analisisIaCategoriaSugerida;
    }

    public String getAnalisisIaPrioridadSugerida() {
        return analisisIaPrioridadSugerida;
    }

    public void setAnalisisIaPrioridadSugerida(String analisisIaPrioridadSugerida) {
        this.analisisIaPrioridadSugerida = analisisIaPrioridadSugerida;
    }

    public Integer getAnalisisIaNivelUrgencia() {
        return analisisIaNivelUrgencia;
    }

    public void setAnalisisIaNivelUrgencia(Integer analisisIaNivelUrgencia) {
        this.analisisIaNivelUrgencia = analisisIaNivelUrgencia;
    }

    public LocalDateTime getAnalisisIaFecha() {
        return analisisIaFecha;
    }

    public void setAnalisisIaFecha(LocalDateTime analisisIaFecha) {
        this.analisisIaFecha = analisisIaFecha;
    }
}
