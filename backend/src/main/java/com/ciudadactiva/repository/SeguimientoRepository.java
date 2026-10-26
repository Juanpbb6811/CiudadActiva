package com.ciudadactiva.repository;

import com.ciudadactiva.entity.RolNombre;
import com.ciudadactiva.entity.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {
    List<Seguimiento> findByReporte_IdOrderByCreadoEnAsc(Long reporteId);
    List<Seguimiento> findByReporte_IdOrderByCreadoEnDesc(Long reporteId);
    List<Seguimiento> findAllByOrderByCreadoEnDesc();
    long countByReporte_IdAndUsuario_Rol_Nombre(Long reporteId, RolNombre rolNombre);
    long countByReporte_IdAndUsuario_Id(Long reporteId, Long usuarioId);
}
