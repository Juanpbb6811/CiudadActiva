package com.ciudadactiva.dto.usuario;

import com.ciudadactiva.entity.Usuario;
import java.time.LocalDateTime;

public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String tipoDocumento;
    private String numeroDocumento;
    private String email;
    private String telefono;
    private String direccion;
    private String fotoUrl;
    private String googleId;
    private Boolean activo;
    private String rol;
    private Long laboresActivas;
    private Boolean verificado;
    private LocalDateTime creadoEn;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Usuario usuario) {
        if (usuario != null) {
            this.id = usuario.getId();
            this.nombre = usuario.getNombre();
            this.primerNombre = usuario.getPrimerNombre();
            this.segundoNombre = usuario.getSegundoNombre();
            this.primerApellido = usuario.getPrimerApellido();
            this.segundoApellido = usuario.getSegundoApellido();
            this.tipoDocumento = usuario.getTipoDocumento() != null ? usuario.getTipoDocumento().name() : "CC";
            this.numeroDocumento = usuario.getNumeroDocumento();
            this.email = usuario.getEmail();
            this.telefono = usuario.getTelefono();
            this.direccion = usuario.getDireccion();
            this.fotoUrl = usuario.getFotoUrl();
            this.googleId = usuario.getGoogleId();
            this.activo = usuario.getActivo();
            this.verificado = usuario.getVerificado();
            if (usuario.getRol() != null) {
                this.rol = usuario.getRol().getNombre().name();
            }
            this.creadoEn = usuario.getCreadoEn();
        }
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

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Long getLaboresActivas() {
        return laboresActivas;
    }

    public void setLaboresActivas(Long laboresActivas) {
        this.laboresActivas = laboresActivas;
    }

    public Boolean getVerificado() {
        return verificado;
    }

    public void setVerificado(Boolean verificado) {
        this.verificado = verificado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
