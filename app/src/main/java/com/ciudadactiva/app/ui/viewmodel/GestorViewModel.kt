package com.ciudadactiva.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GestorViewModel : ViewModel() {

    private val api = ApiClient.getService()

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Bandejas
    val reportesPendientes = MutableStateFlow<List<ReporteResponse>>(emptyList())
    val asignacionesActivas = MutableStateFlow<List<AsignacionModel>>(emptyList())
    val operadoresDisponibles = MutableStateFlow<List<UsuarioModel>>(emptyList())
    val estadisticas = MutableStateFlow<EstadisticasModel?>(null)

    // Ficha actual
    val reporteSeleccionado = MutableStateFlow<ReporteDetalleResponse?>(null)

    init {
        cargarDashboard()
    }

    fun cargarDashboard() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                // Reportes pendientes de validación
                val pendResp = api.listarReportes(estado = "PENDIENTE")
                if (pendResp.isSuccessful && pendResp.body()?.data != null) {
                    reportesPendientes.value = pendResp.body()!!.data!!
                }

                // Asignaciones activas
                val asigResp = api.listarAsignacionesActivas()
                if (asigResp.isSuccessful && asigResp.body()?.data != null) {
                    asignacionesActivas.value = asigResp.body()!!.data!!
                }

                // Operadores en campo
                val opResp = api.listarOperadores()
                if (opResp.isSuccessful && opResp.body()?.data != null) {
                    operadoresDisponibles.value = opResp.body()!!.data!!
                }

                // Estadísticas con semáforo
                val statResp = api.obtenerEstadisticas()
                if (statResp.isSuccessful && statResp.body()?.data != null) {
                    estadisticas.value = statResp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al sincronizar bandeja de gestor: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun cargarFichaInspeccion(reporteId: Long) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val resp = api.obtenerDetalleReporte(reporteId)
                if (resp.isSuccessful && resp.body()?.data != null) {
                    reporteSeleccionado.value = resp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }

    // Regla 1: Todo reporte rechazado debe incluir una justificación clara
    fun rechazarReporte(reporteId: Long, justificacion: String, onSuccess: () -> Unit) {
        if (justificacion.trim().isEmpty()) {
            errorMessage.value = "Regla de Negocio 1: La justificación técnica de rechazo es obligatoria."
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            try {
                val req = ValidarReporteRequest(admitido = false, justificacion = justificacion)
                val resp = api.validarReporte(reporteId, req)
                if (resp.isSuccessful) {
                    cargarDashboard()
                    onSuccess()
                } else {
                    errorMessage.value = resp.errorBody()?.string() ?: "Error al rechazar"
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }

    // Regla 2: Solo un usuario con rol de gestor autorizado puede modificar la prioridad oficial
    fun admitirYPriorizarReporte(reporteId: Long, prioridad: String, justificacion: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val req = ValidarReporteRequest(admitido = true, justificacion = justificacion, prioridad = prioridad)
                val resp = api.validarReporte(reporteId, req)
                if (resp.isSuccessful) {
                    cargarDashboard()
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

    // Regla 5: Asignar operador en campo
    fun asignarOperador(reporteId: Long, operadorId: Long, notas: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val req = AsignacionCreateRequest(operadorId = operadorId, notas = notas)
                val resp = api.asignarOperador(reporteId, req)
                if (resp.isSuccessful) {
                    cargarDashboard()
                    onSuccess()
                } else {
                    errorMessage.value = "No se pudo asignar el operador: ${resp.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }
}
