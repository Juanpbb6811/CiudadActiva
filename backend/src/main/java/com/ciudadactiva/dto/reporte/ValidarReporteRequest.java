package com.ciudadactiva.dto.reporte;

import com.ciudadactiva.entity.PrioridadReporte;
import jakarta.validation.constraints.NotNull;

public class ValidarReporteRequest {

    @NotNull(message = "Debe indicar si el reporte es admitido (true) o rechazado (false)")
    private Boolean admitido;

    // Obligatoria si admitido == false
    private String justificacion;

    // Prioridad oficial opcional asignada por el gestor al admitir
    private PrioridadReporte prioridad;

    public ValidarReporteRequest() {
    }

    public ValidarReporteRequest(Boolean admitido, String justificacion, PrioridadReporte prioridad) {
        this.admitido = admitido;
        this.justificacion = justificacion;
        this.prioridad = prioridad;
    }

    public Boolean getAdmitido() {
        return admitido;
    }

    public void setAdmitido(Boolean admitido) {
        this.admitido = admitido;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public PrioridadReporte getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadReporte prioridad) {
        this.prioridad = prioridad;
    }
}
