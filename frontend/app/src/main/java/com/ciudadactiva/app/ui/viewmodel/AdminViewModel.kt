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

    // Datos del panel administrativo
    val estadisticas = MutableStateFlow<EstadisticasModel?>(null)
    val usuarios = MutableStateFlow<List<UsuarioModel>>(emptyList())
    val categorias = MutableStateFlow<List<CategoriaModel>>(emptyList())
    val auditoriaTrazabilidad = MutableStateFlow<List<SeguimientoModel>>(emptyList())
    val reportes = MutableStateFlow<List<ReporteResponse>>(emptyList())

    // Filtros de reporte y usuarios
    val filtroEstadoReportes = MutableStateFlow("TODOS")
    val filtroRolUsuarios = MutableStateFlow("TODOS")

    init {
        cargarTodo()
    }

    fun cargarTodo() {
        cargarEstadisticas()
        cargarUsuarios()
        cargarCategorias()
        cargarAuditoria()
        cargarReportes()
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                val resp = api.obtenerEstadisticas()
                if (resp.isSuccessful && resp.body()?.data != null) {
                    estadisticas.value = resp.body()!!.data!!
                }
            } catch (e: Exception) {
                // Silencioso en caso de no estar autenticado al boot inicial
            }
        }
    }

    fun cargarUsuarios(rol: String? = null) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val paramRol = if (rol == "TODOS" || rol.isNullOrBlank()) null else rol
                val resp = api.listarTodosLosUsuarios(paramRol)
                if (resp.isSuccessful && resp.body()?.data != null) {
                    usuarios.value = resp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al cargar usuarios: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun cargarCategorias() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val resp = api.listarCategorias()
                if (resp.isSuccessful && resp.body()?.data != null) {
                    categorias.value = resp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al cargar categorías: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun cargarAuditoria() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val resp = api.consultarAuditoriaRUP()
                if (resp.isSuccessful && resp.body()?.data != null) {
                    auditoriaTrazabilidad.value = resp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al cargar matriz de auditoría: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun cargarReportes(estado: String? = null) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val param = if (estado == "TODOS" || estado.isNullOrBlank()) null else estado
                val resp = api.listarReportes(estado = param)
                if (resp.isSuccessful && resp.body()?.data != null) {
                    reportes.value = resp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al cargar reportes municipales: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun setFiltroReportes(estado: String) {
        filtroEstadoReportes.value = estado
        cargarReportes(estado)
    }

    fun cambiarEstadoReporte(reporteId: Long, nuevoEstado: String, observaciones: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val req = CambiarEstadoRequest(
                    estado = nuevoEstado,
                    observaciones = observaciones.ifBlank { "Actualización de estado realizada por el Administrador Municipal" }
                )
                val resp = api.cambiarEstadoReporte(reporteId, req)
                if (resp.isSuccessful) {
                    cargarReportes(filtroEstadoReportes.value)
                    cargarEstadisticas()
                    cargarAuditoria()
                    onSuccess()
                } else {
                    errorMessage.value = "Error al actualizar estado: ${resp.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun eliminarReporte(reporteId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val resp = api.eliminarReporte(reporteId)
                if (resp.isSuccessful) {
                    cargarReportes(filtroEstadoReportes.value)
                    cargarEstadisticas()
                    cargarAuditoria()
                    onSuccess()
                } else {
                    errorMessage.value = "Error al eliminar reporte: ${resp.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.localizedMessage}"
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
                    cargarUsuarios(filtroRolUsuarios.value)
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
                    cargarUsuarios(filtroRolUsuarios.value)
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
                    cargarCategorias()
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
