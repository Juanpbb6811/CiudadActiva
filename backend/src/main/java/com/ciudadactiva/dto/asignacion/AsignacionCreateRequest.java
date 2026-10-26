package com.ciudadactiva.dto.asignacion;

import jakarta.validation.constraints.NotNull;

public class AsignacionCreateRequest {

    @NotNull(message = "El ID del operador es obligatorio")
    private Long operadorId;

    private String notas;

    public AsignacionCreateRequest() {
    }

    public AsignacionCreateRequest(Long operadorId, String notas) {
        this.operadorId = operadorId;
        this.notas = notas;
    }

    public Long getOperadorId() {
        return operadorId;
    }

    public void setOperadorId(Long operadorId) {
        this.operadorId = operadorId;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
