package com.ciudadactiva.app.data.model

data class AsignacionModel(
    val id: Long,
    val reporteId: Long,
    val reporteTitulo: String?,
    val operadorId: Long,
    val operadorNombre: String?,
    val gestorId: Long?,
    val gestorNombre: String?,
    val notas: String?,
    val activa: Boolean,
    val creadoEn: String?
)

data class AsignacionCreateRequest(
    val operadorId: Long,
    val notas: String? = null
)
