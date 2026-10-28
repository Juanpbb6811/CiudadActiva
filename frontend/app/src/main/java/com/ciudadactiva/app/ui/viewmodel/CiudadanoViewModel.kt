package com.ciudadactiva.app.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.data.model.*
import com.ciudadactiva.app.ui.components.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class CiudadanoViewModel : ViewModel() {

    private val api = ApiClient.getService()

    // Estados de pantalla
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Datos Home Ciudadano
    val categorias = MutableStateFlow<List<CategoriaModel>>(emptyList())
    val misReportes = MutableStateFlow<List<ReporteResponse>>(emptyList())
    val reporteActivo = MutableStateFlow<ReporteResponse?>(null)
    val reportesCercanos = MutableStateFlow<List<ReporteResponse>>(emptyList())

    // Wizard Nuevo Reporte (3 Pasos)
    val nuevoReporteCategoria = MutableStateFlow<CategoriaModel?>(null)
    val nuevoReporteTitulo = MutableStateFlow("")
    val nuevoReporteDescripcion = MutableStateFlow("")
    val nuevoReporteUrgencia = MutableStateFlow("Media") // Media, Alta, Muy Urgente

    val nuevoReporteDireccion = MutableStateFlow("")
    val nuevoReporteCiudad = MutableStateFlow("Bogotá D.C.")
    val nuevoReporteDepartamento = MutableStateFlow("Cundinamarca")
    val nuevoReporteBarrio = MutableStateFlow("Chapinero")
    val isGeocoding = MutableStateFlow(false)
    val nuevoReporteLatitud = MutableStateFlow(4.609712)
    val nuevoReporteLongitud = MutableStateFlow(-74.081750)

    val fotoTomadaBitmap = MutableStateFlow<Bitmap?>(null)
    val fotoNombreArchivo = MutableStateFlow<String?>(null)
    val nuevoReporteCreado = MutableStateFlow<ReporteResponse?>(null)

    // Detalle / Seguimiento
    val reporteDetalle = MutableStateFlow<ReporteDetalleResponse?>(null)
    val comentariosList = MutableStateFlow<List<ComentarioModel>>(emptyList())

    init {
        cargarDatosHome()
    }

    fun cargarDatosHome() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                // Categorías con semáforos
                val catResp = api.listarCategorias()
                if (catResp.isSuccessful && catResp.body()?.data != null) {
                    categorias.value = catResp.body()!!.data!!
                    if (nuevoReporteCategoria.value == null && categorias.value.isNotEmpty()) {
                        nuevoReporteCategoria.value = categorias.value.first()
                    }
                }

                // Mis reportes
                val misResp = api.listarMisReportes()
                if (misResp.isSuccessful && misResp.body()?.data != null) {
                    val lista = misResp.body()!!.data!!
                    misReportes.value = lista
                    reporteActivo.value = lista.firstOrNull { it.estado != "CERRADO" && it.estado != "RECHAZADO" }
                }

                // Reportes generales para mapa y cerca de ti
                val genResp = api.listarReportes()
                if (genResp.isSuccessful && genResp.body()?.data != null) {
                    reportesCercanos.value = genResp.body()!!.data!!
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al sincronizar datos: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun setFotoCamara(bitmap: Bitmap) {
        fotoTomadaBitmap.value = bitmap
        fotoNombreArchivo.value = "evidencia_camara_${System.currentTimeMillis()}.jpg"
    }

    fun setFotoGaleria(bitmap: Bitmap) {
        fotoTomadaBitmap.value = bitmap
        fotoNombreArchivo.value = "evidencia_galeria_${System.currentTimeMillis()}.jpg"
    }

    fun eliminarFoto() {
        fotoTomadaBitmap.value = null
        fotoNombreArchivo.value = null
    }

    fun actualizarUbicacionDesdeCoordenadas(context: Context, lat: Double, lng: Double) {
        nuevoReporteLatitud.value = lat
        nuevoReporteLongitud.value = lng
        viewModelScope.launch {
            isGeocoding.value = true
            try {
                val info = LocationHelper.reverseGeocode(context, lat, lng)
                nuevoReporteBarrio.value = info.barrio
                nuevoReporteCiudad.value = info.ciudad
                nuevoReporteDepartamento.value = info.departamento
            } catch (_: Exception) {
            } finally {
                isGeocoding.value = false
            }
        }
    }

    fun enviarReporte(onSuccess: (ReporteResponse) -> Unit) {
        val cat = nuevoReporteCategoria.value ?: return
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val fotoBitmap = fotoTomadaBitmap.value
                val response = if (fotoBitmap != null) {
                    // Envío Multipart con imagen tomada desde la cámara del celular
                    val stream = ByteArrayOutputStream()
                    fotoBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val byteArray = stream.toByteArray()

                    val reqFoto = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val partFoto = MultipartBody.Part.createFormData("foto", fotoNombreArchivo.value ?: "foto.jpg", reqFoto)

                    val reqTitulo = nuevoReporteTitulo.value.toRequestBody(MultipartBody.FORM)
                    val reqDesc = nuevoReporteDescripcion.value.toRequestBody(MultipartBody.FORM)
                    val reqCatId = cat.id.toString().toRequestBody(MultipartBody.FORM)
                    val reqLat = nuevoReporteLatitud.value.toString().toRequestBody(MultipartBody.FORM)
                    val reqLng = nuevoReporteLongitud.value.toString().toRequestBody(MultipartBody.FORM)
                    val reqDir = nuevoReporteDireccion.value.takeIf { it.isNotBlank() }?.toRequestBody(MultipartBody.FORM)
                    val reqCiudad = nuevoReporteCiudad.value.toRequestBody(MultipartBody.FORM)
                    val reqDepto = nuevoReporteDepartamento.value.toRequestBody(MultipartBody.FORM)
                    val reqBarrio = nuevoReporteBarrio.value.toRequestBody(MultipartBody.FORM)

                    api.crearReporteConFoto(reqTitulo, reqDesc, reqCatId, reqLat, reqLng, reqDir, reqCiudad, reqDepto, reqBarrio, partFoto)
                } else {
                    // Envío estándar JSON
                    val body = ReporteCreateRequest(
                        titulo = nuevoReporteTitulo.value,
                        descripcion = nuevoReporteDescripcion.value,
                        categoriaId = cat.id,
                        latitud = nuevoReporteLatitud.value,
                        longitud = nuevoReporteLongitud.value,
                        direccion = nuevoReporteDireccion.value.takeIf { it.isNotBlank() },
                        ciudad = nuevoReporteCiudad.value,
                        departamento = nuevoReporteDepartamento.value,
                        barrio = nuevoReporteBarrio.value
                    )
                    api.crearReporte(body)
                }

                if (response.isSuccessful && response.body()?.data != null) {
                    val guardado = response.body()!!.data!!
                    nuevoReporteCreado.value = guardado
                    // Limpiar formulario
                    nuevoReporteTitulo.value = ""
                    nuevoReporteDescripcion.value = ""
                    nuevoReporteDireccion.value = ""
                    nuevoReporteBarrio.value = "Chapinero"
                    nuevoReporteCiudad.value = "Bogotá D.C."
                    nuevoReporteDepartamento.value = "Cundinamarca"
                    fotoTomadaBitmap.value = null
                    cargarDatosHome()
                    onSuccess(guardado)
                } else {
                    errorMessage.value = "No se pudo registrar la incidencia: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Fallo de comunicación: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun cargarDetalleReporte(reporteId: Long) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val resp = api.obtenerDetalleReporte(reporteId)
                if (resp.isSuccessful && resp.body()?.data != null) {
                    reporteDetalle.value = resp.body()!!.data!!
                    comentariosList.value = reporteDetalle.value!!.comentarios
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }

    fun agregarComentario(reporteId: Long, texto: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            try {
                val resp = api.agregarComentario(reporteId, ComentarioCreateRequest(texto))
                if (resp.isSuccessful) {
                    cargarDetalleReporte(reporteId)
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al enviar comentario: ${e.localizedMessage}"
            }
        }
    }

    fun eliminarComentario(reporteId: Long, comentarioId: Long) {
        viewModelScope.launch {
            try {
                val resp = api.eliminarComentario(comentarioId)
                if (resp.isSuccessful) {
                    cargarDetalleReporte(reporteId)
                }
            } catch (e: Exception) {
                errorMessage.value = "No se pudo eliminar el comentario: ${e.localizedMessage}"
            }
        }
    }

    fun eliminarReporte(
        reporteId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val resp = api.eliminarReporte(reporteId)
                if (resp.isSuccessful) {
                    cargarDatosHome()
                    onSuccess()
                } else {
                    val err = resp.errorBody()?.string() ?: ""
                    val msg = if (err.contains("PENDIENTE")) {
                        "Solo se pueden retirar reportes en estado PENDIENTE."
                    } else {
                        "No se pudo retirar el reporte."
                    }
                    errorMessage.value = msg
                    onError(msg)
                }
            } catch (e: Exception) {
                val msg = "Error de conexión: ${e.localizedMessage}"
                errorMessage.value = msg
                onError(msg)
            } finally {
                isLoading.value = false
            }
        }
    }
}
