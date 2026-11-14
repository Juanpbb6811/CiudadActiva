package com.ciudadactiva.entity;

public enum TipoDocumento {
    CC("Cédula de Ciudadanía"),
    TI("Tarjeta de Identidad"),
    CE("Cédula de Extranjería"),
    PASAPORTE("Pasaporte"),
    PPT("Permiso por Protección Temporal");

    private final String descripcion;

    TipoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
