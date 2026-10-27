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
        private const val KEY_BASE_URL = "base_url"

        // URL pública de producción en Render (accesible desde cualquier parte del mundo)
        const val DEFAULT_BASE_URL = "https://ciudadactiva-4ah7.onrender.com/"
    }

    fun saveAuth(token: String, id: Long, email: String?, nombre: String?, rol: String?, fotoUrl: String? = null) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, id)
            .putString(KEY_EMAIL, email ?: "")
            .putString(KEY_NOMBRE, nombre ?: "")
            .putString(KEY_ROL, rol ?: "")
            .putString(KEY_FOTO_URL, fotoUrl ?: "")
            .apply()
    }

    fun updateProfile(nombre: String, email: String, telefono: String?, direccion: String?, fotoUrl: String? = null) {
        prefs.edit()
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_EMAIL, email)
            .putString(KEY_TELEFONO, telefono ?: "")
            .putString(KEY_DIRECCION, direccion ?: "")
            .putString(KEY_FOTO_URL, fotoUrl ?: "")
            .apply()
    }

    fun getFotoUrl(): String? = prefs.getString(KEY_FOTO_URL, null)

    fun getTelefono(): String? = prefs.getString(KEY_TELEFONO, "3001234567")

    fun getDireccion(): String? = prefs.getString(KEY_DIRECCION, "Calle 13 # 8-45")

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
            .apply()
    }
}
