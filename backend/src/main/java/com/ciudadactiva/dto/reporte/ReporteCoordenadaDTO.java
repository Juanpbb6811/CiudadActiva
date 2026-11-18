package com.ciudadactiva.dto.reporte;

import com.ciudadactiva.entity.EstadoReporte;
import com.ciudadactiva.entity.PrioridadReporte;

/**
 * Proyección ultraligera para cálculos geográficos, semáforos y clusters.
 * Evita transferir datos binarios pesados (fotos) desde la base de datos a Render.
 */
public interface ReporteCoordenadaDTO {
    Long getId();
    Double getLatitud();
    Double getLongitud();
    Long getCategoriaId();
    String getCategoriaNombre();
    EstadoReporte getEstado();
    PrioridadReporte getPrioridad();
}
