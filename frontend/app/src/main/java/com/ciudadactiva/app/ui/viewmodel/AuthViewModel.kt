package com.ciudadactiva.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.data.local.DemoAccount
import com.ciudadactiva.app.data.local.DemoCredentials
import com.ciudadactiva.app.data.model.LoginRequest
import com.ciudadactiva.app.data.model.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val rol: String) : AuthUiState()
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
                    _uiState.value = AuthUiState.Success(userRol)
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
                    _uiState.value = AuthUiState.Success(userRol)
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

    fun cerrarSesion() {
        session.clearSession()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
