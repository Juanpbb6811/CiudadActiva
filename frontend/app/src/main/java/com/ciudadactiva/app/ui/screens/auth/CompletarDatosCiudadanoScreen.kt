package com.ciudadactiva.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.data.model.CompletarDatosLegalesRequest
import com.ciudadactiva.app.ui.components.GradientButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletarDatosCiudadanoScreen(
    onCompletadoExitoso: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val session = CiudadActivaApplication.sessionManager
    val coroutineScope = rememberCoroutineScope()

    var primerNombre by remember { mutableStateOf(session.getNombre()?.split(" ")?.firstOrNull() ?: "") }
    var segundoNombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf("") }
    var segundoApellido by remember { mutableStateOf("") }
    var tipoDocumento by remember { mutableStateOf(session.getTipoDocumento()) }
    var numeroDocumento by remember { mutableStateOf(session.getNumeroDocumento() ?: "") }
    var telefono by remember { mutableStateOf(session.getTelefono() ?: "") }
    var direccion by remember { mutableStateOf(session.getDireccion() ?: "") }

    var expandedTipoDoc by remember { mutableStateOf(false) }
    val tiposDoc = listOf(
        "CC" to "Cédula de Ciudadanía (CC)",
        "TI" to "Tarjeta de Identidad (TI)",
        "CE" to "Cédula de Extranjería (CE)",
        "PASAPORTE" to "Pasaporte",
        "PPT" to "Permiso por Protección Temporal (PPT)"
    )

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isFormValid = primerNombre.isNotBlank() &&
            primerApellido.isNotBlank() &&
            numeroDocumento.isNotBlank() &&
            telefono.isNotBlank() &&
            direccion.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Encabezado
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Badge,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Registro Legal de Ciudadano",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Para reportar y dar seguimiento formal en CiudadActiva, la normativa exige asociar tu identidad y ubicación real.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Tarjeta con Formulario
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Nombres
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Primer Nombre *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = primerNombre,
                            onValueChange = { primerNombre = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Segundo Nombre", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = segundoNombre,
                            onValueChange = { segundoNombre = it },
                            placeholder = { Text("Opcional", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                // Apellidos
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Primer Apellido *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = primerApellido,
                            onValueChange = { primerApellido = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Segundo Apellido", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = segundoApellido,
                            onValueChange = { segundoApellido = it },
                            placeholder = { Text("Opcional", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                // Tipo y Número de Documento
                Column {
                    Text("Tipo de Documento *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedTipoDoc,
                        onExpandedChange = { expandedTipoDoc = !expandedTipoDoc }
                    ) {
                        OutlinedTextField(
                            value = tiposDoc.firstOrNull { it.first == tipoDocumento }?.second ?: tipoDocumento,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoDoc) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTipoDoc,
                            onDismissRequest = { expandedTipoDoc = false }
                        ) {
                            tiposDoc.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.second, fontSize = 13.sp) },
                                    onClick = {
                                        tipoDocumento = item.first
                                        expandedTipoDoc = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column {
                    Text("Número de Documento *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = numeroDocumento,
                        onValueChange = { numeroDocumento = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Teléfono
                Column {
                    Text("Teléfono Móvil *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: 3101234567", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Dirección
                Column {
                    Text("Dirección de Residencia *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: Calle 53 # 13-20, Bogotá", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                GradientButton(
                    text = "Guardar y Habilitar Ciudadanía",
                    enabled = isFormValid,
                    isLoading = isLoading,
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val req = CompletarDatosLegalesRequest(
                                    primerNombre = primerNombre.trim(),
                                    segundoNombre = segundoNombre.trim().ifBlank { null },
                                    primerApellido = primerApellido.trim(),
                                    segundoApellido = segundoApellido.trim().ifBlank { null },
                                    tipoDocumento = tipoDocumento,
                                    numeroDocumento = numeroDocumento.trim(),
                                    telefono = telefono.trim(),
                                    direccion = direccion.trim()
                                )
                                val resp = ApiClient.getService().completarDatosLegales(req)
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
                                        direccion = auth.userDireccion,
                                        tipoDocumento = auth.userTipoDocumento,
                                        numeroDocumento = auth.userNumeroDocumento,
                                        primerNombre = auth.userPrimerNombre,
                                        primerApellido = auth.userPrimerApellido,
                                        googleId = auth.userGoogleId,
                                        datosLegalesCompletos = true
                                    )
                                    onCompletadoExitoso()
                                } else {
                                    errorMessage = "Error al guardar datos: ${resp.message()}"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de conexión: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Cerrar Sesión
        TextButton(onClick = onCerrarSesion) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cerrar Sesión / Salir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
