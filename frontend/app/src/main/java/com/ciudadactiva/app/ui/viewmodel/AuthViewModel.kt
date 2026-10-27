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
    val emailInput = MutableStateFlow(DemoCredentials.accounts[0].email)
    val passwordInput = MutableStateFlow(DemoCredentials.accounts[0].pass)
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
                        rol = userRol
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
                val response = api.registrar(RegisterRequest(nombre, email, pass, tel, dir))
                if (response.isSuccessful && response.body()?.data != null) {
                    val auth = response.body()!!.data!!
                    val userRol = auth.userRol
                    session.saveAuth(
                        token = auth.token,
                        id = auth.userId,
                        email = auth.userEmail,
                        nombre = auth.userNombre,
                        rol = userRol
                    )
                    _uiState.value = AuthUiState.Success(userRol)
                } else {
                    _uiState.value = AuthUiState.Error("No se pudo registrar la cuenta. Verifique que el correo no esté duplicado.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Fallo de red: ${e.localizedMessage}")
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
