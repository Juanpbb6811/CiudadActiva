package com.ciudadactiva.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ciudadactiva.app.MainActivity
import com.ciudadactiva.app.R

object NotificationHelper {

    const val CANAL_REPORTES_ID = "canal_ciudadactiva_reportes"
    const val CANAL_REPORTES_NOMBRE = "Actualizaciones de Reportes Urbanos"

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_REPORTES_ID,
                CANAL_REPORTES_NOMBRE,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre avances y cambios de estado en tus reportes ciudadanos"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(canal)
        }
    }

    fun mostrarNotificacionEstado(
        context: Context,
        reporteId: Long,
        tituloReporte: String,
        nuevoEstado: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        crearCanalNotificaciones(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reporte_id", reporteId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reporteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val textoEstado = when (nuevoEstado.uppercase()) {
            "EN_PROCESO" -> "Una cuadrilla técnica ha comenzado las labores en campo."
            "ATENDIDO" -> "Las cuadrillas han solucionado la incidencia con evidencia."
            "CERRADO" -> "El caso ha sido verificado y cerrado satisfactoriamente."
            "RECHAZADO" -> "El reporte fue revisado con observaciones técnicas."
            else -> "El estado ha cambiado a $nuevoEstado."
        }

        val builder = NotificationCompat.Builder(context, CANAL_REPORTES_ID)
            .setSmallIcon(R.drawable.ic_logo_ciudadactiva)
            .setContentTitle("Actualización: $tituloReporte")
            .setContentText(textoEstado)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Tu reporte '$tituloReporte' ha sido actualizado a $nuevoEstado.\n$textoEstado"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(reporteId.toInt(), builder.build())
        } catch (_: SecurityException) {
            // Permiso no concedido
        }
    }
}
