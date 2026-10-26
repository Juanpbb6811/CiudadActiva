package com.ciudadactiva.dto.reporte;

import com.ciudadactiva.entity.PrioridadReporte;
import jakarta.validation.constraints.NotNull;

public class CambiarPrioridadRequest {

    @NotNull(message = "La prioridad es obligatoria")
    private PrioridadReporte prioridad;

    private String observaciones;

    public CambiarPrioridadRequest() {
    }

    public CambiarPrioridadRequest(PrioridadReporte prioridad, String observaciones) {
        this.prioridad = prioridad;
        this.observaciones = observaciones;
    }

    public PrioridadReporte getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadReporte prioridad) {
        this.prioridad = prioridad;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
