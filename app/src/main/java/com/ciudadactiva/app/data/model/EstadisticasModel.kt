package com.ciudadactiva.app.data.model

data class EstadisticasModel(
    val totalReportes: Long = 0,
    val reportesPorEstado: Map<String, Long> = emptyMap(),
    val reportesPorCategoria: Map<String, Long> = emptyMap(),
    val reportesPorPrioridad: Map<String, Long> = emptyMap(),
    val tiempoPromedioAtencionHoras: Double = 0.0,
    val reportesAtendidosOCerrados: Long = 0,
    val reportesPendientes: Long = 0,
    val semaforoGlobal: SemaforoModel? = null,
    val semaforoPorCategoria: Map<String, SemaforoModel> = emptyMap()
)
