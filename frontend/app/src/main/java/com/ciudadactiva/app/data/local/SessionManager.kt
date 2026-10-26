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
        private const val KEY_BASE_URL = "base_url"

        // IP por defecto para Emulador Android apuntando al Backend Spring Boot local en Windows
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/"
    }

    fun saveAuth(token: String, id: Long, email: String, nombre: String, rol: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, id)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_ROL, rol)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getNombre(): String? = prefs.getString(KEY_NOMBRE, null)

    fun getRol(): String? = prefs.getString(KEY_ROL, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun getBaseUrl(): String = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

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
