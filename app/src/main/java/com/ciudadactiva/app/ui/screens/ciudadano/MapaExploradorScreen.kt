package com.ciudadactiva.app.ui.screens.ciudadano

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.data.model.ReporteResponse
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.components.PrioridadChip
import com.ciudadactiva.app.ui.components.StatusChip
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.CiudadanoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaExploradorScreen(
    viewModel: CiudadanoViewModel,
    onVerReporteDetalle: (Long) -> Unit
) {
    val reportes by viewModel.reportesCercanos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedReporte by remember { mutableStateOf<ReporteResponse?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo simulado de mapa urbano con cuadrícula
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEBE6DC))
        ) {
            // Representación de calles principales
            Box(modifier = Modifier.fillMaxWidth().height(28.dp).offset(y = 220.dp).background(Color(0xFFFAF8F5)))
            Box(modifier = Modifier.fillMaxWidth().height(28.dp).offset(y = 480.dp).background(Color(0xFFFAF8F5)))
            Box(modifier = Modifier.fillMaxHeight().width(28.dp).offset(x = 110.dp).background(Color(0xFFFAF8F5)))
            Box(modifier = Modifier.fillMaxHeight().width(28.dp).offset(x = 280.dp).background(Color(0xFFFAF8F5)))

            // Pin de usuario (Cra 5 #12-30)
            Box(
                modifier = Modifier
                    .offset(x = 180.dp, y = 360.dp)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(BrandTealDark.copy(alpha = 0.15f)),
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

            // Pines de incidencias con colores semáforo
            reportes.forEachIndexed { index, rep ->
                val posX = (60 + (index * 85) % 300).dp
                val posY = (180 + (index * 130) % 450).dp
                val pinColor = when (rep.prioridad) {
                    "CRITICA" -> Color(0xFFF44336) // Rojo semáforo
                    "ALTA" -> Color(0xFFFF9800) // Naranja semáforo
                    "MEDIA" -> Color(0xFFFFEB3B) // Amarillo semáforo
                    else -> Color(0xFF4CAF50) // Verde semáforo
                }

                Box(
                    modifier = Modifier
                        .offset(x = posX, y = posY)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(pinColor.copy(alpha = 0.3f))
                        .clickable { selectedReporte = rep },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(pinColor)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        // Barra superior de búsqueda y filtros (M01_09)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(26.dp)),
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
                        placeholder = { Text("Buscar por barrio, dirección o código...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandTealDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtro", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips con conteos de semáforo
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categorias) { cat ->
                    val colorPin = cat.semaforo?.toComposeColor() ?: Color(0xFF4CAF50)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colorPin))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${cat.nombre} (${cat.totalReportes ?: 0})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }
                    }
                }
            }
        }

        // BottomSheet Preview del Reporte seleccionado (M01_10)
        if (selectedReporte != null) {
            val rep = selectedReporte!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 40.dp, height = 4.dp)
                            .background(Color(0xFFCFD8DC), RoundedCornerShape(2.dp))
                    )

                    Text(
                        text = "INCIDENCIA #${rep.id} • ${rep.categoriaNombre?.uppercase() ?: "GENERAL"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A8B88)
                    )

                    Text(
                        text = rep.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        text = rep.direccion ?: "Ubicación en mapa",
                        fontSize = 12.sp,
                        color = Color(0xFF6C757D)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(status = rep.estado)
                        PrioridadChip(prioridad = rep.prioridad)
                    }

                    if (!rep.operadorAsignadoNombre.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF1F5F4))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Operador Asignado: ${rep.operadorAsignadoNombre}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTealDark
                                )
                                Text(
                                    text = "Labores de atención en curso en terreno",
                                    fontSize = 11.sp,
                                    color = Color(0xFF5A6665)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    GradientButton(
                        text = "Ver Historial Completo (RF07) →",
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
