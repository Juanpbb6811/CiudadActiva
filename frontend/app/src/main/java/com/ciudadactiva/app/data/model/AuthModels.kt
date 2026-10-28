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
    val rol: String? = null,
    val requiereVerificacion: Boolean? = false,
    val codigoGeneradoDemo: String? = null
) {
    val userId: Long
        get() = usuario?.id ?: id ?: -1L

    val userEmail: String
        get() = usuario?.email ?: email ?: ""

    val userNombre: String
        get() = usuario?.nombre ?: nombre ?: ""

    val userRol: String
        get() = usuario?.rol ?: rol ?: "ROLE_CIUDADANO"

    val userTelefono: String?
        get() = usuario?.telefono

    val userDireccion: String?
        get() = usuario?.direccion

    val userFotoUrl: String?
        get() = usuario?.fotoUrl
}

data class UsuarioModel(
    val id: Long,
    val nombre: String,
    val email: String,
    val telefono: String?,
    val direccion: String?,
    val fotoUrl: String? = null,
    val activo: Boolean,
    val verificado: Boolean? = false,
    val rol: String,
    val laboresActivas: Long? = 0,
    val creadoEn: String?
)

data class UsuarioUpdateRequest(
    val nombre: String?,
    val email: String? = null,
    val telefono: String?,
    val direccion: String?,
    val fotoUrl: String? = null,
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

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val codigo: String,
    val nuevaPassword: String
)

data class VerificarCodigoRequest(
    val email: String,
    val codigo: String
)

data class ReenviarCodigoRequest(
    val email: String,
    val tipo: String = "VERIFICACION"
)

data class CambiarPasswordRequest(
    val passwordActual: String,
    val passwordNueva: String
)
