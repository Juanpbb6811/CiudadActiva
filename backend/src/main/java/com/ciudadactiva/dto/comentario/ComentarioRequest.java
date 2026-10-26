package com.ciudadactiva.dto.comentario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ComentarioRequest {

    @NotBlank(message = "El contenido del comentario no puede estar vacío")
    @Size(min = 2, max = 1000, message = "El comentario debe tener entre 2 y 1000 caracteres")
    private String contenido;

    public ComentarioRequest() {
    }

    public ComentarioRequest(String contenido) {
        this.contenido = contenido;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}
