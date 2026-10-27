package com.ciudadactiva.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberCameraLauncher(
    onPhotoTaken: (Bitmap) -> Unit,
    onError: ((String) -> Unit)? = null
): () -> Unit {
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onPhotoTaken(bitmap)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch()
            } catch (e: Exception) {
                val msg = "No se pudo iniciar la cámara: ${e.localizedMessage ?: "Error desconocido"}"
                onError?.invoke(msg) ?: Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            val msg = "Se requiere permiso de cámara para capturar la fotografía"
            onError?.invoke(msg) ?: Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    return remember(cameraLauncher, permissionLauncher, context) {
        {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                try {
                    cameraLauncher.launch()
                } catch (e: Exception) {
                    val msg = "Error al abrir la cámara: ${e.localizedMessage ?: "Dispositivo sin app de cámara"}"
                    onError?.invoke(msg) ?: Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
