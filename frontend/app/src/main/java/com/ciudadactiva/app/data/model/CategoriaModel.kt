package com.ciudadactiva.app.data.model

data class CategoriaModel(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val icono: String?,
    val activa: Boolean = true,
    val totalReportes: Long? = 0,
    val semaforo: SemaforoModel? = null
)

data class CategoriaCreateRequest(
    val nombre: String,
    val descripcion: String?,
    val icono: String?
)
