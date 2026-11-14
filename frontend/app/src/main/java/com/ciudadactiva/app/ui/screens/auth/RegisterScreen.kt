package com.ciudadactiva.app.ui.screens.auth

import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.R
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.components.GoogleAuthHelper
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.viewmodel.AuthUiState
import com.ciudadactiva.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Datos Legales Colombianos del Ciudadano
    var primerNombre by remember { mutableStateOf("") }
    var segundoNombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf("") }
    var segundoApellido by remember { mutableStateOf("") }

    val tiposDocumento = listOf("CC", "TI", "CE", "PASAPORTE", "PPT")
    val tiposDocumentoLabels = listOf(
        "CC - Cédula de Ciudadanía",
        "TI - Tarjeta de Identidad",
        "CE - Cédula de Extranjería",
        "PASAPORTE - Pasaporte",
        "PPT - Permiso Protección Temporal"
    )
    var expandedTipoDoc by remember { mutableStateOf(false) }
    var tipoDocumentoSeleccionado by remember { mutableStateOf("CC") }

    var numeroDocumento by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var aceptaTerminos by remember { mutableStateOf(true) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var googleEmailInput by remember { mutableStateOf("") }

    val isEmailValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val passwordsMatch = confirmPassword.isEmpty() || password == confirmPassword
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Launcher del panel oficial de cuentas de Google de Android
    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cuenta = GoogleAuthHelper.procesarResultadoSelector(result.resultCode, result.data)
        if (cuenta != null) {
            viewModel.loginConGoogle(cuenta.first, cuenta.second)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CiudadActivaTopBar(
                title = "Registro Ciudadano",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registro Legal de Ciudadanos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Identificación ciudadana conforme a la normativa colombiana para trazabilidad comunitaria",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Continuar con Google
                OutlinedButton(
                    onClick = {
                        GoogleAuthHelper.iniciarGoogleSignIn(
                            context = context,
                            coroutineScope = coroutineScope,
                            onSuccess = { googleEmail, name ->
                                viewModel.loginConGoogle(googleEmail, name)
                            },
                            onLaunchNativePicker = {
                                try {
                                    val intent = GoogleAuthHelper.crearIntentSelectorCuentasGoogle()
                                    googleAccountPickerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    googleEmailInput = email.ifBlank { "" }
                                    showGoogleDialog = true
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Registrarse con Google / Gmail",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(
                        text = "  O completa tu formulario legal  ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Datos de Identificación Personal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Nombres
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = primerNombre,
                                onValueChange = { primerNombre = it },
                                label = { Text("Primer Nombre *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = segundoNombre,
                                onValueChange = { segundoNombre = it },
                                label = { Text("Segundo Nombre") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }

                        // Apellidos
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = primerApellido,
                                onValueChange = { primerApellido = it },
                                label = { Text("Primer Apellido *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = segundoApellido,
                                onValueChange = { segundoApellido = it },
                                label = { Text("Segundo Apellido *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }

                        // Selector Tipo de Documento
                        ExposedDropdownMenuBox(
                            expanded = expandedTipoDoc,
                            onExpandedChange = { expandedTipoDoc = !expandedTipoDoc },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = tiposDocumentoLabels[tiposDocumento.indexOf(tipoDocumentoSeleccionado).coerceAtLeast(0)],
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Documento *") },
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
                                tiposDocumento.forEachIndexed { idx, code ->
                                    DropdownMenuItem(
                                        text = { Text(tiposDocumentoLabels[idx], fontSize = 13.sp) },
                                        onClick = {
                                            tipoDocumentoSeleccionado = code
                                            expandedTipoDoc = false
                                        }
                                    )
                                }
                            }
                        }

                        // Número de Documento
                        OutlinedTextField(
                            value = numeroDocumento,
                            onValueChange = { numeroDocumento = it.filter { char -> char.isLetterOrDigit() } },
                            label = { Text("Número de Documento *") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Text(
                            text = "Contacto y Ubicación en Colombia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Teléfono Celular
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it.filter { char -> char.isDigit() } },
                            label = { Text("Número de Teléfono Celular *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            placeholder = { Text("Ej: 3001234567") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        // Dirección
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Dirección de Residencia *") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            placeholder = { Text("Ej: Calle 72 # 11-45, Chapinero") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Text(
                            text = "Seguridad de Acceso",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Correo con validación de formato
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electrónico *") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            isError = !isEmailValid,
                            supportingText = {
                                if (!isEmailValid) {
                                    Text(
                                        text = "Ingresa un correo electrónico válido (ej: nombre@gmail.com)",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        // Contraseña con ícono para ver
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña (mín 6 caracteres) *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Ocultar" else "Ver",
                                        tint = Color(0xFF7A8B88)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        // Repetir contraseña con ícono para ver
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirmar Contraseña *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Ocultar" else "Ver",
                                        tint = Color(0xFF7A8B88)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            isError = !passwordsMatch,
                            supportingText = {
                                if (!passwordsMatch) {
                                    Text(
                                        text = "Las contraseñas no coinciden",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        // Términos y condiciones
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = aceptaTerminos,
                                onCheckedChange = { aceptaTerminos = it },
                                colors = CheckboxDefaults.colors(checkedColor = BrandTealDark)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Acepto el tratamiento de datos y términos comunitarios de CiudadActiva",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }

                        // Mensaje de Error si ocurre
                        if (uiState is AuthUiState.Error) {
                            val errorText = (uiState as AuthUiState.Error).message
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = errorText,
                                    color = Color(0xFFC62828),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Validación de formulario completo
                        val isFormValid = primerNombre.trim().isNotBlank() &&
                                primerApellido.trim().isNotBlank() &&
                                segundoApellido.trim().isNotBlank() &&
                                numeroDocumento.trim().isNotBlank() &&
                                telefono.trim().length >= 7 &&
                                direccion.trim().isNotBlank() &&
                                email.trim().isNotBlank() &&
                                isEmailValid &&
                                password.length >= 6 &&
                                password == confirmPassword &&
                                aceptaTerminos

                        GradientButton(
                            text = "Completar Registro Ciudadano",
                            enabled = isFormValid,
                            isLoading = uiState is AuthUiState.Loading,
                            onClick = {
                                viewModel.registrarCiudadanoLegal(
                                    primerNombre = primerNombre,
                                    segundoNombre = segundoNombre,
                                    primerApellido = primerApellido,
                                    segundoApellido = segundoApellido,
                                    tipoDocumento = tipoDocumentoSeleccionado,
                                    numeroDocumento = numeroDocumento,
                                    email = email,
                                    pass = password,
                                    tel = telefono,
                                    dir = direccion
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Modal para Registro con Google
        if (showGoogleDialog) {
            AlertDialog(
                onDismissRequest = { showGoogleDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Acceder con Google", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Ingresa tu cuenta de Google (Gmail) para vincularte e iniciar sesión automáticamente:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = googleEmailInput,
                            onValueChange = { googleEmailInput = it },
                            label = { Text("Correo de Google") },
                            placeholder = { Text("usuario@gmail.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (googleEmailInput.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(googleEmailInput.trim()).matches()) {
                                showGoogleDialog = false
                                val inferredName = if (primerNombre.isNotBlank()) "$primerNombre $primerApellido" else googleEmailInput.substringBefore("@")
                                viewModel.loginConGoogle(
                                    googleEmail = googleEmailInput.trim(),
                                    googleName = inferredName,
                                    primerNombre = primerNombre.ifBlank { null },
                                    primerApellido = primerApellido.ifBlank { null },
                                    segundoApellido = segundoApellido.ifBlank { null },
                                    tipoDocumento = tipoDocumentoSeleccionado,
                                    numeroDocumento = numeroDocumento.ifBlank { null },
                                    telefono = telefono.ifBlank { null },
                                    direccion = direccion.ifBlank { null }
                                )
                            }
                        },
                        enabled = googleEmailInput.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(googleEmailInput.trim()).matches()
                    ) {
                        Text("Continuar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoogleDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
