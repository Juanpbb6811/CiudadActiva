package com.ciudadactiva.dto.usuario;

import com.ciudadactiva.entity.RolNombre;
import jakarta.validation.constraints.NotNull;

public class CambiarRolRequest {

    @NotNull(message = "El nuevo rol es obligatorio")
    private RolNombre rol;

    public CambiarRolRequest() {
    }

    public CambiarRolRequest(RolNombre rol) {
        this.rol = rol;
    }

    public RolNombre getRol() {
        return rol;
    }

    public void setRol(RolNombre rol) {
        this.rol = rol;
    }
}
