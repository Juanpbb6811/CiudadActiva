package com.ciudadactiva.app.ui.screens.auth

import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.R
import androidx.compose.ui.platform.LocalContext
import com.ciudadactiva.app.ui.components.GoogleAuthHelper
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.navigation.Screen
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.theme.TextMuted
import com.ciudadactiva.app.ui.viewmodel.AuthUiState
import com.ciudadactiva.app.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.emailInput.collectAsState()
    val password by viewModel.passwordInput.collectAsState()
    val rememberSession by viewModel.rememberSession.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var googleEmailInput by remember { mutableStateOf("") }

    var currentServerUrl by remember {
        mutableStateOf(CiudadActivaApplication.sessionManager.getBaseUrl())
    }

    val isEmailValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
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
            val rol = (uiState as AuthUiState.Success).rol
            when (rol) {
                "ROLE_GESTOR" -> onLoginSuccess(Screen.GestorDashboard.route)
                "ROLE_OPERADOR" -> onLoginSuccess(Screen.OperadorTareas.route)
                "ROLE_ADMIN" -> onLoginSuccess(Screen.AdminPanel.route)
                else -> onLoginSuccess(Screen.CiudadanoHome.route)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Logo CiudadActiva
            Image(
                painter = painterResource(id = R.drawable.ic_logo_ciudadactiva),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ciudadactiva",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTealDark
            )

            Text(
                text = "Gestión y Seguimiento Comunitario",
                fontSize = 13.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                border = BorderStroke(1.dp, Color(0xFFD0D7D6)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
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
                        text = "Continuar con Google / Gmail",
                        color = TextDark,
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
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDE3E2))
                Text(
                    text = "  O ingresa con tu cuenta  ",
                    fontSize = 11.sp,
                    color = Color(0xFF7A8B88),
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDE3E2))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tarjeta de Formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Correo Electrónico
                    Column {
                        Text(
                            text = "Correo Electrónico",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { viewModel.emailInput.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("tu_correo@ejemplo.com", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF7A8B88))
                            },
                            singleLine = true,
                            isError = !isEmailValid,
                            supportingText = {
                                if (!isEmailValid) {
                                    Text(
                                        text = "Formato de correo no válido",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }

                    // Contraseña
                    Column {
                        Text(
                            text = "Contraseña",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { viewModel.passwordInput.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("••••••••", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF7A8B88))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Ocultar" else "Ver contraseña",
                                        tint = Color(0xFF7A8B88)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }

                    // Recordar sesión y Olvidaste clave
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberSession,
                                onCheckedChange = { viewModel.rememberSession.value = it },
                                colors = CheckboxDefaults.colors(checkedColor = BrandTealDark)
                            )
                            Text(text = "Recordar sesión", fontSize = 12.sp, color = TextDark)
                        }

                        Text(
                            text = "¿Olvidaste clave?",
                            fontSize = 12.sp,
                            color = Color(0xFF4C877D),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { /* Simulación */ }
                        )
                    }

                    // Mensaje de Error
                    if (uiState is AuthUiState.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFEBEE))
                                .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = (uiState as AuthUiState.Error).message,
                                color = Color(0xFFC62828),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Botón Ingresar
                    GradientButton(
                        text = "Ingresar a la Plataforma",
                        enabled = email.isNotBlank() && isEmailValid && password.isNotBlank(),
                        onClick = { viewModel.login() },
                        isLoading = uiState is AuthUiState.Loading
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer link para registrar cuenta propia
            Text(
                text = "¿No tienes una cuenta aún?",
                fontSize = 13.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Crear cuenta de Ciudadano",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTealDark,
                modifier = Modifier.clickable(onClick = onRegisterClick)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Botón superior de configuración de servidor / URL
        IconButton(
            onClick = {
                currentServerUrl = CiudadActivaApplication.sessionManager.getBaseUrl()
                showServerDialog = true
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurar Servidor",
                tint = BrandTealDark
            )
        }

        // Diálogo para acceder con Google
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
                            text = "Ingresa tu cuenta de Google (Gmail o institucional) para iniciar sesión o registrarte automáticamente:",
                            fontSize = 13.sp,
                            color = Color(0xFF555555)
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
                                val inferredName = googleEmailInput.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                                viewModel.loginConGoogle(googleEmailInput.trim(), inferredName)
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

        // Diálogo para cambiar la URL del Backend
        if (showServerDialog) {
            AlertDialog(
                onDismissRequest = { showServerDialog = false },
                title = { Text("Conexión del Backend", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Selecciona o escribe la URL del servidor:",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        OutlinedTextField(
                            value = currentServerUrl,
                            onValueChange = { currentServerUrl = it },
                            label = { Text("URL Base") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { currentServerUrl = "https://ciudadactiva-4ah7.onrender.com/" },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Render Nube", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { currentServerUrl = "http://10.0.2.2:8080/" },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Emulador", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { currentServerUrl = "http://192.168.1.22:8080/" },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Wi-Fi PC", fontSize = 11.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            CiudadActivaApplication.sessionManager.setBaseUrl(currentServerUrl)
                            showServerDialog = false
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showServerDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
