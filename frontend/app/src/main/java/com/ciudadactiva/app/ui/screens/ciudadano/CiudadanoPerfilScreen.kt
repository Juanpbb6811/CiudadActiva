package com.ciudadactiva.app.ui.screens.ciudadano

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.R
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.data.local.DemoCredentials
import com.ciudadactiva.app.data.model.LoginRequest
import com.ciudadactiva.app.data.model.UsuarioUpdateRequest
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.components.GoogleAuthHelper
import com.ciudadactiva.app.ui.components.rememberCameraLauncher
import com.ciudadactiva.app.ui.screens.auth.ModalCambiarPasswordPerfil
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.BrandTealLight
import com.ciudadactiva.app.util.AppThemeMode
import com.ciudadactiva.app.util.ThemeManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun CiudadanoPerfilScreen(
    onCerrarSesion: () -> Unit,
    onCambiarActor: (String) -> Unit = {},
    onCompletarDatosLegales: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val session = CiudadActivaApplication.sessionManager
    val coroutineScope = rememberCoroutineScope()

    // Modo de apariencia reactivo
    val currentThemeMode by ThemeManager.themeMode.collectAsState()

    // Estado de modo edición: FALSO por defecto (solo lectura inicial)
    var isEditing by remember { mutableStateOf(false) }

    // Valores en edición
    var nombre by remember { mutableStateOf(session.getNombre() ?: "") }
    var email by remember { mutableStateOf(session.getEmail() ?: "") }
    var telefono by remember { mutableStateOf(session.getTelefono() ?: "") }
    var direccion by remember { mutableStateOf(session.getDireccion() ?: "") }
    var fotoUrl by remember { mutableStateOf(session.getFotoUrl()) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Identificación legal
    var tipoDoc by remember { mutableStateOf(session.getTipoDocumento()) }
    var numDoc by remember { mutableStateOf(session.getNumeroDocumento() ?: "") }
    var googleId by remember { mutableStateOf(session.getGoogleId() ?: "") }
    var datosLegalesCompletos by remember { mutableStateOf(session.areDatosLegalesCompletos()) }

    // Backup para botón Cancelar
    var backupNombre by remember { mutableStateOf(nombre) }
    var backupEmail by remember { mutableStateOf(email) }
    var backupTelefono by remember { mutableStateOf(telefono) }
    var backupDireccion by remember { mutableStateOf(direccion) }
    var backupFotoUrl by remember { mutableStateOf(fotoUrl) }

    var isSaving by remember { mutableStateOf(false) }
    var isSyncingGoogle by remember { mutableStateOf(false) }
    var isSwitchingActor by remember { mutableStateOf(false) }
    var showCambiarPassDialog by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val rol = session.getRol() ?: "ROLE_CIUDADANO"

    // Launcher de cámara para foto de perfil
    val tomarFotoPerfil = rememberCameraLauncher(
        onPhotoTaken = { bitmap ->
            avatarBitmap = bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val base64 = "data:image/jpeg;base64," + Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            fotoUrl = base64
        }
    )

    // Launcher de selector de Google
    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cuenta = GoogleAuthHelper.procesarResultadoSelector(result.resultCode, result.data)
        if (cuenta != null) {
            isSyncingGoogle = true
            coroutineScope.launch {
                try {
                    val resp = ApiClient.getService().vincularGoogle(mapOf("googleId" to cuenta.first))
                    if (resp.isSuccessful) {
                        session.setGoogleId(cuenta.first)
                        googleId = cuenta.first
                        mensajeExito = "Cuenta de Google (${cuenta.first}) sincronizada exitosamente con tu identidad ciudadana."
                    } else {
                        val err = resp.errorBody()?.string() ?: "No se pudo sincronizar la cuenta."
                        mensajeError = err
                    }
                } catch (e: Exception) {
                    mensajeError = "Fallo al conectar con el servidor: ${e.localizedMessage}"
                } finally {
                    isSyncingGoogle = false
                }
            }
        }
    }

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
                tipoDoc = u.tipoDocumento ?: "CC"
                numDoc = u.numeroDocumento ?: ""
                googleId = u.googleId ?: ""
                datosLegalesCompletos = !numDoc.isBlank() && !numDoc.contains("PENDIENTE") && !telefono.isBlank() && !direccion.isBlank() && !nombre.isBlank()

                backupNombre = nombre
                backupEmail = email
                backupTelefono = telefono
                backupDireccion = direccion
                backupFotoUrl = fotoUrl

                session.updateProfile(
                    nombre = u.nombre ?: "",
                    email = u.email ?: "",
                    telefono = u.telefono,
                    direccion = u.direccion,
                    fotoUrl = u.fotoUrl,
                    tipoDocumento = u.tipoDocumento,
                    numeroDocumento = u.numeroDocumento,
                    googleId = u.googleId
                )
            }
        } catch (_: Exception) {
            // Ignorar y usar valores locales
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        CiudadActivaTopBar(title = "Mi Perfil", onBackClick = onBackClick)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar con botón de cámara
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(3.dp, MaterialTheme.colorScheme.secondary, CircleShape),
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Botón circular para cambiar foto
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable {
                            if (!isEditing) isEditing = true
                            tomarFotoPerfil()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar Foto",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón explícito "Tomar Foto"
            OutlinedButton(
                onClick = {
                    if (!isEditing) isEditing = true
                    tomarFotoPerfil()
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tomar Foto", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = nombre.ifBlank { "Ciudadano" },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = email,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = rol.replace("ROLE_", "PERFIL: "),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Mensajes globales de éxito o error
            if (mensajeExito != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(text = mensajeExito!!, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                }
            }

            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(text = mensajeError!!, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 1. TARJETA: APARIENCIA Y TEMA VISUAL (REQUERIDO)
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Apariencia y Modo Visual",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Elige la apariencia visual de la app o sincronízala automáticamente con el sistema operativo de tu dispositivo.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(AppThemeMode.SYSTEM, "📱 Sistema", Icons.Default.BrightnessAuto),
                            Triple(AppThemeMode.LIGHT, "☀️ Claro", Icons.Default.LightMode),
                            Triple(AppThemeMode.DARK, "🌙 Oscuro", Icons.Default.DarkMode)
                        ).forEach { (mode, label, icon) ->
                            val isSelected = currentThemeMode == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { ThemeManager.setTheme(context, mode) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "Activo",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 2. TARJETA: IDENTIFICACIÓN LEGAL CIUDADANA (REQUERIDO)
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Identidad Ciudadana Legal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Badge de verificación
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (datosLegalesCompletos) Color(0xFF2E7D32).copy(alpha = 0.2f)
                                    else Color(0xFFE65100).copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (datosLegalesCompletos) "Verificado ✓" else "Incompleto ⚠️",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (datosLegalesCompletos) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }

                    Text(
                        text = "Registro único de ciudadano bajo normativa colombiana para radicación oficial de PQRSD e incidencias.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Tipo Documento", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = tipoDoc.ifBlank { "CC" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text(text = "Número de Documento", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = numDoc.ifBlank { "Sin registrar" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    if (!datosLegalesCompletos && onCompletarDatosLegales != null) {
                        Button(
                            onClick = onCompletarDatosLegales,
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Completar mis datos legales ahora", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 3. TARJETA: SINCRONIZACIÓN CON GOOGLE (REQUERIDO)
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sincronización con Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Sincroniza tu cuenta de Google para agilizar la verificación de tu identidad legal sin necesidad de contraseña.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (googleId.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Cuenta de Google Vinculada", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = googleId, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = GoogleAuthHelper.crearIntentSelectorCuentasGoogle()
                                    googleAccountPickerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    mensajeError = "No se pudo abrir el selector de cuentas de Google: ${e.localizedMessage}"
                                }
                            },
                            enabled = !isSyncingGoogle,
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            if (isSyncingGoogle) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sincronizar mi cuenta con Google", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 4. TARJETA: DATOS PERSONALES Y CONTACTO
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            color = MaterialTheme.colorScheme.onSurface
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
                                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Nombre Completo
                    Column {
                        Text("Nombre Completo *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                    }

                    // Correo Electrónico
                    Column {
                        Text("Correo Electrónico *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                    }

                    // Teléfono
                    Column {
                        Text("Número de Teléfono", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            placeholder = { Text("Sin registrar (Opcional)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp) }
                        )
                    }

                    // Dirección de Residencia
                    Column {
                        Text("Dirección de Residencia", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            placeholder = { Text("Sin registrar (Opcional)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp) }
                        )
                    }

                    // Acciones en modo edición
                    if (isEditing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    nombre = backupNombre
                                    email = backupEmail
                                    telefono = backupTelefono
                                    direccion = backupDireccion
                                    fotoUrl = backupFotoUrl
                                    avatarBitmap = null
                                    mensajeExito = null
                                    mensajeError = null
                                    isEditing = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (nombre.isBlank() || email.isBlank()) {
                                        mensajeError = "El nombre y el correo no pueden estar vacíos."
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
                                                telefono = telefono.trim().ifBlank { null },
                                                direccion = direccion.trim().ifBlank { null },
                                                fotoUrl = fotoUrl
                                            )
                                            val resp = ApiClient.getService().actualizarMiPerfil(req)
                                            if (resp.isSuccessful && resp.body()?.data != null) {
                                                val u = resp.body()!!.data!!
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

                                                session.updateProfile(
                                                    nombre = u.nombre ?: "",
                                                    email = u.email ?: "",
                                                    telefono = u.telefono,
                                                    direccion = u.direccion,
                                                    fotoUrl = u.fotoUrl
                                                )

                                                mensajeExito = "Perfil actualizado correctamente."
                                                isEditing = false
                                            } else {
                                                mensajeError = "No se pudo actualizar el perfil. Verifica los datos."
                                            }
                                        } catch (e: Exception) {
                                            mensajeError = "Error al guardar: ${e.localizedMessage}"
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { showCambiarPassDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cambiar Contraseña de Acceso", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // =========================================================================
            // 5. TARJETA: CAMBIO DE ROL / ACTOR (EXCLUSIVO CUENTAS DEMO)
            // =========================================================================
            val emailActual = session.getEmail()?.lowercase()?.trim() ?: ""
            val esCuentaDemo = emailActual in listOf(
                "ciudadano@ciudadactiva.gov.co",
                "gestor@ciudadactiva.gov.co",
                "operador@ciudadactiva.gov.co",
                "admin@ciudadactiva.gov.co"
            )

            if (esCuentaDemo) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Alterna entre los 4 perfiles del sistema",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSwitchingActor) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

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
                                        color = if (esRolActual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
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
                                    containerColor = if (esRolActual) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
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
                                            color = if (esRolActual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (esRolActual) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVO ✓",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Cambiar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // =========================================================================
            // 6. BOTÓN CERRAR SESIÓN (ALTO CONTRASTE)
            // =========================================================================
            Button(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    color = MaterialTheme.colorScheme.onErrorContainer,
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
