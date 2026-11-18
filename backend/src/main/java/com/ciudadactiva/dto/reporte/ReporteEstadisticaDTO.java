package com.ciudadactiva.dto.reporte;

import com.ciudadactiva.entity.EstadoReporte;
import com.ciudadactiva.entity.PrioridadReporte;
import java.time.LocalDateTime;

/**
 * Proyección liviana para generación de estadísticas y tableros.
 * Elimina la carga de fotos binarias en memoria y base de datos.
 */
public interface ReporteEstadisticaDTO {
    Long getId();
    LocalDateTime getCreadoEn();
    LocalDateTime getFechaCierre();
    EstadoReporte getEstado();
    PrioridadReporte getPrioridad();
    Long getCategoriaId();
    String getCategoriaNombre();
}
