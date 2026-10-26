package com.ciudadactiva.app.data.model

data class SeguimientoModel(
    val id: Long,
    val reporteId: Long?,
    val reporteTitulo: String?,
    val usuarioId: Long?,
    val usuarioNombre: String?,
    val usuarioEmail: String?,
    val usuarioRol: String?,
    val estadoAnterior: String?,
    val estadoNuevo: String?,
    val accion: String?,
    val observaciones: String?,
    val evidenciaUrl: String?,
    val hashTrazabilidad: String? = null,
    val creadoEn: String?
)

data class SeguimientoCreateRequest(
    val nuevoEstado: String? = null,
    val accion: String,
    val observaciones: String,
    val evidenciaUrl: String? = null
)
