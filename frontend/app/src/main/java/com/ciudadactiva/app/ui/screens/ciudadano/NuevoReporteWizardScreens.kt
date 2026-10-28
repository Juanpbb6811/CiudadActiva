package com.ciudadactiva.app.ui.screens.ciudadano

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.components.LocationHelper
import com.ciudadactiva.app.ui.components.OpenStreetMapComposable
import com.ciudadactiva.app.ui.components.rememberCameraLauncher
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
                text = "SELECCIONA LA CATEGORÍA DE LA INCIDENCIA",
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 4
                        )
                    }

                    // Cálculo automático de urgencia (El usuario NO la selecciona arbitrariamente)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Urgencia Calculada Automáticamente",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "El nivel de prioridad y el semáforo de atención se asignan de forma automática según la concentración de reportes en la misma zona geográfica (1 reporte = Verde, 10+ reportes = Rojo Crítico).",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

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

// ================== PASO 2: GEOLOCALIZACIÓN CON MAPA REAL (M01_04) ==================
@Composable
fun NuevoReportePaso2Screen(
    viewModel: CiudadanoViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val direccion by viewModel.nuevoReporteDireccion.collectAsState()
    val lat by viewModel.nuevoReporteLatitud.collectAsState()
    val lng by viewModel.nuevoReporteLongitud.collectAsState()

    var userGpsLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            LocationHelper.getCurrentLocation(
                context = context,
                onLocationReceived = { gLat, gLng ->
                    userGpsLocation = Pair(gLat, gLng)
                    viewModel.nuevoReporteLatitud.value = gLat
                    viewModel.nuevoReporteLongitud.value = gLng
                    Toast.makeText(context, "📍 Ubicación GPS establecida", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Permiso denegado. Puedes marcar el punto en el mapa.", Toast.LENGTH_SHORT).show()
        }
    }

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
            // Mapa 100% Gratuito OpenStreetMap con Pin Arrastrable e Interactivo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE8DE))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    OpenStreetMapComposable(
                        modifier = Modifier.fillMaxSize(),
                        centerLat = lat,
                        centerLng = lng,
                        zoom = 15,
                        userLocation = userGpsLocation,
                        isPickerMode = true,
                        selectedLat = lat,
                        selectedLng = lng,
                        onLocationSelected = { pickedLat, pickedLng ->
                            viewModel.nuevoReporteLatitud.value = pickedLat
                            viewModel.nuevoReporteLongitud.value = pickedLng
                        }
                    )

                    // Pastilla con instrucción flotante
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Toca el mapa o arrastra el marcador",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandTealDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón para Usar GPS del Teléfono
            OutlinedButton(
                onClick = {
                    if (LocationHelper.hasLocationPermission(context)) {
                        LocationHelper.getCurrentLocation(
                            context = context,
                            onLocationReceived = { gLat, gLng ->
                                userGpsLocation = Pair(gLat, gLng)
                                viewModel.nuevoReporteLatitud.value = gLat
                                viewModel.nuevoReporteLongitud.value = gLng
                                Toast.makeText(context, "📍 Ubicación GPS establecida", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTealDark)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Usar mi ubicación GPS actual", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "COORDENADAS DE LA INCIDENCIA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A8B88)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = String.format("%.6f", lat),
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            readOnly = true,
                            label = { Text("Latitud") }
                        )
                        OutlinedTextField(
                            value = String.format("%.6f", lng),
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
                        label = { Text("Dirección o Referencia *") },
                        placeholder = { Text("Ej: Calle 13 # 8-45, frente al parque") }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    GradientButton(
                        text = "Confirmar Ubicación (Paso 3) →",
                        enabled = direccion.isNotBlank(),
                        onClick = onNext
                    )
                }
            }
        }
    }
}

// ================== PASO 3: CÁMARA Y EVIDENCIA ADJUNTA (M01_05) ==================
@Composable
fun NuevoReportePaso3Screen(
    viewModel: CiudadanoViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val fotoBitmap by viewModel.fotoTomadaBitmap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Tomar foto con la cámara del dispositivo con validación de permisos en tiempo de ejecución
    val tomarFotoCamara = rememberCameraLauncher(
        onPhotoTaken = { bitmap ->
            viewModel.setFotoCamara(bitmap)
            Toast.makeText(context, "📸 Foto capturada con éxito", Toast.LENGTH_SHORT).show()
        }
    )

    // Seleccionar foto desde la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        viewModel.setFotoGaleria(bitmap)
                        Toast.makeText(context, "🖼️ Foto cargada de galería", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al abrir imagen: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
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

            // Visor de Foto o Estado Inicial
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .border(1.5.dp, Color(0xFF74A596), RoundedCornerShape(24.dp))
                    .clickable {
                        if (fotoBitmap == null) {
                            tomarFotoCamara()
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE4ECEB))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (fotoBitmap != null) {
                        Image(
                            bitmap = fotoBitmap!!.asImageBitmap(),
                            contentDescription = "Foto capturada",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandTealDark.copy(alpha = 0.9f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✓ Evidencia Adjunta Lista",
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
                                text = "Toca aquí para tomar foto",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTealDark
                            )
                            Text(
                                text = "O usa los botones de abajo para cámara o galería",
                                fontSize = 12.sp,
                                color = Color(0xFF6C757D)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botones: Tomar con Cámara y Elegir de Galería
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { tomarFotoCamara() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTealDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Tomar Foto", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCFD8DC)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Elegir Galería", color = BrandTealDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (fotoBitmap != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.eliminarFoto() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Eliminar Foto Adjunta", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                        text = "Protección de Datos y Privacidad",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La fotografía y las coordenadas se procesan de forma cifrada y son almacenadas con respaldo de trazabilidad institucional.",
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

            val radicadoTexto = reporte?.codigoSeguimiento ?: "RAD-2026-${String.format("%05d", reporte?.id ?: 1)}"
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, BrandCopper, RoundedCornerShape(12.dp)),
                color = Color(0xFFFFF8E1)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tag, contentDescription = null, tint = BrandCopper, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Código de Seguimiento: $radicadoTexto",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8D6E63)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0F2F1))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "PENDIENTE DE VALIDACIÓN • PRIORIDAD ASIGNADA: ${reporte?.prioridad ?: "BAJA"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00695C)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu reporte ha sido registrado con éxito. Se calculó su prioridad inicial según las incidencias del sector y un gestor municipal validará su pertinencia técnica.",
                fontSize = 13.sp,
                color = Color(0xFF5A6665),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            GradientButton(
                text = "Ver Seguimiento del Reporte",
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
