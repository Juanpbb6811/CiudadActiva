package com.ciudadactiva.repository;

import com.ciudadactiva.entity.ReporteImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReporteImagenRepository extends JpaRepository<ReporteImagen, Long> {

    Optional<ReporteImagen> findFirstByReporteIdOrderByIdDesc(Long reporteId);

    boolean existsByReporteId(Long reporteId);

    void deleteByReporteId(Long reporteId);
}
