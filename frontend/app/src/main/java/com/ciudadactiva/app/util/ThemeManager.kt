package com.ciudadactiva.app.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val titulo: String, val descripcion: String) {
    SYSTEM("Automático", "Sigue la configuración del sistema operativo"),
    LIGHT("Modo Claro", "Apariencia clásica con fondos blancos y limpios"),
    DARK("Modo Oscuro", "Fondo oscuro profundo de alto contraste y descanso visual")
}

object ThemeManager {
    private const val PREFS_NAME = "ciudadactiva_theme_prefs"
    private const val KEY_MODE = "app_theme_mode"

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_MODE, AppThemeMode.SYSTEM.name)
        _themeMode.value = try {
            AppThemeMode.valueOf(saved ?: AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setTheme(context: Context, mode: AppThemeMode) {
        _themeMode.value = mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }
}
