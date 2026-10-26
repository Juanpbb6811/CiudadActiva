package com.ciudadactiva.app.ui.screens.ciudadano

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.CiudadanoViewModel

// ================== PASO 1: CATEGORÍA Y DESCRIPCIÓN (M01_03) ==================
@Composable
fun NuevoReportePaso1Screen(
    viewModel: CiudadanoViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val categorias by viewModel.categorias.collectAsState()
    val selectedCat by viewModel.nuevoReporteCategoria.collectAsState()
    val titulo by viewModel.nuevoReporteTitulo.collectAsState()
    val descripcion by viewModel.nuevoReporteDescripcion.collectAsState()
    val urgencia by viewModel.nuevoReporteUrgencia.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        CiudadActivaTopBar(
            title = "Nuevo Reporte",
            roleTag = "PASO 1 DE 3",
            onBackClick = onBack
        )

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "SELECCIONA LA CATEGORÍA (RF02)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A8B88)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categorias) { cat ->
                    val isSelected = selectedCat?.id == cat.id
                    Card(
                        modifier = Modifier
                            .width(86.dp)
                            .height(96.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) BrandTealDark else Color(0xFFDCE2E1),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.nuevoReporteCategoria.value = cat },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE8F0EF) else Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when (cat.icono) {
                                    "road" -> Icons.Default.DirectionsCar
                                    "lightbulb" -> Icons.Default.Lightbulb
                                    "trash" -> Icons.Default.Delete
                                    "park" -> Icons.Default.Park
                                    else -> Icons.Default.Traffic
                                },
                                contentDescription = null,
                                tint = if (isSelected) BrandTealDark else Color(0xFF6A7775),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.nombre.split(" ").firstOrNull() ?: cat.nombre,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = BrandTealDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text("Título de la Incidencia *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { viewModel.nuevoReporteTitulo.value = it },
                            placeholder = { Text("Ej: Hueco profundo en calzada principal", fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    Column {
                        Text("Descripción del Problema *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { viewModel.nuevoReporteDescripcion.value = it },
                            placeholder = { Text("Detalla el daño y posibles riesgos...", fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 4
                        )
                    }

                    Column {
                        Text("Nivel de Urgencia Percibida (Ciudadano)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Media", "Alta", "Muy Urgente").forEach { nivel ->
                                val isSelected = urgencia == nivel
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, if (isSelected) BrandTealDark else Color(0xFFCFD8DC), RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFFE0F2F1) else Color.White)
                                        .clickable { viewModel.nuevoReporteUrgencia.value = nivel }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSelected) "$nivel ✓" else nivel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BrandTealDark else Color(0xFF546E7A)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GradientButton(
                        text = "Continuar a Ubicación (Paso 2) →",
                        enabled = titulo.isNotBlank() && descripcion.isNotBlank() && selectedCat != null,
                        onClick = onNext
                    )
                }
            }
        }
    }
}

// ================== PASO 2: GEOLOCALIZACIÓN Y SIMULACIÓN (M01_04) ==================
@Composable
fun NuevoReportePaso2Screen(
    viewModel: CiudadanoViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val direccion by viewModel.nuevoReporteDireccion.collectAsState()
    val lat by viewModel.nuevoReporteLatitud.collectAsState()
    val lng by viewModel.nuevoReporteLongitud.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        CiudadActivaTopBar(
            title = "Ubicación",
            roleTag = "PASO 2 DE 3",
            onBackClick = onBack
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Mapa interactivo simulado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE8DE))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Pastilla con dirección
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = direccion, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                            Text(text = "Arrastra el pin para ajustar posición", fontSize = 10.sp, color = Color(0xFF8B9695))
                        }
                    }

                    // Pin central
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BrandTealDark.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(BrandTealDark)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "COORDENADAS GPS SIMULADAS (REGLA 6)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A8B88)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = "Lat: $lat",
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            readOnly = true,
                            label = { Text("Latitud") }
                        )
                        OutlinedTextField(
                            value = "Lng: $lng",
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            readOnly = true,
                            label = { Text("Longitud") }
                        )
                    }

                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { viewModel.nuevoReporteDireccion.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        label = { Text("Dirección de Referencia") }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    GradientButton(
                        text = "Confirmar Ubicación →",
                        onClick = onNext
                    )
                }
            }
        }
    }
}

// ================== PASO 3: CÁMARA MÓVIL Y EVIDENCIA (M01_05) ==================
@Composable
fun NuevoReportePaso3Screen(
    viewModel: CiudadanoViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val fotoBitmap by viewModel.fotoTomadaBitmap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.setFotoCamara(bitmap)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        CiudadActivaTopBar(
            title = "Evidencia",
            roleTag = "PASO 3 DE 3",
            onBackClick = onBack
        )

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "FOTOGRAFÍA DEL PROBLEMA (OPCIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A8B88)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Visor o Lanzador de Cámara Móvil
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .border(1.5.dp, Color(0xFF74A596), RoundedCornerShape(24.dp))
                    .clickable { cameraLauncher.launch() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE4ECEB))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (fotoBitmap != null) {
                        Image(
                            bitmap = fotoBitmap!!.asImageBitmap(),
                            contentDescription = "Foto capturada",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandTealDark)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Foto Cargada con Cámara",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Tomar foto",
                                tint = BrandTealDark,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Toca para abrir cámara móvil",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTealDark
                            )
                            Text(
                                text = "Toma una foto clara del daño o situación",
                                fontSize = 12.sp,
                                color = Color(0xFF6C757D)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de gestión de foto
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { cameraLauncher.launch() },
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCFD8DC)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Tomar Otra Foto", color = BrandTealDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                if (fotoBitmap != null) {
                    Button(
                        onClick = { viewModel.eliminarFoto() },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Eliminar Foto", color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Banner Protección de Datos (RNF01)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Protección de Datos (RNF01)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La fotografía y ubicación se procesan bajo protocolos de privacidad y son almacenadas de forma segura en la base de datos.",
                        fontSize = 11.sp,
                        color = Color(0xFF388E3C),
                        lineHeight = 15.sp
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = "Publicar Incidencia Oficial",
                isLoading = isLoading,
                onClick = {
                    viewModel.enviarReporte { onSuccess() }
                }
            )
        }
    }
}

// ================== PANTALLA ÉXITO (M01_06) ==================
@Composable
fun NuevoReporteExitoScreen(
    viewModel: CiudadanoViewModel,
    onVerSeguimiento: (Long) -> Unit,
    onVolverInicio: () -> Unit
) {
    val reporte by viewModel.nuevoReporteCreado.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "¡Incidencia Radicada!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTealDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Radicado Oficial N.° ${reporte?.id ?: 1045}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrandCopper
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0F2F1))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "PENDIENTE DE VALIDACIÓN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00695C)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu reporte ha sido registrado en la matriz inmutable de trazabilidad (RNF04). Un gestor validará su pertinencia técnica y asignará un operador en campo.",
                fontSize = 13.sp,
                color = Color(0xFF5A6665),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            GradientButton(
                text = "Ver Línea de Tiempo (RF07)",
                onClick = {
                    reporte?.id?.let { onVerSeguimiento(it) } ?: onVolverInicio()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onVolverInicio) {
                Text(
                    text = "Volver al Menú Principal",
                    color = BrandTealDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
