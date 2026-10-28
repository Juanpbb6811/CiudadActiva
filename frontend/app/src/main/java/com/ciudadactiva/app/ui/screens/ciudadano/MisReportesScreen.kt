package com.ciudadactiva.app.ui.screens.ciudadano

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.data.model.ReporteResponse
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
    val misReportes by viewModel.misReportes.collectAsState()
    val reportesComunidad by viewModel.reportesCercanos.collectAsState()

    var reporteParaEliminar by remember { mutableStateOf<ReporteResponse?>(null) }
    val currentUserId = remember { com.ciudadactiva.app.CiudadActivaApplication.sessionManager.getUserId() }
    val currentUserRole = remember { com.ciudadactiva.app.CiudadActivaApplication.sessionManager.getRol() }

    LaunchedEffect(Unit) {
        viewModel.cargarDatosHome()
    }

    if (reporteParaEliminar != null) {
        val rep = reporteParaEliminar!!
        AlertDialog(
            onDismissRequest = { reporteParaEliminar = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFD32F2F)) },
            title = { Text("¿Retirar este reporte?", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Estás seguro de que deseas retirar el reporte \"${rep.titulo}\"? Como se encuentra en estado PENDIENTE, se retirará y eliminará permanentemente del sistema.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idAEliminar = rep.id
                        reporteParaEliminar = null
                        viewModel.eliminarReporte(idAEliminar)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Sí, retirar reporte", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reporteParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    var pestanaSeleccionada by remember { mutableStateOf(0) } // 0: Comunidad, 1: Mis Reportes
    var filtroEstado by remember { mutableStateOf("TODOS") }

    val reportesActivos = if (pestanaSeleccionada == 0) reportesComunidad else misReportes

    val reportesFiltrados = remember(reportesActivos, filtroEstado) {
        when (filtroEstado) {
            "PENDIENTES" -> reportesActivos.filter { it.estado == "PENDIENTE" }
            "ATENCION" -> reportesActivos.filter { it.estado == "ADMITIDO" || it.estado == "EN_PROCESO" || it.estado == "ATENDIDO" }
            "CERRADOS" -> reportesActivos.filter { it.estado == "CERRADO" }
            "RECHAZADOS" -> reportesActivos.filter { it.estado == "RECHAZADO" }
            else -> reportesActivos
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(bottom = 75.dp)
    ) {
        CiudadActivaTopBar(title = if (pestanaSeleccionada == 0) "Muro Comunitario" else "Mis Reportes")

        // Selector de Pestañas: Comunidad vs Mis Reportes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE5ECEB))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (pestanaSeleccionada == 0) Color.White else Color.Transparent)
                    .clickable { pestanaSeleccionada = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = if (pestanaSeleccionada == 0) BrandTealDark else Color(0xFF6A7775),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Comunidad (${reportesComunidad.size})",
                        fontSize = 12.sp,
                        fontWeight = if (pestanaSeleccionada == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (pestanaSeleccionada == 0) BrandTealDark else Color(0xFF6A7775)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (pestanaSeleccionada == 1) Color.White else Color.Transparent)
                    .clickable { pestanaSeleccionada = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (pestanaSeleccionada == 1) BrandTealDark else Color(0xFF6A7775),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mis Reportes (${misReportes.size})",
                        fontSize = 12.sp,
                        fontWeight = if (pestanaSeleccionada == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (pestanaSeleccionada == 1) BrandTealDark else Color(0xFF6A7775)
                    )
                }
            }
        }

        // Filtros horizontales por estado
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val opciones = listOf(
                "TODOS" to "Todos (${reportesActivos.size})",
                "PENDIENTES" to "Pendientes",
                "ATENCION" to "En Atención",
                "CERRADOS" to "Cerrados"
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

        Spacer(modifier = Modifier.height(4.dp))

        if (reportesFiltrados.isEmpty()) {
            // Estado vacío amigable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (pestanaSeleccionada == 0) Icons.Default.ChatBubbleOutline else Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (pestanaSeleccionada == 0) "No hay reportes comunitarios con este filtro" else "Aún no has creado reportes propios",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (pestanaSeleccionada == 0) "Prueba seleccionando otro filtro de estado." else "¡Explora lo que reportan otros vecinos en la pestaña 'Comunidad' o crea una incidencia con el botón (+)!",
                        fontSize = 12.sp,
                        color = Color(0xFF7A8B88),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reportesFiltrados) { reporte ->
                    val esAutor = (reporte.ciudadanoId == currentUserId || currentUserRole == "ROLE_ADMIN")
                    val puedeEliminar = esAutor && reporte.estado == "PENDIENTE"
                    ReporteFeedItemCard(
                        reporte = reporte,
                        onClick = { onVerReporteClick(reporte.id) },
                        onEliminarClick = if (puedeEliminar) { { reporteParaEliminar = reporte } } else null
                    )
                }
            }
        }
    }
}

@Composable
fun ReporteFeedItemCard(
    reporte: ReporteResponse,
    onClick: () -> Unit,
    onEliminarClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (reporte.categoriaIcono) {
                                "road" -> Icons.Default.DirectionsCar
                                "lightbulb" -> Icons.Default.Lightbulb
                                "trash" -> Icons.Default.Delete
                                "park" -> Icons.Default.Park
                                "water" -> Icons.Default.WaterDrop
                                else -> Icons.Default.ReportProblem
                            },
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = reporte.categoriaNombre ?: "General",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "Reportado por: ${reporte.ciudadanoNombre ?: "Ciudadano"}",
                            fontSize = 11.sp,
                            color = Color(0xFF8B9695)
                        )
                    }
                }
                StatusChip(status = reporte.estado)
            }

            Spacer(modifier = Modifier.height(10.dp))

            val radicado = reporte.codigoSeguimiento ?: "RAD-2026-${String.format("%05d", reporte.id)}"
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5F4),
                border = BorderStroke(1.dp, Color(0xFFDCE2E1))
            ) {
                Text(
                    text = radicado,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00695C),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = reporte.titulo,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!reporte.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reporte.descripcion,
                    fontSize = 12.sp,
                    color = Color(0xFF555555),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF8B9695),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reporte.ubicacionLegible,
                    fontSize = 11.sp,
                    color = Color(0xFF7A8B88),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F4F3))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrioridadChip(prioridad = reporte.prioridad)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onEliminarClick != null) {
                        IconButton(
                            onClick = onEliminarClick,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Retirar reporte",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0F2F1))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comentar",
                            tint = BrandTealDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ver y Comentar 💬",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTealDark
                        )
                    }
                }
            }
        }
    }
}
