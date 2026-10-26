package com.ciudadactiva.repository;

import com.ciudadactiva.entity.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {
    Optional<Asignacion> findByReporte_IdAndActivaTrue(Long reporteId);
    List<Asignacion> findByOperador_IdAndActivaTrueOrderByCreadoEnDesc(Long operadorId);
    long countByOperador_IdAndActivaTrue(Long operadorId);
    List<Asignacion> findByActivaTrueOrderByCreadoEnDesc();
    List<Asignacion> findByReporte_IdOrderByCreadoEnDesc(Long reporteId);
}
