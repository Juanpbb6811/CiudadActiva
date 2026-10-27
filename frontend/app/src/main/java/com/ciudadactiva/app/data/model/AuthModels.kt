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
    val tipoToken: String? = "Bearer",
    val tipo: String? = "Bearer",
    val usuario: UsuarioModel? = null,
    val id: Long? = null,
    val email: String? = null,
    val nombre: String? = null,
    val rol: String? = null
) {
    val userId: Long
        get() = usuario?.id ?: id ?: -1L

    val userEmail: String
        get() = usuario?.email ?: email ?: ""

    val userNombre: String
        get() = usuario?.nombre ?: nombre ?: ""

    val userRol: String
        get() = usuario?.rol ?: rol ?: "ROLE_CIUDADANO"
}

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
