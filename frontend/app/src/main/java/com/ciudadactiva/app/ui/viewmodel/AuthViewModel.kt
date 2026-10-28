package com.ciudadactiva.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.data.local.DemoAccount
import com.ciudadactiva.app.data.local.DemoCredentials
import com.ciudadactiva.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val rol: String) : AuthUiState()
    data class RequiereVerificacion(val email: String, val demoCode: String?) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {

    private val api = ApiClient.getService()
    private val session = CiudadActivaApplication.sessionManager

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    val selectedDemoAccount = MutableStateFlow<DemoAccount>(DemoCredentials.accounts[0])
    val emailInput = MutableStateFlow("")
    val passwordInput = MutableStateFlow("")
    val rememberSession = MutableStateFlow(true)

    fun selectDemoAccount(account: DemoAccount) {
        selectedDemoAccount.value = account
        emailInput.value = account.email
        passwordInput.value = account.pass
    }

    fun login(email: String = emailInput.value, pass: String = passwordInput.value) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = api.login(LoginRequest(email.trim(), pass))
                if (response.isSuccessful && response.body()?.data != null) {
                    val auth = response.body()!!.data!!
                    val userRol = auth.userRol
                    session.saveAuth(
                        token = auth.token,
                        id = auth.userId,
                        email = auth.userEmail,
                        nombre = auth.userNombre,
                        rol = userRol,
                        fotoUrl = auth.userFotoUrl,
                        telefono = auth.userTelefono,
                        direccion = auth.userDireccion
                    )

                    if (auth.requiereVerificacion == true) {
                        _uiState.value = AuthUiState.RequiereVerificacion(
                            email = email.trim().lowercase(),
                            demoCode = auth.codigoGeneradoDemo
                        )
                    } else {
                        _uiState.value = AuthUiState.Success(userRol)
                    }
                } else {
                    val errMsg = response.errorBody()?.string() ?: "Credenciales incorrectas"
                    _uiState.value = AuthUiState.Error("Credenciales inválidas. Verifica tu correo y contraseña.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error al conectar con el servidor: ${e.localizedMessage ?: "Verifique su red"}")
            }
        }
    }

    fun registrarCiudadano(nombre: String, email: String, pass: String, tel: String?, dir: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = api.registrar(RegisterRequest(nombre.trim(), email.trim().lowercase(), pass, tel?.trim(), dir?.trim()))
                if (response.isSuccessful && response.body()?.data != null) {
                    val auth = response.body()!!.data!!
                    val userRol = auth.userRol
                    session.saveAuth(
                        token = auth.token,
                        id = auth.userId,
                        email = auth.userEmail,
                        nombre = auth.userNombre,
                        rol = userRol,
                        fotoUrl = auth.userFotoUrl,
                        telefono = auth.userTelefono,
                        direccion = auth.userDireccion
                    )

                    if (auth.requiereVerificacion == true) {
                        _uiState.value = AuthUiState.RequiereVerificacion(
                            email = email.trim().lowercase(),
                            demoCode = auth.codigoGeneradoDemo
                        )
                    } else {
                        _uiState.value = AuthUiState.Success(userRol)
                    }
                } else {
                    val errorBodyStr = response.errorBody()?.string() ?: ""
                    val friendlyMsg = when {
                        errorBodyStr.contains("ya se encuentra registrado", ignoreCase = true) ->
                            "El correo electrónico ya se encuentra registrado. Por favor inicia sesión con tu contraseña."
                        errorBodyStr.contains("correo", ignoreCase = true) ->
                            "El formato del correo electrónico no es válido."
                        errorBodyStr.contains("contraseña", ignoreCase = true) ->
                            "La contraseña debe tener al menos 6 caracteres."
                        else ->
                            "No se pudo registrar la cuenta. Verifique los datos ingresados."
                    }
                    _uiState.value = AuthUiState.Error(friendlyMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Fallo de red al registrar: ${e.localizedMessage}")
            }
        }
    }

    fun loginConGoogle(googleEmail: String, googleName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Intenta login primero
                val loginResponse = api.login(LoginRequest(googleEmail.trim().lowercase(), "GoogleAuth_2026!"))
                if (loginResponse.isSuccessful && loginResponse.body()?.data != null) {
                    val auth = loginResponse.body()!!.data!!
                    session.saveAuth(
                        token = auth.token,
                        id = auth.userId,
                        email = auth.userEmail,
                        nombre = auth.userNombre,
                        rol = auth.userRol,
                        fotoUrl = auth.userFotoUrl,
                        telefono = auth.userTelefono,
                        direccion = auth.userDireccion
                    )
                    _uiState.value = AuthUiState.Success(auth.userRol)
                    return@launch
                }

                // Si no existe, lo registra automáticamente con cuenta Google
                val regResponse = api.registrar(
                    RegisterRequest(
                        nombre = googleName.ifBlank { "Usuario Google" },
                        email = googleEmail.trim().lowercase(),
                        password = "GoogleAuth_2026!",
                        telefono = null,
                        direccion = null
                    )
                )
                if (regResponse.isSuccessful && regResponse.body()?.data != null) {
                    val auth = regResponse.body()!!.data!!
                    session.saveAuth(
                        token = auth.token,
                        id = auth.userId,
                        email = auth.userEmail,
                        nombre = auth.userNombre,
                        rol = auth.userRol,
                        fotoUrl = auth.userFotoUrl,
                        telefono = auth.userTelefono,
                        direccion = auth.userDireccion
                    )
                    _uiState.value = AuthUiState.Success(auth.userRol)
                } else {
                    val err = regResponse.errorBody()?.string() ?: ""
                    if (err.contains("ya se encuentra registrado", ignoreCase = true)) {
                        _uiState.value = AuthUiState.Error("Esta cuenta de Google ya está registrada. Inicia sesión con tus credenciales habituales.")
                    } else {
                        _uiState.value = AuthUiState.Error("No se pudo autenticar con Google. Intente nuevamente.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error al conectar con Google: ${e.localizedMessage}")
            }
        }
    }

    fun verificarCodigo(email: String, codigo: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = api.verificarCodigo(VerificarCodigoRequest(email.trim().lowercase(), codigo.trim()))
                if (resp.isSuccessful && resp.body()?.data != null) {
                    val auth = resp.body()!!.data!!
                    session.saveAuth(
                        token = auth.token,
                        id = auth.userId,
                        email = auth.userEmail,
                        nombre = auth.userNombre,
                        rol = auth.userRol,
                        fotoUrl = auth.userFotoUrl,
                        telefono = auth.userTelefono,
                        direccion = auth.userDireccion
                    )
                    _uiState.value = AuthUiState.Success(auth.userRol)
                    onResult(true, "Cuenta verificada exitosamente")
                } else {
                    val errorBody = resp.errorBody()?.string() ?: ""
                    val msg = if (errorBody.contains("incorrecto", ignoreCase = true)) {
                        "El código de 6 dígitos es incorrecto."
                    } else if (errorBody.contains("expirado", ignoreCase = true)) {
                        "El código ha expirado. Por favor solicita uno nuevo."
                    } else {
                        "No se pudo verificar el código. Intenta de nuevo."
                    }
                    onResult(false, msg)
                }
            } catch (e: Exception) {
                onResult(false, "Fallo de comunicación: ${e.localizedMessage}")
            }
        }
    }

    fun reenviarCodigo(email: String, tipo: String = "VERIFICACION", onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = api.reenviarCodigo(ReenviarCodigoRequest(email.trim().lowercase(), tipo))
                if (resp.isSuccessful) {
                    val msg = resp.body()?.data ?: "Código reenviado con éxito"
                    onResult(true, msg)
                } else {
                    onResult(false, "No se pudo reenviar el código. Intenta de nuevo.")
                }
            } catch (e: Exception) {
                onResult(false, "Error de red: ${e.localizedMessage}")
            }
        }
    }

    fun solicitarRecuperacionPassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = api.solicitarRecuperacionPassword(ForgotPasswordRequest(email.trim().lowercase()))
                if (resp.isSuccessful) {
                    val msg = resp.body()?.data ?: "Código de recuperación generado."
                    onResult(true, msg)
                } else {
                    onResult(false, "No se pudo procesar la solicitud de recuperación.")
                }
            } catch (e: Exception) {
                onResult(false, "Fallo de conexión: ${e.localizedMessage}")
            }
        }
    }

    fun resetPassword(email: String, codigo: String, nuevaPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = api.resetPassword(ResetPasswordRequest(email.trim().lowercase(), codigo.trim(), nuevaPass))
                if (resp.isSuccessful) {
                    val msg = resp.body()?.data ?: "Contraseña restablecida con éxito."
                    onResult(true, msg)
                } else {
                    val errorBody = resp.errorBody()?.string() ?: ""
                    val msg = if (errorBody.contains("incorrecto", ignoreCase = true)) {
                        "El código de recuperación es incorrecto."
                    } else if (errorBody.contains("expirado", ignoreCase = true)) {
                        "El código ha expirado. Por favor solicita uno nuevo."
                    } else {
                        "No se pudo restablecer la contraseña. Verifica los datos."
                    }
                    onResult(false, msg)
                }
            } catch (e: Exception) {
                onResult(false, "Error de red: ${e.localizedMessage}")
            }
        }
    }

    fun cambiarPassword(actual: String, nueva: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = api.cambiarPassword(CambiarPasswordRequest(actual, nueva))
                if (resp.isSuccessful) {
                    onResult(true, "Contraseña actualizada exitosamente.")
                } else {
                    val err = resp.errorBody()?.string() ?: ""
                    onResult(false, if (err.contains("incorrecta", ignoreCase = true)) "La contraseña actual es incorrecta." else "No se pudo cambiar la contraseña.")
                }
            } catch (e: Exception) {
                onResult(false, "Error de comunicación: ${e.localizedMessage}")
            }
        }
    }

    fun cerrarSesion() {
        session.clearSession()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
