package com.ciudadactiva.dto.reporte;

public record DireccionBogota(
        String direccionFormateada,
        String barrio,
        String localidad,
        String departamento
) {
    public static DireccionBogota porDefecto(String fallbackDireccion) {
        return new DireccionBogota(
                fallbackDireccion != null && !fallbackDireccion.isBlank() ? fallbackDireccion : "Bogotá D.C., Colombia",
                "Chapinero",
                "Bogotá",
                "Cundinamarca"
        );
    }
}
