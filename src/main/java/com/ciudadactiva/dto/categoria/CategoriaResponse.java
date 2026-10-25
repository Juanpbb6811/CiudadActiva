package com.ciudadactiva.dto.categoria;

import com.ciudadactiva.entity.Categoria;
import java.time.LocalDateTime;

public class CategoriaResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String icono;
    private Boolean activa;
    private LocalDateTime creadoEn;

    public CategoriaResponse() {
    }

    public CategoriaResponse(Categoria categoria) {
        if (categoria != null) {
            this.id = categoria.getId();
            this.nombre = categoria.getNombre();
            this.descripcion = categoria.getDescripcion();
            this.icono = categoria.getIcono();
            this.activa = categoria.getActiva();
            this.creadoEn = categoria.getCreadoEn();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
