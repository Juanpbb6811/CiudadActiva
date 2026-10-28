package com.ciudadactiva.app.ui.screens.auth

import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.components.OtpInputField
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modal interactivo para verificación y activación de cuenta de usuario con código OTP de 6 dígitos.
 */
@Composable
fun ModalVerificarCuenta(
    email: String,
    demoCode: String? = null,
    onDismiss: () -> Unit,
    onVerificadoExitoso: () -> Unit,
    viewModel: AuthViewModel
) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var exitoMessage by remember { mutableStateOf<String?>(null) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Timer para cuenta regresiva de reenvío de código
    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds--
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícono de cabecera
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = BrandTealDark,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Verificación de Seguridad",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandTealDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Hemos generado un código de 6 dígitos para verificar tu cuenta:",
                    fontSize = 12.sp,
                    color = Color(0xFF6C757D),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = email,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandTealDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Componente OTP de 6 dígitos
                OtpInputField(
                    otpValue = otp,
                    onOtpChange = {
                        otp = it
                        errorMessage = null
                    },
                    isError = errorMessage != null
                )

                // Mensaje informativo de verificación
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color(0xFFF1F8F6)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Revisa tu bandeja de entrada o spam en tu correo. El código de 6 dígitos vence en 15 minutos.",
                            fontSize = 11.sp,
                            color = Color(0xFF37474F),
                            lineHeight = 15.sp
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                if (exitoMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = exitoMessage!!,
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Verificar
                GradientButton(
                    text = if (isLoading) "Validando..." else "Verificar y Activar Cuenta",
                    enabled = otp.length == 6 && !isLoading,
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        viewModel.verificarCodigo(email, otp) { exito, msg ->
                            isLoading = false
                            if (exito) {
                                Toast.makeText(context, "¡Cuenta verificada exitosamente!", Toast.LENGTH_SHORT).show()
                                onVerificadoExitoso()
                            } else {
                                errorMessage = msg
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Reenviar código con cooldown
                if (cooldownSeconds > 0) {
                    Text(
                        text = "Puedes solicitar nuevo código en ${cooldownSeconds}s",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else {
                    TextButton(
                        onClick = {
                            cooldownSeconds = 45
                            viewModel.reenviarCodigo(email, "VERIFICACION") { exito, msg ->
                                if (exito) {
                                    exitoMessage = "Nuevo código generado con éxito"
                                    // Extraer código si viene en la respuesta demo
                                    val match = Regex("(\\d{6})").find(msg)?.value
                                    if (match != null) {
                                        otp = match
                                    }
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "¿No recibiste el código? Reenviar",
                            color = BrandTealDark,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Cancelar",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Modal interactivo para Recuperación de Contraseña Olvidada con OTP de 6 dígitos.
 */
@Composable
fun ModalRecuperarPassword(
    initialEmail: String = "",
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit,
    viewModel: AuthViewModel
) {
    var paso by remember { mutableIntStateOf(1) } // 1: Email, 2: OTP y Nueva Contraseña
    var email by remember { mutableStateOf(initialEmail) }
    var otp by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds--
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Encabezado
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (paso == 1) Icons.Default.LockReset else Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (paso == 1) "Recuperar Contraseña" else "Restablecer Contraseña",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandTealDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (paso == 1) {
                        "Ingresa tu correo registrado para generar un código OTP de recuperación de 6 dígitos:"
                    } else {
                        "Ingresa el código de 6 dígitos y define tu nueva contraseña:"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF6C757D),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (paso == 1) {
                    // PASO 1: Ingreso de correo electrónico
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Correo Electrónico Registrado") },
                        placeholder = { Text("ejemplo@correo.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandTealDark) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage!!, color = Color(0xFFD32F2F), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    GradientButton(
                        text = if (isLoading) "Generando código..." else "Enviar Código de Recuperación",
                        enabled = email.contains("@") && email.contains(".") && !isLoading,
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            viewModel.solicitarRecuperacionPassword(email) { exito, msg ->
                                isLoading = false
                                if (exito) {
                                    paso = 2
                                    cooldownSeconds = 60
                                    infoMessage = "Revisa tu correo electrónico para obtener el código de 6 dígitos."
                                } else {
                                    errorMessage = msg
                                }
                            }
                        }
                    )
                } else {
                    // PASO 2: Ingreso de código OTP y Nueva contraseña
                    Text(
                        text = "Código enviado a: $email",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandTealDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OtpInputField(
                        otpValue = otp,
                        onOtpChange = {
                            otp = it
                            errorMessage = null
                        },
                        isError = errorMessage != null
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = Color(0xFFF1F8F6)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hemos enviado el código OTP de 6 dígitos a tu bandeja de correo (o carpeta spam). Vence en 15 minutos.",
                                fontSize = 11.sp,
                                color = Color(0xFF37474F),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo Nueva Contraseña
                    OutlinedTextField(
                        value = nuevaPassword,
                        onValueChange = {
                            nuevaPassword = it
                            errorMessage = null
                        },
                        label = { Text("Nueva Contraseña") },
                        placeholder = { Text("Mínimo 6 caracteres") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandTealDark) },
                        trailingIcon = {
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(
                                    imageVector = if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Campo Confirmar Contraseña
                    OutlinedTextField(
                        value = confirmarPassword,
                        onValueChange = {
                            confirmarPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirmar Nueva Contraseña") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = BrandTealDark) },
                        visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage!!, color = Color(0xFFD32F2F), fontSize = 12.sp, textAlign = TextAlign.Center)
                    }

                    if (infoMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = infoMessage!!, color = Color(0xFF2E7D32), fontSize = 12.sp, textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    GradientButton(
                        text = if (isLoading) "Restableciendo..." else "Actualizar Contraseña",
                        enabled = otp.length == 6 && nuevaPassword.length >= 6 && nuevaPassword == confirmarPassword && !isLoading,
                        onClick = {
                            if (nuevaPassword != confirmarPassword) {
                                errorMessage = "Las contraseñas no coinciden"
                                return@GradientButton
                            }
                            isLoading = true
                            errorMessage = null
                            viewModel.resetPassword(email, otp, nuevaPassword) { exito, msg ->
                                isLoading = false
                                if (exito) {
                                    Toast.makeText(context, "¡Contraseña actualizada con éxito!", Toast.LENGTH_LONG).show()
                                    onSuccess(email)
                                } else {
                                    errorMessage = msg
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reenviar código
                    if (cooldownSeconds > 0) {
                        Text(
                            text = "Reenviar código en ${cooldownSeconds}s",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    } else {
                        TextButton(
                            onClick = {
                                cooldownSeconds = 60
                                viewModel.reenviarCodigo(email, "RECUPERACION") { exito, msg ->
                                    if (exito) {
                                        infoMessage = "Nuevo código de 6 dígitos enviado a tu correo."
                                        otp = ""
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            enabled = !isLoading
                        ) {
                            Text("¿No te llegó el código? Reenviar", fontSize = 11.sp, color = BrandTealDark)
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("Cerrar", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Modal para cambio de contraseña desde el perfil de usuario autenticado.
 */
@Composable
fun ModalCambiarPasswordPerfil(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    var passActual by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }
    var passConfirmar by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cambiar Contraseña",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandTealDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = passActual,
                    onValueChange = { passActual = it; errorMessage = null },
                    label = { Text("Contraseña Actual") },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = passNueva,
                    onValueChange = { passNueva = it; errorMessage = null },
                    label = { Text("Nueva Contraseña (mínimo 6)") },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = passConfirmar,
                    onValueChange = { passConfirmar = it; errorMessage = null },
                    label = { Text("Confirmar Nueva Contraseña") },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = Color(0xFFD32F2F), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                GradientButton(
                    text = if (isLoading) "Guardando..." else "Actualizar Contraseña",
                    enabled = passActual.isNotBlank() && passNueva.length >= 6 && passNueva == passConfirmar && !isLoading,
                    onClick = {
                        if (passNueva != passConfirmar) {
                            errorMessage = "Las contraseñas no coinciden"
                            return@GradientButton
                        }
                        isLoading = true
                        errorMessage = null
                        viewModel.cambiarPassword(passActual, passNueva) { exito, msg ->
                            isLoading = false
                            if (exito) {
                                Toast.makeText(context, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            } else {
                                errorMessage = msg
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss, enabled = !isLoading) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        }
    }
}
