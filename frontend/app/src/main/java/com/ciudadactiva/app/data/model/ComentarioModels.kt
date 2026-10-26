package com.ciudadactiva.app.data.model

data class ComentarioModel(
    val id: Long,
    val reporteId: Long,
    val usuarioId: Long,
    val usuarioNombre: String,
    val usuarioRol: String?,
    val contenido: String,
    val creadoEn: String?,
    val actualizadoEn: String?
)

data class ComentarioCreateRequest(
    val contenido: String
)
