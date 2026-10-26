package com.ciudadactiva.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val api = ApiClient.getService()

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Datos del panel
    val estadisticas = MutableStateFlow<EstadisticasModel?>(null)
    val usuarios = MutableStateFlow<List<UsuarioModel>>(emptyList())
    val categorias = MutableStateFlow<List<CategoriaModel>>(emptyList())
    val auditoriaTrazabilidad = MutableStateFlow<List<SeguimientoModel>>(emptyList())

    init {
        cargarTodo()
    }

    fun cargarTodo() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                // Estadísticas
                val estResp = api.obtenerEstadisticas()
                if (estResp.isSuccessful && estResp.body()?.data != null) {
                    estadisticas.value = estResp.body()!!.data!!
                }

                // Usuarios
                val userResp = api.listarTodosLosUsuarios()
                if (userResp.isSuccessful && userResp.body()?.data != null) {
                    usuarios.value = userResp.body()!!.data!!
                }

                // Categorías
                val catResp = api.listarCategorias()
                if (catResp.isSuccessful && catResp.body()?.data != null) {
                    categorias.value = catResp.body()!!.data!!
                }

                // Auditoría RUP
                val auditResp = api.consultarAuditoriaRUP()
                if (auditResp.isSuccessful && auditResp.body()?.data != null) {
                    auditoriaTrazabilidad.value = auditResp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al sincronizar módulo administrativo: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun crearUsuario(nombre: String, email: String, pass: String, rol: String, tel: String?, dir: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val req = UsuarioCreateAdminRequest(nombre, email, pass, tel, dir, rol)
                val resp = api.crearUsuarioAdmin(req)
                if (resp.isSuccessful) {
                    cargarTodo()
                    onSuccess()
                } else {
                    errorMessage.value = resp.message()
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }

    fun desactivarUsuario(usuarioId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val resp = api.desactivarUsuarioAdmin(usuarioId)
                if (resp.isSuccessful) {
                    cargarTodo()
                    onSuccess()
                } else {
                    errorMessage.value = resp.message()
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }

    fun crearCategoria(nombre: String, descripcion: String?, icono: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val req = CategoriaCreateRequest(nombre, descripcion, icono)
                val resp = api.crearCategoria(req)
                if (resp.isSuccessful) {
                    cargarTodo()
                    onSuccess()
                } else {
                    errorMessage.value = resp.message()
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }
}
