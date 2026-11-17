package com.ciudadactiva.service;

import com.ciudadactiva.dto.common.SemaforoDTO;
import com.ciudadactiva.dto.estadistica.EstadisticasResponse;
import com.ciudadactiva.entity.Categoria;
import com.ciudadactiva.entity.EstadoReporte;
import com.ciudadactiva.entity.PrioridadReporte;
import com.ciudadactiva.entity.Reporte;
import com.ciudadactiva.exception.BadRequestException;
import com.ciudadactiva.repository.CategoriaRepository;
import com.ciudadactiva.repository.ReporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstadisticaService {

    private final ReporteRepository reporteRepository;
    private final CategoriaRepository categoriaRepository;

    public EstadisticaService(ReporteRepository reporteRepository, CategoriaRepository categoriaRepository) {
        this.reporteRepository = reporteRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // ================== RF09: INDICADORES ESTADÍSTICOS ==================
    @Transactional(readOnly = true)
    public EstadisticasResponse obtenerEstadisticas() {
        return obtenerEstadisticas(null, null);
    }

    /**
     * RF09 / CP10: indicadores filtrados por rango de fechas de creación (ambos extremos inclusivos).
     * Si fechaInicio > fechaFin se lanza 400 (rango ilógico).
     */
    @Transactional(readOnly = true)
    public EstadisticasResponse obtenerEstadisticas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("Rango de fechas inválido: la fecha de inicio (" + fechaInicio
                    + ") no puede ser posterior a la fecha de fin (" + fechaFin + ").");
        }
        final LocalDateTime desde = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        final LocalDateTime hastaExcl = fechaFin != null ? fechaFin.plusDays(1).atStartOfDay() : null;

        List<Reporte> todosLosReportes = reporteRepository.findAll().stream()
                .filter(r -> r.getCreadoEn() != null)
                .filter(r -> desde == null || !r.getCreadoEn().isBefore(desde))
                .filter(r -> hastaExcl == null || r.getCreadoEn().isBefore(hastaExcl))
                .collect(Collectors.toList());
        List<Categoria> todasLasCategorias = categoriaRepository.findAll();

        EstadisticasResponse response = new EstadisticasResponse();
        response.setFechaInicio(fechaInicio);
        response.setFechaFin(fechaFin);
        response.setTotalReportes(todosLosReportes.size());

        // Conteo por Estado
        Map<String, Long> porEstado = new HashMap<>();
        for (EstadoReporte estado : EstadoReporte.values()) {
            porEstado.put(estado.name(), 0L);
        }
        for (Reporte r : todosLosReportes) {
            porEstado.put(r.getEstado().name(), porEstado.getOrDefault(r.getEstado().name(), 0L) + 1);
        }
        response.setReportesPorEstado(porEstado);

        // Conteo por Categoría
        Map<String, Long> porCategoria = new HashMap<>();
        Map<String, SemaforoDTO> semaforoPorCategoria = new HashMap<>();
        for (Categoria c : todasLasCategorias) {
            porCategoria.put(c.getNombre(), 0L);
        }
        for (Reporte r : todosLosReportes) {
            if (r.getCategoria() != null) {
                String catNombre = r.getCategoria().getNombre();
                porCategoria.put(catNombre, porCategoria.getOrDefault(catNombre, 0L) + 1);
            }
        }
        for (Categoria c : todasLasCategorias) {
            int cantidad = porCategoria.getOrDefault(c.getNombre(), 0L).intValue();
            semaforoPorCategoria.put(c.getNombre(), SemaforoDTO.calcular(cantidad));
        }
        response.setReportesPorCategoria(porCategoria);
        response.setSemaforoPorCategoria(semaforoPorCategoria);

        // Conteo por Prioridad
        Map<String, Long> porPrioridad = new HashMap<>();
        for (PrioridadReporte p : PrioridadReporte.values()) {
            porPrioridad.put(p.name(), 0L);
        }
        for (Reporte r : todosLosReportes) {
            if (r.getPrioridad() != null) {
                porPrioridad.put(r.getPrioridad().name(), porPrioridad.getOrDefault(r.getPrioridad().name(), 0L) + 1);
            }
        }
        response.setReportesPorPrioridad(porPrioridad);

        // Conteo de cerrados / atendidos y pendientes
        long cerrados = porEstado.getOrDefault(EstadoReporte.CERRADO.name(), 0L)
                + porEstado.getOrDefault(EstadoReporte.ATENDIDO.name(), 0L);
        long pendientes = porEstado.getOrDefault(EstadoReporte.PENDIENTE.name(), 0L);
        response.setReportesAtendidosOCerrados(cerrados);
        response.setReportesPendientes(pendientes);

        long rechazados = porEstado.getOrDefault(EstadoReporte.RECHAZADO.name(), 0L);
        response.setReportesRechazados(rechazados);
        long total = response.getTotalReportes();
        response.setPorcentajeAtendidas(total > 0 ? Math.round(cerrados * 10000.0 / total) / 100.0 : 0.0);
        response.setPorcentajeRechazadas(total > 0 ? Math.round(rechazados * 10000.0 / total) / 100.0 : 0.0);

        // Semáforo Global basado en reportes pendientes / activos o totales
        response.setSemaforoGlobal(SemaforoDTO.calcular((int) response.getTotalReportes()));

        // Tiempo promedio de atención en horas
        double totalHoras = 0;
        long totalCerradosConFecha = 0;

        for (Reporte r : todosLosReportes) {
            if (r.getFechaCierre() != null && r.getCreadoEn() != null) {
                Duration duracion = Duration.between(r.getCreadoEn(), r.getFechaCierre());
                totalHoras += duracion.toMinutes() / 60.0;
                totalCerradosConFecha++;
            }
        }

        double promedio = totalCerradosConFecha > 0 ? (totalHoras / totalCerradosConFecha) : 0.0;
        response.setTiempoPromedioAtencionHoras(Math.round(promedio * 100.0) / 100.0);
        response.setReportesCerradosConTiempo(totalCerradosConFecha);

        return response;
    }

    /**
     * Calcula dinámicamente el estado del semáforo para cualquier cantidad dada (1 a 10 escala de color).
     */
    public SemaforoDTO calcularSemaforo(int cantidad) {
        return SemaforoDTO.calcular(cantidad);
    }
}
