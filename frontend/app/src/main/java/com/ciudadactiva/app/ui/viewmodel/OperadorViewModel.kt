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
    val tareasResueltas = MutableStateFlow<List<ReporteResponse>>(emptyList())
    val fichaSeleccionada = MutableStateFlow<ReporteDetalleResponse?>(null)

    // Formulario de avance
    val avanceObservaciones = MutableStateFlow("")
    val avanceEvidenciaFoto = MutableStateFlow<Bitmap?>(null)

    // Formulario de cierre
    val cierreObservaciones = MutableStateFlow("")
    val cierreEvidenciaFoto = MutableStateFlow<Bitmap?>(null)

    init {
        cargarTareas()
    }

    fun cargarTareas() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val resp = api.listarMisAsignaciones()
                if (resp.isSuccessful && resp.body()?.data != null) {
                    tareasAsignadas.value = resp.body()!!.data!!
                }

                // Cargar también reportes cerrados para el historial
                val histResp = api.listarReportes(estado = "CERRADO")
                if (histResp.isSuccessful && histResp.body()?.data != null) {
                    tareasResueltas.value = histResp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al obtener tareas asignadas: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
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

    // RF06: Registrar avance operativo
    fun registrarAvance(reporteId: Long, observaciones: String, onSuccess: () -> Unit) {
        if (observaciones.isBlank()) {
            errorMessage.value = "Debe especificar las observaciones técnicas del avance."
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            try {
                val req = SeguimientoCreateRequest(
                    nuevoEstado = "EN_PROCESO",
                    accion = "AVANCE_OPERATIVO",
                    observaciones = observaciones
                )
                val resp = api.registrarAvance(reporteId, req)
                if (resp.isSuccessful) {
                    cargarTareas()
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
                    onSuccess()
                } else {
                    val errorText = resp.errorBody()?.string() ?: ""
                    // Si el backend disparó la Regla 3 por falta de avance previo:
                    if (errorText.contains("Regla de Negocio") || errorText.contains("acción de atención")) {
                        regla3Bloqueada.value = true
                        onRegla3Violation()
                    } else {
                        errorMessage.value = "Error al cerrar: ${resp.message()}"
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
