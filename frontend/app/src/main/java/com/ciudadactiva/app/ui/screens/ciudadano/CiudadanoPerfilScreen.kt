package com.ciudadactiva.app.ui.screens.ciudadano

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.data.model.UsuarioUpdateRequest
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun CiudadanoPerfilScreen(
    onCerrarSesion: () -> Unit
) {
    val session = CiudadActivaApplication.sessionManager
    val coroutineScope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf(session.getNombre() ?: "Ana Ciudadana") }
    var email by remember { mutableStateOf(session.getEmail() ?: "ciudadano@ciudadactiva.gov.co") }
    var telefono by remember { mutableStateOf(session.getTelefono() ?: "3001234567") }
    var direccion by remember { mutableStateOf(session.getDireccion() ?: "Calle 13 # 8-45") }
    var fotoUrl by remember { mutableStateOf(session.getFotoUrl()) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val rol = session.getRol() ?: "ROLE_CIUDADANO"

    // Launcher para tomar foto con la cámara del celular
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            avatarBitmap = bitmap
            // Convertir a base64
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val base64 = "data:image/jpeg;base64," + Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            fotoUrl = base64
        }
    }

    // Cargar perfil actual desde backend al inicio
    LaunchedEffect(Unit) {
        try {
            val response = ApiClient.getService().obtenerMiPerfil()
            if (response.isSuccessful && response.body()?.data != null) {
                val u = response.body()!!.data!!
                nombre = u.nombre
                email = u.email
                u.telefono?.let { telefono = it }
                u.direccion?.let { direccion = it }
                u.fotoUrl?.let { fotoUrl = it }
                session.updateProfile(u.nombre, u.email, u.telefono, u.direccion, u.fotoUrl)
            }
        } catch (e: Exception) {
            // Ignorar y usar valores locales en cache
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        CiudadActivaTopBar(title = "Mi Perfil")

        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar con botón de cámara integrado
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(BrandTealDark)
                        .border(3.dp, BrandCopper, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap!!.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!fotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initials = nombre.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
                        Text(
                            text = if (initials.isNotBlank()) initials else "U",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Botón flotante para cambiar foto
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BrandCopper)
                        .clickable { cameraLauncher.launch(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar Foto",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = nombre,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTealDark
            )

            Text(
                text = email,
                fontSize = 13.sp,
                color = Color(0xFF6C757D)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0F2F1))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = rol.replace("ROLE_", "PERFIL: "),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00695C)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Formulario de Modificación de Datos Personales (Nombre, Correo, Teléfono, Dirección)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Datos Personales",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandTealDark
                    )

                    // Nombre Completo
                    Column {
                        Text("Nombre Completo *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                nombre = it
                                mensajeExito = null
                                mensajeError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandTealDark) }
                        )
                    }

                    // Correo Electrónico
                    Column {
                        Text("Correo Electrónico *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                mensajeExito = null
                                mensajeError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandTealDark) }
                        )
                    }

                    // Teléfono
                    Column {
                        Text("Número de Teléfono", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = {
                                telefono = it
                                mensajeExito = null
                                mensajeError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandTealDark) }
                        )
                    }

                    // Dirección de Residencia
                    Column {
                        Text("Dirección de Residencia", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = {
                                direccion = it
                                mensajeExito = null
                                mensajeError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = BrandTealDark) }
                        )
                    }

                    // Botón para actualizar la foto del perfil
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar Foto para el Perfil", fontSize = 12.sp, color = BrandTealDark, fontWeight = FontWeight.Bold)
                    }

                    // Mensajes de éxito o error
                    if (mensajeExito != null) {
                        Text(
                            text = mensajeExito!!,
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (mensajeError != null) {
                        Text(
                            text = mensajeError!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón Guardar Cambios
                    Button(
                        onClick = {
                            if (nombre.isBlank() || email.isBlank()) {
                                mensajeError = "El nombre y el correo no pueden estar vacíos"
                                return@Button
                            }
                            isSaving = true
                            mensajeExito = null
                            mensajeError = null
                            coroutineScope.launch {
                                try {
                                    val req = UsuarioUpdateRequest(
                                        nombre = nombre.trim(),
                                        email = email.trim(),
                                        telefono = telefono.trim(),
                                        direccion = direccion.trim(),
                                        fotoUrl = fotoUrl
                                    )
                                    val resp = ApiClient.getService().actualizarMiPerfil(req)
                                    if (resp.isSuccessful && resp.body()?.data != null) {
                                        val u = resp.body()!!.data!!
                                        session.updateProfile(u.nombre, u.email, u.telefono, u.direccion, u.fotoUrl)
                                        mensajeExito = "✓ Perfil actualizado exitosamente en el servidor"
                                    } else {
                                        mensajeError = "No se pudo actualizar: " + (resp.errorBody()?.string() ?: "Error de validación")
                                    }
                                } catch (e: Exception) {
                                    // Guardar en sesión local como fallback
                                    session.updateProfile(nombre, email, telefono, direccion, fotoUrl)
                                    mensajeExito = "✓ Perfil actualizado localmente"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar Cambios de Perfil", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Cerrar Sesión
            Button(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
