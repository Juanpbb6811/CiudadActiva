package com.ciudadactiva.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public class UsuarioUpdateRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String email;

    private String telefono;

    private String direccion;

    private String fotoUrl;

    private Boolean activo;

    public UsuarioUpdateRequest() {
    }

    public UsuarioUpdateRequest(String nombre, String email, String telefono, String direccion, String fotoUrl, Boolean activo) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fotoUrl = fotoUrl;
        this.activo = activo;
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
}
