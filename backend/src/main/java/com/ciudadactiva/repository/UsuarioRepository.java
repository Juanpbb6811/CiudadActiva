package com.ciudadactiva.repository;

import com.ciudadactiva.entity.RolNombre;
import com.ciudadactiva.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByRol_Nombre(RolNombre rolNombre);
    List<Usuario> findByActivoTrue();
    List<Usuario> findAllByOrderByCreadoEnDesc();
}
