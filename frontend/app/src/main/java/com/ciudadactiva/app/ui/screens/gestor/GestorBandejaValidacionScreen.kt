package com.ciudadactiva.app.ui.screens.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inbox
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
import com.ciudadactiva.app.ui.components.LoadingSkeleton
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.GestorViewModel

/**
 * M02_02: Bandeja de Triage y Validación del Gestor.
 * Permite filtrar por estado (Pendientes, Admitidos, Rechazados, Todos)
 * e inspeccionar cada reporte para validar, priorizar o cambiar de estado.
 */
@Composable
fun GestorBandejaValidacionScreen(
    viewModel: GestorViewModel,
    onInspeccionarReporte: (Long) -> Unit,
    onBackClick: () -> Unit = {}
) {
    val filtro by viewModel.filtroEstado.collectAsState()
    val reportes by viewModel.reportesFiltrados.collectAsState()
    val pendientes by viewModel.reportesPendientes.collectAsState()
    val stats by viewModel.estadisticas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val countPendientes = pendientes.size
    val countAdmitidos = stats?.reportesPorEstado?.get("ADMITIDO") ?: 0
    val countRechazados = stats?.reportesPorEstado?.get("RECHAZADO") ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(
            title = "Bandeja Triage",
            roleTag = "GESTOR",
            onBackClick = onBackClick
        )

        // Chips de Filtro (M02_02)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filtros = listOf(
                "PENDIENTE" to "Pendientes ($countPendientes)",
                "ADMITIDO" to "Admitidos ($countAdmitidos)",
                "RECHAZADO" to "Rechazados ($countRechazados)",
                "TODOS" to "Todos"
            )

            filtros.forEach { (key, label) ->
                val isSelected = filtro == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) BrandTealDark else Color.White)
                        .border(1.dp, if (isSelected) BrandTealDark else Color(0xFFDCE2E1), RoundedCornerShape(20.dp))
                        .clickable { viewModel.setFiltro(key) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF5A6665)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading && reportes.isEmpty()) {
            LoadingSkeleton()
        } else if (reportes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No hay incidencias en esta bandeja",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandTealDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No se encontraron reportes con el estado ${filtro.lowercase()}.",
                        fontSize = 12.sp,
                        color = Color(0xFF78909C)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(reportes) { rep ->
                    val esCritica = rep.prioridad == "CRITICA"
                    val esAlta = rep.prioridad == "ALTA"

                    val borderColor = when {
                        esCritica -> Color(0xFFEF9A9A)
                        esAlta -> Color(0xFFFFCC80)
                        rep.estado == "ADMITIDO" -> Color(0xFFA5D6A7)
                        rep.estado == "RECHAZADO" -> Color(0xFFEF9A9A)
                        else -> Color(0xFFE0E0E0)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
                            .shadow(3.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Cabecera: #ID • CATEGORÍA • PRIORIDAD
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${rep.id} • ${rep.categoriaNombre?.uppercase() ?: "GENERAL"} • ${rep.prioridad ?: "MEDIA"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esCritica) Color(0xFFD32F2F) else if (esAlta) BrandCopper else Color(0xFF7A8B88)
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (rep.estado) {
                                                "ADMITIDO" -> Color(0xFFE8F5E9)
                                                "RECHAZADO" -> Color(0xFFFFEBEE)
                                                "EN_PROCESO" -> Color(0xFFE3F2FD)
                                                "CERRADO" -> Color(0xFFF1F8E9)
                                                else -> Color(0xFFFFF3E0)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = rep.estado,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (rep.estado) {
                                            "ADMITIDO" -> Color(0xFF2E7D32)
                                            "RECHAZADO" -> Color(0xFFC62828)
                                            "EN_PROCESO" -> Color(0xFF1565C0)
                                            "CERRADO" -> Color(0xFF33691E)
                                            else -> Color(0xFFE65100)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = rep.titulo,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTealDark
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${rep.direccion ?: "Sector Urbano"} • Reporta: ${rep.ciudadanoNombre ?: "Ciudadano"} • ${rep.creadoEn?.take(10) ?: ""}",
                                fontSize = 12.sp,
                                color = Color(0xFF6C757D)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onInspeccionarReporte(rep.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Inspeccionar →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
