package com.ciudadactiva.service;

import com.ciudadactiva.dto.categoria.CategoriaRequest;
import com.ciudadactiva.dto.categoria.CategoriaResponse;
import com.ciudadactiva.entity.Categoria;
import com.ciudadactiva.exception.BadRequestException;
import com.ciudadactiva.exception.ResourceNotFoundException;
import com.ciudadactiva.repository.CategoriaRepository;
import com.ciudadactiva.repository.ReporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ReporteRepository reporteRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ReporteRepository reporteRepository) {
        this.categoriaRepository = categoriaRepository;
        this.reporteRepository = reporteRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findByActivaTrue().stream()
                .map(c -> new CategoriaResponse(c, reporteRepository.countByCategoria_Id(c.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(c -> new CategoriaResponse(c, reporteRepository.countByCategoria_Id(c.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = buscarPorId(id);
        long total = reporteRepository.countByCategoria_Id(id);
        return new CategoriaResponse(categoria, total);
    }

    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BadRequestException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = new Categoria(
                request.getNombre(),
                request.getDescripcion(),
                request.getIcono() != null ? request.getIcono() : "tag"
        );
        if (request.getActiva() != null) {
            categoria.setActiva(request.getActiva());
        }

        return new CategoriaResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizarCategoria(Long id, CategoriaRequest request) {
        Categoria categoria = buscarPorId(id);

        if (!categoria.getNombre().equalsIgnoreCase(request.getNombre())
                && categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BadRequestException("Ya existe otra categoría con el nombre: " + request.getNombre());
        }

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        if (request.getIcono() != null) {
            categoria.setIcono(request.getIcono());
        }
        if (request.getActiva() != null) {
            categoria.setActiva(request.getActiva());
        }

        return new CategoriaResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminarODesactivarCategoria(Long id) {
        // RNF08: prohibido el DELETE físico. Siempre baja lógica (activa = false).
        Categoria categoria = buscarPorId(id);
        categoria.setActiva(false);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public CategoriaResponse cambiarEstado(Long id, boolean activa) {
        Categoria categoria = buscarPorId(id);
        categoria.setActiva(activa);
        Categoria guardada = categoriaRepository.save(categoria);
        return new CategoriaResponse(guardada, reporteRepository.countByCategoria_Id(id));
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }
}
