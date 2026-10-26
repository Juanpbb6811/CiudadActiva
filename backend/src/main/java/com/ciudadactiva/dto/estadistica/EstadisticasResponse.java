package com.ciudadactiva.dto.estadistica;

import com.ciudadactiva.dto.common.SemaforoDTO;
import java.util.HashMap;
import java.util.Map;

public class EstadisticasResponse {

    private long totalReportes;
    private Map<String, Long> reportesPorEstado = new HashMap<>();
    private Map<String, Long> reportesPorCategoria = new HashMap<>();
    private Map<String, Long> reportesPorPrioridad = new HashMap<>();
    private double tiempoPromedioAtencionHoras;
    private long reportesAtendidosOCerrados;
    private long reportesPendientes;
    private SemaforoDTO semaforoGlobal;
    private Map<String, SemaforoDTO> semaforoPorCategoria = new HashMap<>();

    public EstadisticasResponse() {
    }

    public long getTotalReportes() {
        return totalReportes;
    }

    public void setTotalReportes(long totalReportes) {
        this.totalReportes = totalReportes;
    }

    public Map<String, Long> getReportesPorEstado() {
        return reportesPorEstado;
    }

    public void setReportesPorEstado(Map<String, Long> reportesPorEstado) {
        this.reportesPorEstado = reportesPorEstado;
    }

    public Map<String, Long> getReportesPorCategoria() {
        return reportesPorCategoria;
    }

    public void setReportesPorCategoria(Map<String, Long> reportesPorCategoria) {
        this.reportesPorCategoria = reportesPorCategoria;
    }

    public Map<String, Long> getReportesPorPrioridad() {
        return reportesPorPrioridad;
    }

    public void setReportesPorPrioridad(Map<String, Long> reportesPorPrioridad) {
        this.reportesPorPrioridad = reportesPorPrioridad;
    }

    public double getTiempoPromedioAtencionHoras() {
        return tiempoPromedioAtencionHoras;
    }

    public void setTiempoPromedioAtencionHoras(double tiempoPromedioAtencionHoras) {
        this.tiempoPromedioAtencionHoras = tiempoPromedioAtencionHoras;
    }

    public long getReportesAtendidosOCerrados() {
        return reportesAtendidosOCerrados;
    }

    public void setReportesAtendidosOCerrados(long reportesAtendidosOCerrados) {
        this.reportesAtendidosOCerrados = reportesAtendidosOCerrados;
    }

    public long getReportesPendientes() {
        return reportesPendientes;
    }

    public void setReportesPendientes(long reportesPendientes) {
        this.reportesPendientes = reportesPendientes;
    }

    public SemaforoDTO getSemaforoGlobal() {
        return semaforoGlobal;
    }

    public void setSemaforoGlobal(SemaforoDTO semaforoGlobal) {
        this.semaforoGlobal = semaforoGlobal;
    }

    public Map<String, SemaforoDTO> getSemaforoPorCategoria() {
        return semaforoPorCategoria;
    }

    public void setSemaforoPorCategoria(Map<String, SemaforoDTO> semaforoPorCategoria) {
        this.semaforoPorCategoria = semaforoPorCategoria;
    }
}
