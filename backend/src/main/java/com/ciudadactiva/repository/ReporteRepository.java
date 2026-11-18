package com.ciudadactiva.repository;

import com.ciudadactiva.dto.reporte.ReporteCoordenadaDTO;
import com.ciudadactiva.dto.reporte.ReporteEstadisticaDTO;
import com.ciudadactiva.entity.EstadoReporte;
import com.ciudadactiva.entity.PrioridadReporte;
import com.ciudadactiva.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    Optional<Reporte> findByCodigoSeguimiento(String codigoSeguimiento);

    List<Reporte> findByCiudadano_IdOrderByCreadoEnDesc(Long ciudadanoId);

    @Query("SELECT r FROM Reporte r WHERE " +
           "(:categoriaId IS NULL OR r.categoria.id = :categoriaId) AND " +
           "(:estado IS NULL OR r.estado = :estado) AND " +
           "(:prioridad IS NULL OR r.prioridad = :prioridad) AND " +
           "(:ciudadanoId IS NULL OR r.ciudadano.id = :ciudadanoId) " +
           "ORDER BY r.creadoEn DESC")
    List<Reporte> filtrarReportes(
            @Param("categoriaId") Long categoriaId,
            @Param("estado") EstadoReporte estado,
            @Param("prioridad") PrioridadReporte prioridad,
            @Param("ciudadanoId") Long ciudadanoId
    );

    @Query("SELECT r.id AS id, r.latitud AS latitud, r.longitud AS longitud, " +
           "r.categoria.id AS categoriaId, r.categoria.nombre AS categoriaNombre, " +
           "r.estado AS estado, r.prioridad AS prioridad " +
           "FROM Reporte r WHERE r.latitud IS NOT NULL AND r.longitud IS NOT NULL " +
           "AND r.estado NOT IN (com.ciudadactiva.entity.EstadoReporte.RECHAZADO, com.ciudadactiva.entity.EstadoReporte.CERRADO)")
    List<ReporteCoordenadaDTO> findReportesActivosParaCluster();

    @Query("SELECT r.id AS id, r.creadoEn AS creadoEn, r.fechaCierre AS fechaCierre, " +
           "r.estado AS estado, r.prioridad AS prioridad, " +
           "r.categoria.id AS categoriaId, r.categoria.nombre AS categoriaNombre " +
           "FROM Reporte r")
    List<ReporteEstadisticaDTO> findAllParaEstadisticas();

    long countByEstado(EstadoReporte estado);

    long countByCategoria_Id(Long categoriaId);

    long countByPrioridad(PrioridadReporte prioridad);
}
