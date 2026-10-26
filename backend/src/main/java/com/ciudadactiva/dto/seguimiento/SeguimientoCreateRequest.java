package com.ciudadactiva.dto.seguimiento;

import com.ciudadactiva.entity.EstadoReporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SeguimientoCreateRequest {

    @NotBlank(message = "Las observaciones del avance son obligatorias")
    @Size(min = 5, message = "Las observaciones deben tener al menos 5 caracteres")
    private String observaciones;

    private String accion;

    private EstadoReporte nuevoEstado;

    private String evidenciaUrl;

    public SeguimientoCreateRequest() {
    }

    public SeguimientoCreateRequest(String observaciones, String accion, EstadoReporte nuevoEstado, String evidenciaUrl) {
        this.observaciones = observaciones;
        this.accion = accion;
        this.nuevoEstado = nuevoEstado;
        this.evidenciaUrl = evidenciaUrl;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public EstadoReporte getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(EstadoReporte nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getEvidenciaUrl() {
        return evidenciaUrl;
    }

    public void setEvidenciaUrl(String evidenciaUrl) {
        this.evidenciaUrl = evidenciaUrl;
    }
}
