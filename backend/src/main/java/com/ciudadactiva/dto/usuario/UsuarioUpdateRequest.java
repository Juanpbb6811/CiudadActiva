package com.ciudadactiva.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public class UsuarioUpdateRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String telefono;

    private String direccion;

    private Boolean activo;

    public UsuarioUpdateRequest() {
    }

    public UsuarioUpdateRequest(String nombre, String telefono, String direccion, Boolean activo) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
