package com.ciudadactiva.app.ui.screens.ciudadano

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
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
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.components.LoadingSkeleton
import com.ciudadactiva.app.ui.components.PrioridadChip
import com.ciudadactiva.app.ui.components.StatusChip
import com.ciudadactiva.app.ui.theme.*
import com.ciudadactiva.app.ui.viewmodel.CiudadanoViewModel

@Composable
fun SeguimientoTimelineScreen(
    reporteId: Long,
    viewModel: CiudadanoViewModel,
    onComentariosClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val detalle by viewModel.reporteDetalle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(reporteId) {
        viewModel.cargarDetalleReporte(reporteId)
    }

    if (isLoading && detalle == null) {
        LoadingSkeleton()
        return
    }

    val rep = detalle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        CiudadActivaTopBar(
            title = "Seguimiento #${reporteId}",
            roleTag = rep?.estado ?: "PENDIENTE",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (rep != null) {
                // Tarjeta de Información General
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "${rep.categoriaNombre?.uppercase() ?: "GENERAL"} • ${rep.direccion ?: "Sector Urbano"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7A8B88)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = rep.titulo,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTealDark
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PrioridadChip(prioridad = rep.prioridad)
                                Text(
                                    text = if (!rep.operadorAsignadoNombre.isNullOrBlank()) "Asignado a: ${rep.operadorAsignadoNombre}" else "Sin operador asignado",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF5A6665)
                                )
                            }
                        }
                    }
                }

                // Si está rechazado, mostrar alerta obligatoria de Justificación (M01_12 - Regla 1)
                if (rep.estado == "RECHAZADO") {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFFEF9A9A), RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFD32F2F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Justificación Obligatoria del Gestor (Regla 1)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = rep.justificacionRechazo ?: "Incidencia no admitida para cuadrilla municipal.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF424242),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Sección Trazabilidad Inmutable
                item {
                    Text(
                        text = "TRAZABILIDAD INMUTABLE (RF07 / REGLA 5)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A8B88),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Lista vertical de checkpoints de seguimiento (M01_11)
                itemsIndexed(rep.lineaDeTiempo) { index, seg ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Columna del nodo y línea conectora
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val dotColor = when (seg.accion) {
                                "CREACION_REPORTE" -> BrandTealDark
                                "VALIDACION_ADMITIDA" -> BrandTealDark
                                "RECHAZO_REPORTE" -> Color(0xFFD32F2F)
                                "ASIGNACION_OPERADOR" -> BrandTealDark
                                "AVANCE_OPERATIVO" -> BrandCopper
                                "CIERRE_REPORTE" -> Color(0xFF2E7D32)
                                else -> BrandTealLight
                            }

                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .border(3.dp, Color.White, CircleShape)
                            )

                            if (index < rep.lineaDeTiempo.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(60.dp)
                                        .background(Color(0xFFCFD8DC))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Contenido del evento
                        Column(modifier = Modifier.weight(1f).padding(bottom = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = seg.accion?.replace("_", " ") ?: "Evento",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTealDark
                                )
                                Text(
                                    text = seg.creadoEn?.substring(0, 10) ?: "",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B9695)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = seg.observaciones ?: "Acción registrada en trazabilidad",
                                fontSize = 12.sp,
                                color = Color(0xFF424242),
                                lineHeight = 16.sp
                            )
                            if (seg.usuarioNombre != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Por: ${seg.usuarioNombre} (${seg.usuarioRol ?: "SISTEMA"})",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78909C)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Botón inferior para ir a comentarios (M01_13)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            GradientButton(
                text = "Dejar un comentario o retroalimentación →",
                onClick = { onComentariosClick(reporteId) }
            )
        }
    }
}
