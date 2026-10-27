package com.ciudadactiva.app.ui.screens.ciudadano

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.data.model.ReporteResponse
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.components.LocationHelper
import com.ciudadactiva.app.ui.components.OpenStreetMapComposable
import com.ciudadactiva.app.ui.components.PrioridadChip
import com.ciudadactiva.app.ui.components.StatusChip
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.CiudadanoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaExploradorScreen(
    viewModel: CiudadanoViewModel,
    onVerReporteDetalle: (Long) -> Unit
) {
    val context = LocalContext.current
    val reportes by viewModel.reportesCercanos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoriaId by remember { mutableStateOf<Long?>(null) }
    var selectedReporte by remember { mutableStateOf<ReporteResponse?>(null) }

    // Ubicación GPS del usuario
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var centerLat by remember { mutableStateOf(4.60971) }
    var centerLng by remember { mutableStateOf(-74.08175) }
    var zoomLevel by remember { mutableStateOf(14) }

    // Launcher de Permisos de Ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            LocationHelper.getCurrentLocation(
                context = context,
                onLocationReceived = { lat, lng ->
                    userLocation = Pair(lat, lng)
                    centerLat = lat
                    centerLng = lng
                    zoomLevel = 15
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Se requiere permiso de ubicación para ver tu posición", Toast.LENGTH_SHORT).show()
        }
    }

    // Solicitar ubicación al iniciar la pantalla
    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            LocationHelper.getCurrentLocation(
                context = context,
                onLocationReceived = { lat, lng ->
                    userLocation = Pair(lat, lng)
                    centerLat = lat
                    centerLng = lng
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
    }

    // Filtrar reportes por búsqueda de texto y por categoría seleccionada
    val filteredReportes = remember(reportes, searchQuery, selectedCategoriaId) {
        reportes.filter { rep ->
            val matchQuery = searchQuery.isBlank() ||
                rep.titulo.contains(searchQuery, ignoreCase = true) ||
                (rep.descripcion ?: "").contains(searchQuery, ignoreCase = true) ||
                (rep.direccion ?: "").contains(searchQuery, ignoreCase = true) ||
                (rep.categoriaNombre ?: "").contains(searchQuery, ignoreCase = true)

            val matchCat = selectedCategoriaId == null || rep.categoriaId == selectedCategoriaId

            matchQuery && matchCat
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa 100% Gratuito OpenStreetMap con Leaflet
        OpenStreetMapComposable(
            modifier = Modifier.fillMaxSize(),
            centerLat = centerLat,
            centerLng = centerLng,
            zoom = zoomLevel,
            userLocation = userLocation,
            reportes = filteredReportes,
            isPickerMode = false,
            onReportClick = { repId ->
                val found = reportes.firstOrNull { it.id == repId }
                if (found != null) {
                    selectedReporte = found
                } else {
                    onVerReporteDetalle(repId)
                }
            }
        )

        // Barra superior de búsqueda y filtros
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF7A8B88))
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por dirección o tipo...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BrandTealDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtro", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips con conteos de semáforo interactivos
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val isTodos = selectedCategoriaId == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isTodos) BrandTealDark else Color.White.copy(alpha = 0.95f))
                            .clickable { selectedCategoriaId = null }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Todos (${reportes.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTodos) Color.White else TextDark
                        )
                    }
                }

                items(categorias) { cat ->
                    val isSelected = selectedCategoriaId == cat.id
                    val count = reportes.count { it.categoriaId == cat.id }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) BrandTealDark else Color.White.copy(alpha = 0.95f))
                            .clickable {
                                selectedCategoriaId = if (isSelected) null else cat.id
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(cat.semaforo?.toComposeColor() ?: Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${cat.nombre} ($count)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else TextDark
                            )
                        }
                    }
                }
            }
        }

        // Botón Flotante para Centrar en Mi Ubicación GPS
        FloatingActionButton(
            onClick = {
                if (LocationHelper.hasLocationPermission(context)) {
                    LocationHelper.getCurrentLocation(
                        context = context,
                        onLocationReceived = { lat, lng ->
                            userLocation = Pair(lat, lng)
                            centerLat = lat
                            centerLng = lng
                            zoomLevel = 16
                            Toast.makeText(context, "📍 Centrado en tu ubicación", Toast.LENGTH_SHORT).show()
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
                .align(Alignment.BottomEnd)
                .padding(bottom = if (selectedReporte != null) 280.dp else 90.dp, end = 16.dp)
                .size(48.dp),
            containerColor = Color.White,
            contentColor = BrandTealDark,
            shape = CircleShape
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación", modifier = Modifier.size(24.dp))
        }

        // BottomSheet Preview del Reporte seleccionado (M01_10)
        if (selectedReporte != null) {
            val rep = selectedReporte!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp)
                    .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INCIDENCIA #${rep.id} • ${rep.categoriaNombre?.uppercase() ?: "GENERAL"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A8B88)
                        )
                        IconButton(onClick = { selectedReporte = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                        }
                    }

                    Text(
                        text = rep.titulo,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        text = "📍 ${rep.direccion ?: "Sector urbano registrado"}",
                        fontSize = 12.sp,
                        color = Color(0xFF6C757D)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(status = rep.estado)
                        PrioridadChip(prioridad = rep.prioridad)
                    }

                    // Urgencia acumulada por proximidad
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF4F7F6))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "📊 ${rep.reportesCercanos ?: 1} reportes acumulados en esta misma zona. ${rep.semaforoDescripcion ?: ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandTealDark
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    GradientButton(
                        text = "Ver Trazabilidad y Comentarios 💬 →",
                        onClick = {
                            val id = rep.id
                            selectedReporte = null
                            onVerReporteDetalle(id)
                        }
                    )
                }
            }
        }
    }
}
