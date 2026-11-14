package com.ciudadactiva.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_imagenes")
public class ReporteImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id", nullable = false)
    private Reporte reporte;

    @Lob
    @Column(name = "datos_binarios", nullable = false, columnDefinition = "BYTEA")
    private byte[] datosBinarios;

    @Column(name = "tipo_mime", nullable = false, length = 50)
    private String tipoMime;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "peso_bytes", nullable = false)
    private Integer pesoBytes;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    public ReporteImagen() {
    }

    public ReporteImagen(Reporte reporte, byte[] datosBinarios, String tipoMime, String nombreArchivo) {
        this.reporte = reporte;
        this.datosBinarios = datosBinarios;
        this.tipoMime = tipoMime != null ? tipoMime : "image/jpeg";
        this.nombreArchivo = nombreArchivo;
        this.pesoBytes = datosBinarios != null ? datosBinarios.length : 0;
        this.creadoEn = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.creadoEn == null) {
            this.creadoEn = LocalDateTime.now();
        }
        if (this.pesoBytes == null && this.datosBinarios != null) {
            this.pesoBytes = this.datosBinarios.length;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reporte getReporte() {
        return reporte;
    }

    public void setReporte(Reporte reporte) {
        this.reporte = reporte;
    }

    public byte[] getDatosBinarios() {
        return datosBinarios;
    }

    public void setDatosBinarios(byte[] datosBinarios) {
        this.datosBinarios = datosBinarios;
        this.pesoBytes = datosBinarios != null ? datosBinarios.length : 0;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Integer getPesoBytes() {
        return pesoBytes;
    }

    public void setPesoBytes(Integer pesoBytes) {
        this.pesoBytes = pesoBytes;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
