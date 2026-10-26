package com.ciudadactiva.dto.reporte;

import jakarta.validation.constraints.NotBlank;

public class CerrarReporteRequest {

    @NotBlank(message = "La evidencia de la solución (fotografía o enlace) es obligatoria para el cierre")
    private String evidenciaCierreUrl;

    private String observaciones;

    public CerrarReporteRequest() {
    }

    public CerrarReporteRequest(String evidenciaCierreUrl, String observaciones) {
        this.evidenciaCierreUrl = evidenciaCierreUrl;
        this.observaciones = observaciones;
    }

    public String getEvidenciaCierreUrl() {
        return evidenciaCierreUrl;
    }

    public void setEvidenciaCierreUrl(String evidenciaCierreUrl) {
        this.evidenciaCierreUrl = evidenciaCierreUrl;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
