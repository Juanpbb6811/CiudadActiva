package com.ciudadactiva.app.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val direccion: String? = null
)

data class AuthResponse(
    val token: String,
    val tipo: String,
    val id: Long,
    val email: String,
    val nombre: String,
    val rol: String
)

data class UsuarioModel(
    val id: Long,
    val nombre: String,
    val email: String,
    val telefono: String?,
    val direccion: String?,
    val activo: Boolean,
    val rol: String,
    val laboresActivas: Long? = 0,
    val creadoEn: String?
)

data class UsuarioUpdateRequest(
    val nombre: String?,
    val telefono: String?,
    val direccion: String?,
    val activo: Boolean? = null
)

data class UsuarioCreateAdminRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val rol: String
)
