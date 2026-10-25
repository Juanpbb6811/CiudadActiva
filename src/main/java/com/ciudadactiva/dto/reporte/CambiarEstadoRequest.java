package com.ciudadactiva.dto.reporte;

import com.ciudadactiva.entity.EstadoReporte;
import jakarta.validation.constraints.NotNull;

public class CambiarEstadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoReporte estado;

    private String observaciones;

    public CambiarEstadoRequest() {
    }

    public CambiarEstadoRequest(EstadoReporte estado, String observaciones) {
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    public void setEstado(EstadoReporte estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
