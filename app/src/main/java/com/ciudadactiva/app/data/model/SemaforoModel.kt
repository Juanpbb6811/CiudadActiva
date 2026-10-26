package com.ciudadactiva.app.data.model

import androidx.compose.ui.graphics.Color

data class SemaforoModel(
    val cantidadReportes: Int = 0,
    val colorHex: String = "#4CAF50",
    val colorNombre: String = "VERDE",
    val nivelAlerta: String = "SIN_REPORTES",
    val descripcion: String = "Sin incidencias",
    val porcentajeSaturacion: Double = 0.0
) {
    fun toComposeColor(): Color {
        return try {
            val cleanHex = colorHex.replace("#", "")
            val parsed = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(parsed or 0x00000000FF000000)
            } else {
                Color(parsed)
            }
        } catch (e: Exception) {
            Color(0xFF4CAF50)
        }
    }

    companion object {
        fun calcularLocal(cantidad: Int): SemaforoModel {
            return when {
                cantidad <= 0 -> SemaforoModel(0, "#4CAF50", "VERDE", "SIN_REPORTES", "Sin incidencias", 0.0)
                cantidad <= 2 -> SemaforoModel(cantidad, "#4CAF50", "VERDE", "BAJO", "1 a 2 reportes: Nivel bajo controlado", cantidad * 10.0)
                cantidad <= 4 -> SemaforoModel(cantidad, "#8BC34A", "VERDE_LIMA", "LEVE", "3 a 4 reportes: Nivel leve", cantidad * 10.0)
                cantidad <= 6 -> SemaforoModel(cantidad, "#FFEB3B", "AMARILLO", "MODERADO", "5 a 6 reportes: Alerta preventiva", cantidad * 10.0)
                cantidad <= 8 -> SemaforoModel(cantidad, "#FF9800", "NARANJA", "ALTO", "7 a 8 reportes: Atención urgente", cantidad * 10.0)
                else -> SemaforoModel(cantidad, "#F44336", "ROJO", "CRITICO", "10 o más: Nivel crítico saturado", 100.0)
            }
        }
    }
}
