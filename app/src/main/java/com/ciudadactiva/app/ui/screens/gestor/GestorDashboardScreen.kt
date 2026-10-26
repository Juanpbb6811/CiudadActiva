package com.ciudadactiva.app.ui.screens.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.GestorViewModel

@Composable
fun GestorDashboardScreen(
    viewModel: GestorViewModel,
    onVerBandejaValidacion: () -> Unit,
    onVerAsignaciones: () -> Unit,
    onVerEstadisticas: () -> Unit,
    onInspeccionarReporte: (Long) -> Unit
) {
    val pendientes by viewModel.reportesPendientes.collectAsState()
    val asignaciones by viewModel.asignacionesActivas.collectAsState()
    val stats by viewModel.estadisticas.collectAsState()

    val reporteCritico = remember(pendientes) {
        pendientes.firstOrNull { it.prioridad == "CRITICA" } ?: pendientes.firstOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        CiudadActivaTopBar(
            title = "Triage Operativo",
            roleTag = "GESTOR"
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Fila de 3 Indicadores Rápidos (M02_01)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pendientes por Validar
                Card(
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("PENDIENTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90A4AE))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${pendientes.size}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        Text("Por Validar", fontSize = 11.sp, color = Color(0xFFD32F2F))
                    }
                }

                // Por Asignar
                Card(
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("POR ASIGNAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90A4AE))
                        Spacer(modifier = Modifier.height(4.dp))
                        val admitidosCount = stats?.reportesPorEstado?.get("ADMITIDO") ?: 0
                        Text("$admitidosCount", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = BrandCopper)
                        Text("Admitidos", fontSize = 11.sp, color = BrandCopper)
                    }
                }

                // En Campo
                Card(
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("EN CAMPO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90A4AE))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${asignaciones.size}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        Text("En Proceso", fontSize = 11.sp, color = BrandTealDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // REQUIERE ATENCIÓN INMEDIATA (TRIAGE)
            Text(
                text = "REQUIERE ATENCIÓN INMEDIATA (TRIAGE)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A8B88)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (reporteCritico != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFFEF9A9A), RoundedCornerShape(20.dp))
                        .shadow(3.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "${reporteCritico.prioridad ?: "MEDIA"} • PENDIENTE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reporteCritico.titulo,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${reporteCritico.direccion ?: "Sector Urbano"} • Requiere validación oficial",
                            fontSize = 12.sp,
                            color = Color(0xFF6C757D)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onInspeccionarReporte(reporteCritico.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Validar y Asignar →", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "✓ No hay incidencias urgentes pendientes de validación.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ACCIONES DE GESTIÓN (M02_01)
            Text(
                text = "ACCIONES DE GESTIÓN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A8B88)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val acciones = listOf(
                "Bandeja Completa de Validación (${pendientes.size})" to onVerBandejaValidacion,
                "Gestionar Asignaciones de Cuadrillas (${asignaciones.size})" to onVerAsignaciones,
                "Tablero Estadístico y Semáforos" to onVerEstadisticas
            )

            acciones.forEach { (titulo, accion) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clickable(onClick = accion),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = titulo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTealDark
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = BrandTealDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
