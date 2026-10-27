package com.ciudadactiva.app.data.model

data class ReporteResponse(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val categoriaId: Long?,
    val categoriaNombre: String?,
    val categoriaIcono: String?,
    val ciudadanoId: Long?,
    val ciudadanoNombre: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val imagenUrl: String?,
    val tieneFoto: Boolean? = false,
    val fotoTipo: String? = null,
    val estado: String, // PENDIENTE, ADMITIDO, RECHAZADO, EN_PROCESO, ATENDIDO, CERRADO
    val prioridad: String?, // BAJA, MEDIA, ALTA, CRITICA
    val justificacionRechazo: String?,
    val evidenciaCierreUrl: String?,
    val tieneFotoSolucion: Boolean? = false,
    val fotoSolucionTipo: String? = null,
    val operadorAsignadoNombre: String?,
    val reportesCercanos: Int? = 1,
    val semaforoColorHex: String? = "#4CAF50",
    val semaforoNivel: String? = "BAJO",
    val semaforoDescripcion: String? = "1 reporte: Nivel bajo",
    val creadoEn: String?,
    val actualizadoEn: String?,
    val fechaCierre: String?
)

data class ReporteCreateRequest(
    val titulo: String,
    val descripcion: String,
    val categoriaId: Long,
    val latitud: Double? = 4.60971,
    val longitud: Double? = -74.08175,
    val direccion: String? = null,
    val imagenUrl: String? = null
)

data class ReporteDetalleResponse(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val categoriaId: Long?,
    val categoriaNombre: String?,
    val categoriaIcono: String?,
    val ciudadanoId: Long?,
    val ciudadanoNombre: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val imagenUrl: String?,
    val tieneFoto: Boolean? = false,
    val estado: String,
    val prioridad: String?,
    val justificacionRechazo: String?,
    val evidenciaCierreUrl: String?,
    val tieneFotoSolucion: Boolean? = false,
    val operadorAsignadoNombre: String?,
    val reportesCercanos: Int? = 1,
    val semaforoColorHex: String? = "#4CAF50",
    val semaforoNivel: String? = "BAJO",
    val semaforoDescripcion: String? = "1 reporte: Nivel bajo",
    val creadoEn: String?,
    val actualizadoEn: String?,
    val fechaCierre: String?,
    val asignacionActual: AsignacionModel? = null,
    val lineaDeTiempo: List<SeguimientoModel> = emptyList(),
    val comentarios: List<ComentarioModel> = emptyList()
)

data class ValidarReporteRequest(
    val admitido: Boolean,
    val justificacion: String?,
    val prioridad: String? = null
)

data class CambiarPrioridadRequest(
    val prioridad: String,
    val observaciones: String? = null
)

data class CerrarReporteRequest(
    val evidenciaCierreUrl: String,
    val observaciones: String? = null
)

data class CambiarEstadoRequest(
    val estado: String,
    val observaciones: String? = null
)
