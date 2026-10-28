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
import com.ciudadactiva.app.data.local.DemoCredentials
import com.ciudadactiva.app.data.model.LoginRequest
import com.ciudadactiva.app.data.model.UsuarioUpdateRequest
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.components.rememberCameraLauncher
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.screens.auth.ModalCambiarPasswordPerfil
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun CiudadanoPerfilScreen(
    onCerrarSesion: () -> Unit,
    onCambiarActor: (String) -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    val session = CiudadActivaApplication.sessionManager
    val coroutineScope = rememberCoroutineScope()

    // Estado de modo edición: FALSO por defecto (solo lectura inicial)
    var isEditing by remember { mutableStateOf(false) }

    // Valores en edición
    var nombre by remember { mutableStateOf(session.getNombre() ?: "") }
    var email by remember { mutableStateOf(session.getEmail() ?: "") }
    var telefono by remember { mutableStateOf(session.getTelefono() ?: "") }
    var direccion by remember { mutableStateOf(session.getDireccion() ?: "") }
    var fotoUrl by remember { mutableStateOf(session.getFotoUrl()) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Backup para botón Cancelar
    var backupNombre by remember { mutableStateOf(nombre) }
    var backupEmail by remember { mutableStateOf(email) }
    var backupTelefono by remember { mutableStateOf(telefono) }
    var backupDireccion by remember { mutableStateOf(direccion) }
    var backupFotoUrl by remember { mutableStateOf(fotoUrl) }

    var isSaving by remember { mutableStateOf(false) }
    var isSwitchingActor by remember { mutableStateOf(false) }
    var showCambiarPassDialog by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val rol = session.getRol() ?: "ROLE_CIUDADANO"

    // Launcher seguro para tomar foto de perfil con verificación de permisos en tiempo de ejecución
    val tomarFotoPerfil = rememberCameraLauncher(
        onPhotoTaken = { bitmap ->
            avatarBitmap = bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val base64 = "data:image/jpeg;base64," + Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            fotoUrl = base64
        }
    )

    // Cargar perfil actual desde backend al inicio
    LaunchedEffect(Unit) {
        try {
            val response = ApiClient.getService().obtenerMiPerfil()
            if (response.isSuccessful && response.body()?.data != null) {
                val u = response.body()!!.data!!
                nombre = u.nombre ?: ""
                email = u.email ?: ""
                telefono = u.telefono ?: ""
                direccion = u.direccion ?: ""
                fotoUrl = u.fotoUrl

                backupNombre = nombre
                backupEmail = email
                backupTelefono = telefono
                backupDireccion = direccion
                backupFotoUrl = fotoUrl

                session.updateProfile(u.nombre ?: "", u.email ?: "", u.telefono, u.direccion, u.fotoUrl)
            }
        } catch (_: Exception) {
            // Ignorar y usar valores locales
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        CiudadActivaTopBar(title = "Mi Perfil", onBackClick = onBackClick)

        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar con botón de cámara (activo en modo edición)
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
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
                        val initials = nombre.split(" ")
                            .mapNotNull { it.firstOrNull() }
                            .take(2)
                            .joinToString("")
                        Text(
                            text = if (initials.isNotBlank()) initials else "U",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Botón circular para cambiar foto
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(BrandCopper)
                        .clickable {
                            if (!isEditing) isEditing = true
                            tomarFotoPerfil()
                        },
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

            Spacer(modifier = Modifier.height(8.dp))

            // Botón con texto explícito "Tomar Foto"
            OutlinedButton(
                onClick = {
                    if (!isEditing) isEditing = true
                    tomarFotoPerfil()
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = BrandTealDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandTealDark.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tomar Foto", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
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

            // Tarjeta de Datos Personales
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Datos Personales",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTealDark
                        )

                        if (!isEditing) {
                            TextButton(
                                onClick = {
                                    backupNombre = nombre
                                    backupEmail = email
                                    backupTelefono = telefono
                                    backupDireccion = direccion
                                    backupFotoUrl = fotoUrl
                                    mensajeExito = null
                                    mensajeError = null
                                    isEditing = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = BrandCopper, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar", color = BrandCopper, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Nombre Completo
                    Column {
                        Text("Nombre Completo *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                if (isEditing) {
                                    nombre = it
                                    mensajeExito = null
                                    mensajeError = null
                                }
                            },
                            readOnly = !isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (isEditing) BrandTealDark else Color.Gray) },
                            colors = if (!isEditing) TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9FBFA),
                                unfocusedContainerColor = Color(0xFFF9FBFA),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color(0xFFE5EBEA)
                            ) else TextFieldDefaults.colors()
                        )
                    }

                    // Correo Electrónico
                    Column {
                        Text("Correo Electrónico *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                if (isEditing) {
                                    email = it
                                    mensajeExito = null
                                    mensajeError = null
                                }
                            },
                            readOnly = !isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (isEditing) BrandTealDark else Color.Gray) },
                            colors = if (!isEditing) TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9FBFA),
                                unfocusedContainerColor = Color(0xFFF9FBFA),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color(0xFFE5EBEA)
                            ) else TextFieldDefaults.colors()
                        )
                    }

                    // Teléfono
                    Column {
                        Text("Número de Teléfono", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = {
                                if (isEditing) {
                                    telefono = it
                                    mensajeExito = null
                                    mensajeError = null
                                }
                            },
                            readOnly = !isEditing,
                            placeholder = { Text("Sin registrar (Opcional)", color = Color.Gray, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = if (isEditing) BrandTealDark else Color.Gray) },
                            colors = if (!isEditing) TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9FBFA),
                                unfocusedContainerColor = Color(0xFFF9FBFA),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color(0xFFE5EBEA)
                            ) else TextFieldDefaults.colors()
                        )
                    }

                    // Dirección de Residencia
                    Column {
                        Text("Dirección de Residencia", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = {
                                if (isEditing) {
                                    direccion = it
                                    mensajeExito = null
                                    mensajeError = null
                                }
                            },
                            readOnly = !isEditing,
                            placeholder = { Text("Sin registrar (Opcional)", color = Color.Gray, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = if (isEditing) BrandTealDark else Color.Gray) },
                            colors = if (!isEditing) TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9FBFA),
                                unfocusedContainerColor = Color(0xFFF9FBFA),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color(0xFFE5EBEA)
                            ) else TextFieldDefaults.colors()
                        )
                    }

                    // Botón para tomar foto con la cámara (visible solo si está en modo edición)
                    if (isEditing) {
                        OutlinedButton(
                            onClick = { tomarFotoPerfil() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tomar Foto para el Perfil", fontSize = 12.sp, color = BrandTealDark, fontWeight = FontWeight.Bold)
                        }
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

                    // Botones de acción
                    if (isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Botón Cancelar
                            OutlinedButton(
                                onClick = {
                                    // Restaurar valores previos
                                    nombre = backupNombre
                                    email = backupEmail
                                    telefono = backupTelefono
                                    direccion = backupDireccion
                                    fotoUrl = backupFotoUrl
                                    avatarBitmap = null
                                    mensajeError = null
                                    mensajeExito = null
                                    isEditing = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isSaving
                            ) {
                                Text("Cancelar", color = Color(0xFF5A6665), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Botón Guardar Cambios (Con texto blanco visible)
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
                                                backupNombre = u.nombre
                                                backupEmail = u.email
                                                backupTelefono = u.telefono ?: ""
                                                backupDireccion = u.direccion ?: ""
                                                backupFotoUrl = u.fotoUrl
                                                mensajeExito = "✓ Perfil guardado con éxito"
                                                isEditing = false
                                            } else {
                                                mensajeError = "Error: " + (resp.errorBody()?.string() ?: "No se pudo actualizar")
                                            }
                                        } catch (e: Exception) {
                                            session.updateProfile(nombre, email, telefono, direccion, fotoUrl)
                                            mensajeExito = "✓ Guardado localmente"
                                            isEditing = false
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandTealDark,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Guardar",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // En modo solo lectura: botón para habilitar edición
                        OutlinedButton(
                            onClick = {
                                backupNombre = nombre
                                backupEmail = email
                                backupTelefono = telefono
                                backupDireccion = direccion
                                backupFotoUrl = fotoUrl
                                mensajeExito = null
                                mensajeError = null
                                isEditing = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTealDark)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Modificar Datos Personales", color = BrandTealDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { showCambiarPassDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00695C))
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF00695C), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cambiar Contraseña de Acceso", color = Color(0xFF00695C), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tarjeta de Cambio de Rol / Actor (Metodología RUP) - Exclusiva para Cuentas Demo Oficiales
            val emailActual = session.getEmail()?.lowercase()?.trim() ?: ""
            val esCuentaDemo = emailActual in listOf(
                "ciudadano@ciudadactiva.gov.co",
                "gestor@ciudadactiva.gov.co",
                "operador@ciudadactiva.gov.co",
                "admin@ciudadactiva.gov.co"
            )

            if (esCuentaDemo) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Cambiar de Rol / Actor",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTealDark
                                )
                                Text(
                                    text = "Alterna entre los 4 perfiles del sistema",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6C757D)
                                )
                            }

                            if (isSwitchingActor) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = BrandTealDark
                                )
                            }
                        }

                        // Botones de los 4 actores
                        val actores = listOf(
                            Triple("ROLE_CIUDADANO", "Ciudadano", "Reportar y seguir incidencias"),
                            Triple("ROLE_GESTOR", "Gestor", "Triage, validar y priorizar"),
                            Triple("ROLE_OPERADOR", "Operador", "Órdenes de campo y avances"),
                            Triple("ROLE_ADMIN", "Administrador", "Panel global y auditoría")
                        )

                        actores.forEach { (rolActor, titulo, desc) ->
                            val esRolActual = rol == rolActor
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (esRolActual) 1.5.dp else 1.dp,
                                        color = if (esRolActual) BrandTealDark else Color(0xFFE5EBEA),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = !isSwitchingActor && !esRolActual) {
                                        val acc = DemoCredentials.accounts.firstOrNull { it.rolEnum == rolActor }
                                        if (acc != null) {
                                            isSwitchingActor = true
                                            coroutineScope.launch {
                                                try {
                                                    val resp = ApiClient.getService().login(LoginRequest(acc.email, acc.pass))
                                                    if (resp.isSuccessful && resp.body()?.data != null) {
                                                        val auth = resp.body()!!.data!!
                                                        session.saveAuth(
                                                            token = auth.token,
                                                            id = auth.userId,
                                                            email = auth.userEmail,
                                                            nombre = auth.userNombre,
                                                            rol = auth.userRol,
                                                            fotoUrl = auth.userFotoUrl,
                                                            telefono = auth.userTelefono,
                                                            direccion = auth.userDireccion
                                                        )
                                                        onCambiarActor(auth.userRol)
                                                    } else {
                                                        session.saveAuth("token_demo_${acc.rolTag}", 1L, acc.email, acc.label, acc.rolEnum)
                                                        onCambiarActor(acc.rolEnum)
                                                    }
                                                } catch (_: Exception) {
                                                    session.saveAuth("token_demo_${acc.rolTag}", 1L, acc.email, acc.label, acc.rolEnum)
                                                    onCambiarActor(acc.rolEnum)
                                                } finally {
                                                    isSwitchingActor = false
                                                }
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (esRolActual) Color(0xFFE0F2F1) else Color(0xFFFAFCFB)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = titulo,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (esRolActual) BrandTealDark else TextDark
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = Color(0xFF6C757D)
                                        )
                                    }

                                    if (esRolActual) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(BrandTealDark)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVO ✓",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Cambiar",
                                            tint = BrandTealDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
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

    if (showCambiarPassDialog) {
        ModalCambiarPasswordPerfil(
            onDismiss = { showCambiarPassDialog = false },
            onSuccess = { showCambiarPassDialog = false },
            viewModel = viewModel()
        )
    }
}
