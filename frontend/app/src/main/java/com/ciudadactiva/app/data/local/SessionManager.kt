package com.ciudadactiva.app.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ciudadactiva_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NOMBRE = "user_name"
        private const val KEY_ROL = "user_role"
        private const val KEY_TELEFONO = "user_phone"
        private const val KEY_DIRECCION = "user_address"
        private const val KEY_FOTO_URL = "user_photo_url"
        private const val KEY_TIPO_DOC = "user_tipo_doc"
        private const val KEY_NUM_DOC = "user_num_doc"
        private const val KEY_PRIMER_NOMBRE = "user_primer_nombre"
        private const val KEY_SEGUNDO_NOMBRE = "user_segundo_nombre"
        private const val KEY_PRIMER_APELLIDO = "user_primer_apellido"
        private const val KEY_SEGUNDO_APELLIDO = "user_segundo_apellido"
        private const val KEY_GOOGLE_ID = "user_google_id"
        private const val KEY_DATOS_LEGALES_COMPLETOS = "user_datos_legales_completos"
        private const val KEY_BASE_URL = "base_url"

        // URL pública de producción en Render (accesible desde cualquier parte del mundo)
        const val DEFAULT_BASE_URL = "https://ciudadactiva-4ah7.onrender.com/"
    }

    fun saveAuth(
        token: String,
        id: Long,
        email: String?,
        nombre: String?,
        rol: String?,
        fotoUrl: String? = null,
        telefono: String? = null,
        direccion: String? = null,
        tipoDocumento: String? = null,
        numeroDocumento: String? = null,
        primerNombre: String? = null,
        primerApellido: String? = null,
        googleId: String? = null,
        datosLegalesCompletos: Boolean = false
    ) {
        val editor = prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, id)
            .putString(KEY_EMAIL, email ?: "")
            .putString(KEY_NOMBRE, nombre ?: "")
            .putString(KEY_ROL, rol ?: "")
            .putString(KEY_FOTO_URL, fotoUrl ?: "")
            .putString(KEY_TIPO_DOC, tipoDocumento ?: "CC")
            .putString(KEY_NUM_DOC, numeroDocumento ?: "")
            .putString(KEY_PRIMER_NOMBRE, primerNombre ?: "")
            .putString(KEY_PRIMER_APELLIDO, primerApellido ?: "")
            .putString(KEY_GOOGLE_ID, googleId ?: "")
            .putBoolean(KEY_DATOS_LEGALES_COMPLETOS, datosLegalesCompletos)

        if (telefono != null) {
            editor.putString(KEY_TELEFONO, telefono)
        } else {
            editor.remove(KEY_TELEFONO)
        }

        if (direccion != null) {
            editor.putString(KEY_DIRECCION, direccion)
        } else {
            editor.remove(KEY_DIRECCION)
        }

        editor.apply()
    }

    fun updateProfile(
        nombre: String,
        email: String,
        telefono: String?,
        direccion: String?,
        fotoUrl: String? = null,
        tipoDocumento: String? = null,
        numeroDocumento: String? = null,
        googleId: String? = null
    ) {
        val editor = prefs.edit()
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_EMAIL, email)
            .putString(KEY_FOTO_URL, fotoUrl ?: "")

        if (tipoDocumento != null) editor.putString(KEY_TIPO_DOC, tipoDocumento)
        if (numeroDocumento != null) editor.putString(KEY_NUM_DOC, numeroDocumento)
        if (googleId != null) editor.putString(KEY_GOOGLE_ID, googleId)

        if (telefono != null) {
            editor.putString(KEY_TELEFONO, telefono)
        } else {
            editor.remove(KEY_TELEFONO)
        }

        if (direccion != null) {
            editor.putString(KEY_DIRECCION, direccion)
        } else {
            editor.remove(KEY_DIRECCION)
        }

        editor.apply()
    }

    fun getFotoUrl(): String? = prefs.getString(KEY_FOTO_URL, null)?.takeIf { it.isNotBlank() }
    fun getTelefono(): String? = prefs.getString(KEY_TELEFONO, null)?.takeIf { it.isNotBlank() }
    fun getDireccion(): String? = prefs.getString(KEY_DIRECCION, null)?.takeIf { it.isNotBlank() }
    fun getTipoDocumento(): String = prefs.getString(KEY_TIPO_DOC, "CC") ?: "CC"
    fun getNumeroDocumento(): String? = prefs.getString(KEY_NUM_DOC, null)?.takeIf { it.isNotBlank() }
    fun getGoogleId(): String? = prefs.getString(KEY_GOOGLE_ID, null)?.takeIf { it.isNotBlank() }
    fun isGoogleLinked(): Boolean = !getGoogleId().isNullOrBlank()

    fun areDatosLegalesCompletos(): Boolean {
        val rol = getRol()
        val email = getEmail() ?: ""
        // Si es cuenta de prueba de Ana Ciudadana o staff, siempre completo
        if (email.contains("ciudadano@ciudadactiva.gov.co")) return true
        if (rol == "ROLE_ADMIN" || rol == "ROLE_GESTOR" || rol == "ROLE_OPERADOR") return true
        
        val doc = getNumeroDocumento()
        val tel = getTelefono()
        val dir = getDireccion()
        val nom = getNombre()
        return !doc.isNullOrBlank() && !doc.contains("PENDIENTE") && !tel.isNullOrBlank() && !dir.isNullOrBlank() && !nom.isNullOrBlank()
    }

    fun setDatosLegalesCompletos(completos: Boolean) {
        prefs.edit().putBoolean(KEY_DATOS_LEGALES_COMPLETOS, completos).apply()
    }

    fun setGoogleId(googleId: String?) {
        prefs.edit().putString(KEY_GOOGLE_ID, googleId ?: "").apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getNombre(): String? = prefs.getString(KEY_NOMBRE, null)
    fun getRol(): String? = prefs.getString(KEY_ROL, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun getBaseUrl(): String {
        val saved = prefs.getString(KEY_BASE_URL, null)
        if (saved.isNullOrBlank() || saved.contains("192.168.1.22") || saved.contains("ciudadactiva-backend.onrender.com")) {
            setBaseUrl(DEFAULT_BASE_URL)
            return DEFAULT_BASE_URL
        }
        return saved
    }

    fun setBaseUrl(url: String) {
        val cleanUrl = if (!url.endsWith("/")) "$url/" else url
        prefs.edit().putString(KEY_BASE_URL, cleanUrl).apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_NOMBRE)
            .remove(KEY_ROL)
            .remove(KEY_TELEFONO)
            .remove(KEY_DIRECCION)
            .remove(KEY_FOTO_URL)
            .remove(KEY_TIPO_DOC)
            .remove(KEY_NUM_DOC)
            .remove(KEY_PRIMER_NOMBRE)
            .remove(KEY_PRIMER_APELLIDO)
            .remove(KEY_GOOGLE_ID)
            .remove(KEY_DATOS_LEGALES_COMPLETOS)
            .apply()
    }
}
