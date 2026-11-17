package com.ciudadactiva.dto.common;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo para activar / desactivar lógicamente un registro (RNF08: nunca DELETE físico).
 * Ejemplo: { "activo": false }
 */
public class EstadoActivoRequest {

    @NotNull(message = "Debe indicar el estado activo (true) o inactivo (false)")
    private Boolean activo;

    public EstadoActivoRequest() {
    }

    public EstadoActivoRequest(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
