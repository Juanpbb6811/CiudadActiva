package com.ciudadactiva.service;

import com.ciudadactiva.dto.estadistica.EstadisticasResponse;
import com.ciudadactiva.entity.Categoria;
import com.ciudadactiva.entity.EstadoReporte;
import com.ciudadactiva.entity.PrioridadReporte;
import com.ciudadactiva.entity.Reporte;
import com.ciudadactiva.repository.CategoriaRepository;
import com.ciudadactiva.repository.ReporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
        List<Reporte> todosLosReportes = reporteRepository.findAll();
        List<Categoria> todasLasCategorias = categoriaRepository.findAll();

        EstadisticasResponse response = new EstadisticasResponse();
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
        for (Categoria c : todasLasCategorias) {
            porCategoria.put(c.getNombre(), 0L);
        }
        for (Reporte r : todosLosReportes) {
            if (r.getCategoria() != null) {
                String catNombre = r.getCategoria().getNombre();
                porCategoria.put(catNombre, porCategoria.getOrDefault(catNombre, 0L) + 1);
            }
        }
        response.setReportesPorCategoria(porCategoria);

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

        return response;
    }
}
