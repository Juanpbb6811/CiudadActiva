package com.ciudadactiva.repository;

import com.ciudadactiva.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByReporte_IdOrderByCreadoEnAsc(Long reporteId);
}
