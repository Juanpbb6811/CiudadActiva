package com.ciudadactiva.dto.reporte;

public record GeminiVisionAnalysis(
        boolean esValido,
        String categoriaDetectada,
        String prioridadSugerida,
        String diagnostico
) {
    public static GeminiVisionAnalysis noDisponible(String motivo) {
        return new GeminiVisionAnalysis(true, "NO_DETERMINADA", "MEDIA", motivo);
    }
}
