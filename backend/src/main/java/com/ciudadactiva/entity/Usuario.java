package com.ciudadactiva.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 30)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "verificado")
    private Boolean verificado = false;

    @Column(name = "codigo_verificacion", length = 10)
    private String codigoVerificacion;

    @Column(name = "codigo_verificacion_expira")
    private LocalDateTime codigoVerificacionExpira;

    @Column(name = "codigo_recuperacion", length = 10)
    private String codigoRecuperacion;

    @Column(name = "codigo_recuperacion_expira")
    private LocalDateTime codigoRecuperacionExpira;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public Usuario() {
    }

    public Usuario(String nombre, String email, String password, String telefono, String direccion, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.direccion = direccion;
        this.rol = rol;
        this.activo = true;
        this.verificado = false;
    }

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.verificado == null) {
            this.verificado = false;
        }
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Boolean getVerificado() {
        return verificado;
    }

    public void setVerificado(Boolean verificado) {
        this.verificado = verificado;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public LocalDateTime getCodigoVerificacionExpira() {
        return codigoVerificacionExpira;
    }

    public void setCodigoVerificacionExpira(LocalDateTime codigoVerificacionExpira) {
        this.codigoVerificacionExpira = codigoVerificacionExpira;
    }

    public String getCodigoRecuperacion() {
        return codigoRecuperacion;
    }

    public void setCodigoRecuperacion(String codigoRecuperacion) {
        this.codigoRecuperacion = codigoRecuperacion;
    }

    public LocalDateTime getCodigoRecuperacionExpira() {
        return codigoRecuperacionExpira;
    }

    public void setCodigoRecuperacionExpira(LocalDateTime codigoRecuperacionExpira) {
        this.codigoRecuperacionExpira = codigoRecuperacionExpira;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
