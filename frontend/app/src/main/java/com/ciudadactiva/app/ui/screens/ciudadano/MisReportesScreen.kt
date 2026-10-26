package com.ciudadactiva.app.ui.screens.ciudadano

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
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
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.components.PrioridadChip
import com.ciudadactiva.app.ui.components.StatusChip
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.CiudadanoViewModel

@Composable
fun MisReportesScreen(
    viewModel: CiudadanoViewModel,
    onVerReporteClick: (Long) -> Unit,
    onNuevoReporteClick: () -> Unit
) {
    val reportes by viewModel.misReportes.collectAsState()
    var filtroEstado by remember { mutableStateOf("TODOS") }

    val reportesFiltrados = remember(reportes, filtroEstado) {
        when (filtroEstado) {
            "PENDIENTES" -> reportes.filter { it.estado == "PENDIENTE" }
            "ATENCION" -> reportes.filter { it.estado == "ADMITIDO" || it.estado == "EN_PROCESO" || it.estado == "ATENDIDO" }
            "CERRADOS" -> reportes.filter { it.estado == "CERRADO" }
            "RECHAZADOS" -> reportes.filter { it.estado == "RECHAZADO" }
            else -> reportes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(title = "Mis Reportes")

        // Filtros horizontales (M01_07)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val opciones = listOf(
                "TODOS" to "Todos (${reportes.size})",
                "PENDIENTES" to "Pendientes",
                "ATENCION" to "En Atención",
                "CERRADOS" to "Cerrados",
                "RECHAZADOS" to "Rechazados"
            )

            items(opciones) { (clave, etiqueta) ->
                val isSelected = filtroEstado == clave
                FilterChip(
                    selected = isSelected,
                    onClick = { filtroEstado = clave },
                    label = { Text(etiqueta, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandTealDark,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (reportesFiltrados.isEmpty()) {
            // Estado vacío (M01_08)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No hay incidencias en esta bandeja",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandTealDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cuando registres una falla o problema en tu sector, aparecerá aquí con su estado y seguimiento en tiempo real.",
                        fontSize = 12.sp,
                        color = Color(0xFF78909C),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reportesFiltrados) { reporte ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(20.dp))
                            .clickable { onVerReporteClick(reporte.id) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${reporte.id} • ${reporte.categoriaNombre ?: "General"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78909C)
                                    )
                                    if (reporte.tieneFoto == true) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Con foto",
                                            tint = Color(0xFF00897B),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                StatusChip(status = reporte.estado)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = reporte.titulo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = reporte.direccion ?: "Sin dirección específica",
                                fontSize = 12.sp,
                                color = Color(0xFF6A7775)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PrioridadChip(prioridad = reporte.prioridad)

                                Text(
                                    text = "Ver trazabilidad →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandCopper
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
