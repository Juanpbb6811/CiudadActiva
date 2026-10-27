package com.ciudadactiva.app.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class OperadorViewModel : ViewModel() {

    private val api = ApiClient.getService()

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    val regla3Bloqueada = MutableStateFlow(false) // Alerta Regla 3

    val tareasAsignadas = MutableStateFlow<List<AsignacionModel>>(emptyList())
    val todosReportesCampo = MutableStateFlow<List<ReporteResponse>>(emptyList())
    val tareasResueltas = MutableStateFlow<List<ReporteResponse>>(emptyList())
    val fichaSeleccionada = MutableStateFlow<ReporteDetalleResponse?>(null)

    // Pestaña activa en pantalla: "MIS_TAREAS" o "TODOS"
    val tabSeleccionado = MutableStateFlow("TODOS")
    val filtroEstadoCampo = MutableStateFlow("TODOS")

    // Formulario de avance
    val avanceObservaciones = MutableStateFlow("")
    val avanceEvidenciaFoto = MutableStateFlow<Bitmap?>(null)

    // Formulario de cierre
    val cierreObservaciones = MutableStateFlow("")
    val cierreEvidenciaFoto = MutableStateFlow<Bitmap?>(null)

    init {
        cargarTareas()
    }

    fun setTab(tab: String) {
        tabSeleccionado.value = tab
    }

    fun setFiltroEstado(estado: String) {
        filtroEstadoCampo.value = estado
        cargarReportesCampo(estado)
    }

    fun cargarTareas() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                // 1. Cargar asignaciones asignadas al operador
                val resp = api.listarMisAsignaciones()
                if (resp.isSuccessful && resp.body()?.data != null) {
                    tareasAsignadas.value = resp.body()!!.data!!
                }

                // 2. Cargar todos los reportes de campo disponibles
                cargarReportesCampo(filtroEstadoCampo.value)

                // 3. Cargar también reportes cerrados para el historial
                val histResp = api.listarReportes(estado = "CERRADO")
                if (histResp.isSuccessful && histResp.body()?.data != null) {
                    tareasResueltas.value = histResp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al sincronizar tareas: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun cargarReportesCampo(estado: String = "TODOS") {
        viewModelScope.launch {
            try {
                val param = if (estado == "TODOS") null else estado
                val resp = api.listarReportes(estado = param)
                if (resp.isSuccessful && resp.body()?.data != null) {
                    todosReportesCampo.value = resp.body()!!.data!!
                }
            } catch (e: Exception) {
                // Silencioso
            }
        }
    }

    fun cargarFicha(reporteId: Long) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val resp = api.obtenerDetalleReporte(reporteId)
                if (resp.isSuccessful && resp.body()?.data != null) {
                    fichaSeleccionada.value = resp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }

    // RF06: Registrar avance operativo con fotografía opcional en terreno
    fun registrarAvance(reporteId: Long, observaciones: String, onSuccess: () -> Unit) {
        registrarAvanceConEvidencia(reporteId, observaciones, null, "EN_PROCESO", onSuccess)
    }

    fun registrarAvanceConEvidencia(
        reporteId: Long,
        observaciones: String,
        fotoSoporte: Bitmap?,
        nuevoEstado: String = "EN_PROCESO",
        onSuccess: () -> Unit
    ) {
        if (observaciones.isBlank()) {
            errorMessage.value = "Debe especificar las observaciones técnicas del avance."
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            try {
                // Si adjuntó foto de soporte en campo, subirla
                if (fotoSoporte != null) {
                    val stream = ByteArrayOutputStream()
                    fotoSoporte.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val reqFoto = stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val partFoto = MultipartBody.Part.createFormData("foto", "avance_${System.currentTimeMillis()}.jpg", reqFoto)
                    val reqObs = observaciones.toRequestBody(MultipartBody.FORM)
                    try {
                        api.subirFotoSolucion(reporteId, partFoto, reqObs)
                    } catch (_: Exception) {}
                }

                val req = SeguimientoCreateRequest(
                    nuevoEstado = nuevoEstado,
                    accion = "AVANCE_OPERATIVO",
                    observaciones = observaciones
                )
                val resp = api.registrarAvance(reporteId, req)
                if (resp.isSuccessful) {
                    cargarTareas()
                    cargarFicha(reporteId)
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

    // Transición directa de estado por el Operador (ej. marcar EN_PROCESO o ATENDIDO)
    fun cambiarEstado(reporteId: Long, nuevoEstado: String, observaciones: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val req = CambiarEstadoRequest(
                    estado = nuevoEstado,
                    observaciones = observaciones.ifBlank { "Actualización operativa de estado en terreno" }
                )
                val resp = api.cambiarEstadoReporte(reporteId, req)
                if (resp.isSuccessful) {
                    cargarTareas()
                    cargarFicha(reporteId)
                    onSuccess()
                } else {
                    errorMessage.value = resp.errorBody()?.string() ?: resp.message()
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }

    // RF08 & Regla 3: Cierre con evidencia
    fun cerrarIncidenciaConEvidencia(
        reporteId: Long,
        observaciones: String,
        fotoSolucion: Bitmap?,
        onSuccess: () -> Unit,
        onRegla3Violation: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            regla3Bloqueada.value = false
            try {
                val resp = if (fotoSolucion != null) {
                    val stream = ByteArrayOutputStream()
                    fotoSolucion.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val reqFoto = stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val partFoto = MultipartBody.Part.createFormData("foto", "solucion_${System.currentTimeMillis()}.jpg", reqFoto)
                    val reqObs = observaciones.toRequestBody(MultipartBody.FORM)

                    api.subirFotoSolucion(reporteId, partFoto, reqObs)
                } else {
                    val req = CerrarReporteRequest(
                        evidenciaCierreUrl = "https://evidencia-solucion.ciudadactiva.gov.co/reportes/$reporteId",
                        observaciones = observaciones
                    )
                    api.cerrarReporte(reporteId, req)
                }

                if (resp.isSuccessful) {
                    cargarTareas()
                    cargarFicha(reporteId)
                    onSuccess()
                } else {
                    val err = resp.errorBody()?.string() ?: resp.message()
                    if (err.contains("Regla de Negocio: Un reporte no puede cerrarse si no registra previamente al menos una acción")) {
                        regla3Bloqueada.value = true
                        onRegla3Violation()
                    } else {
                        errorMessage.value = err
                    }
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }
}
