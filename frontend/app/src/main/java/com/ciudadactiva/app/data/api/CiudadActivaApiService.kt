package com.ciudadactiva.app.data.api

import com.ciudadactiva.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface CiudadActivaApiService {

    // ================== AUTENTICACIÓN Y PERFIL ==================
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/register")
    suspend fun registrar(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @GET("api/usuarios/me")
    suspend fun obtenerMiPerfil(): Response<ApiResponse<UsuarioModel>>

    @PUT("api/usuarios/me")
    suspend fun actualizarMiPerfil(@Body request: UsuarioUpdateRequest): Response<ApiResponse<UsuarioModel>>

    // ================== CATEGORÍAS ==================
    @GET("api/categorias")
    suspend fun listarCategorias(): Response<ApiResponse<List<CategoriaModel>>>

    @POST("api/categorias")
    suspend fun crearCategoria(@Body request: CategoriaCreateRequest): Response<ApiResponse<CategoriaModel>>

    @PUT("api/categorias/{id}")
    suspend fun actualizarCategoria(
        @Path("id") id: Long,
        @Body request: CategoriaCreateRequest
    ): Response<ApiResponse<CategoriaModel>>

    @DELETE("api/categorias/{id}")
    suspend fun eliminarCategoria(@Path("id") id: Long): Response<ApiResponse<Void>>

    // ================== REPORTES ==================
    @GET("api/reportes")
    suspend fun listarReportes(
        @Query("categoriaId") categoriaId: Long? = null,
        @Query("estado") estado: String? = null,
        @Query("prioridad") prioridad: String? = null,
        @Query("ciudadanoId") ciudadanoId: Long? = null
    ): Response<ApiResponse<List<ReporteResponse>>>

    @GET("api/reportes/mis-reportes")
    suspend fun listarMisReportes(): Response<ApiResponse<List<ReporteResponse>>>

    @GET("api/reportes/{id}")
    suspend fun obtenerDetalleReporte(@Path("id") id: Long): Response<ApiResponse<ReporteDetalleResponse>>

    @POST("api/reportes")
    suspend fun crearReporte(@Body request: ReporteCreateRequest): Response<ApiResponse<ReporteResponse>>

    @Multipart
    @POST("api/reportes/con-foto")
    suspend fun crearReporteConFoto(
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("categoriaId") categoriaId: RequestBody,
        @Part("latitud") latitud: RequestBody?,
        @Part("longitud") longitud: RequestBody?,
        @Part("direccion") direccion: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): Response<ApiResponse<ReporteResponse>>

    @Multipart
    @POST("api/reportes/{id}/foto")
    suspend fun subirFotoReporte(
        @Path("id") id: Long,
        @Part foto: MultipartBody.Part
    ): Response<ApiResponse<ReporteResponse>>

    // ================== GESTIÓN Y VALIDACIÓN (GESTOR / ADMIN) ==================
    @PATCH("api/reportes/{id}/validar")
    suspend fun validarReporte(
        @Path("id") id: Long,
        @Body request: ValidarReporteRequest
    ): Response<ApiResponse<ReporteResponse>>

    @PATCH("api/reportes/{id}/prioridad")
    suspend fun cambiarPrioridad(
        @Path("id") id: Long,
        @Body request: CambiarPrioridadRequest
    ): Response<ApiResponse<ReporteResponse>>

    @POST("api/reportes/{id}/asignaciones")
    suspend fun asignarOperador(
        @Path("id") id: Long,
        @Body request: AsignacionCreateRequest
    ): Response<ApiResponse<AsignacionModel>>

    @DELETE("api/reportes/{id}/asignaciones")
    suspend fun cancelarAsignacion(@Path("id") id: Long): Response<ApiResponse<Void>>

    @GET("api/asignaciones")
    suspend fun listarAsignacionesActivas(): Response<ApiResponse<List<AsignacionModel>>>

    @GET("api/operadores")
    suspend fun listarOperadores(): Response<ApiResponse<List<UsuarioModel>>>

    // ================== OPERACIONES DE CAMPO (OPERADOR) ==================
    @GET("api/asignaciones/mis-asignaciones")
    suspend fun listarMisAsignaciones(): Response<ApiResponse<List<AsignacionModel>>>

    @POST("api/reportes/{id}/seguimientos")
    suspend fun registrarAvance(
        @Path("id") id: Long,
        @Body request: SeguimientoCreateRequest
    ): Response<ApiResponse<SeguimientoModel>>

    @GET("api/reportes/{id}/seguimiento")
    suspend fun obtenerLíneaTiempo(@Path("id") id: Long): Response<ApiResponse<List<SeguimientoModel>>>

    @PATCH("api/reportes/{id}/cerrar")
    suspend fun cerrarReporte(
        @Path("id") id: Long,
        @Body request: CerrarReporteRequest
    ): Response<ApiResponse<ReporteResponse>>

    @Multipart
    @POST("api/reportes/{id}/foto-solucion")
    suspend fun subirFotoSolucion(
        @Path("id") id: Long,
        @Part foto: MultipartBody.Part,
        @Part("observaciones") observaciones: RequestBody? = null
    ): Response<ApiResponse<ReporteResponse>>

    // ================== COMENTARIOS ==================
    @GET("api/reportes/{id}/comentarios")
    suspend fun listarComentarios(@Path("id") id: Long): Response<ApiResponse<List<ComentarioModel>>>

    @POST("api/reportes/{id}/comentarios")
    suspend fun agregarComentario(
        @Path("id") id: Long,
        @Body request: ComentarioCreateRequest
    ): Response<ApiResponse<ComentarioModel>>

    @DELETE("api/comentarios/{id}")
    suspend fun eliminarComentario(@Path("id") id: Long): Response<ApiResponse<Void>>

    // ================== ESTADÍSTICAS Y SEMÁFORO ==================
    @GET("api/estadisticas/reportes")
    suspend fun obtenerEstadisticas(): Response<ApiResponse<EstadisticasModel>>

    @GET("api/estadisticas/semaforo/calcular")
    suspend fun calcularSemaforo(@Query("cantidad") cantidad: Int): Response<ApiResponse<SemaforoModel>>

    // ================== ADMINISTRACIÓN (ADMIN) ==================
    @GET("api/admin/usuarios")
    suspend fun listarTodosLosUsuarios(@Query("rol") rol: String? = null): Response<ApiResponse<List<UsuarioModel>>>

    @POST("api/admin/usuarios")
    suspend fun crearUsuarioAdmin(@Body request: UsuarioCreateAdminRequest): Response<ApiResponse<UsuarioModel>>

    @PUT("api/admin/usuarios/{id}")
    suspend fun actualizarUsuarioAdmin(
        @Path("id") id: Long,
        @Body request: UsuarioUpdateRequest
    ): Response<ApiResponse<UsuarioModel>>

    @DELETE("api/admin/usuarios/{id}")
    suspend fun desactivarUsuarioAdmin(@Path("id") id: Long): Response<ApiResponse<Void>>

    @GET("api/admin/auditoria")
    suspend fun consultarAuditoriaRUP(): Response<ApiResponse<List<SeguimientoModel>>>
}
