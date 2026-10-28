package com.ciudadactiva.app.ui.screens.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.ui.components.EstadoSelectorItem
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.components.LoadingSkeleton
import com.ciudadactiva.app.ui.components.StatusChip
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.GestorViewModel

@Composable
fun GestorFichaInspeccionScreen(
    reporteId: Long,
    viewModel: GestorViewModel,
    onAccionTerminada: () -> Unit,
    onBackClick: () -> Unit
) {
    val detalle by viewModel.reporteSeleccionado.collectAsState()
    val operadores by viewModel.operadoresDisponibles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()

    var prioridadSeleccionada by remember { mutableStateOf("ALTA") }
    var mostrarModalRechazo by remember { mutableStateOf(false) }
    var motivoRechazo by remember { mutableStateOf("") }

    var mostrarModalAsignar by remember { mutableStateOf(false) }
    var operadorSeleccionadoId by remember { mutableStateOf<Long?>(null) }
    var notasAsignacion by remember { mutableStateOf("") }

    // Validación y cambio de estado directo por el Gestor
    var mostrarModalCambiarEstado by remember { mutableStateOf(false) }
    var estadoSeleccionado by remember { mutableStateOf("ADMITIDO") }
    var observacionesCambioEstado by remember { mutableStateOf("") }

    LaunchedEffect(reporteId) {
        viewModel.cargarFichaInspeccion(reporteId)
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
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp)
    ) {
        CiudadActivaTopBar(
            title = "Inspección",
            roleTag = rep?.estado ?: "PENDIENTE",
            onBackClick = onBackClick
        )

        if (rep != null) {
            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Tarjeta Principal de la Ficha (M02_04)
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "${rep.categoriaNombre?.uppercase() ?: "GENERAL"} • RADICADO RECIENTE",
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

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Reportado por: ${rep.ciudadanoNombre ?: "Ciudadano Anónimo"}",
                            fontSize = 12.sp,
                            color = Color(0xFF5A6665)
                        )
                        Text(
                            text = "Dirección: ${rep.direccion ?: "Sector Urbano Central"}",
                            fontSize = 12.sp,
                            color = Color(0xFF5A6665)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = rep.descripcion,
                            fontSize = 13.sp,
                            color = Color(0xFF37474F),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Caja de Evidencia con Foto Real o Placeholder
                        val fotoUrl = if (rep.tieneFoto == true) "${ApiClient.getBaseUrl()}api/reportes/${rep.id}/foto" else rep.imagenUrl
                        if (!fotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = fotoUrl,
                                contentDescription = "Evidencia Fotográfica del Ciudadano",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE4ECEB)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE4ECEB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sin fotografía adjunta",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78909C)
                                )
                            }
                        }
                    }
                }

                // PRIORIDAD OFICIAL (EXCLUSIVA GESTOR - REGLA 2)
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PRIORIDAD OFICIAL ASIGNADA:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A8B88)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("BAJA", "MEDIA", "ALTA", "CRITICA").forEach { prio ->
                                val isSelected = prioridadSeleccionada == prio
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFFD4A373) else Color(0xFFF1F5F4))
                                        .clickable { prioridadSeleccionada = prio }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSelected) "$prio ✓" else prio,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF5A6665)
                                    )
                                }
                            }
                        }
                    }
                }

                // CONTROL DE ESTADO DIRECTO POR EL GESTOR
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ESTADO ACTUAL DEL REPORTE:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7A8B88)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE0F2F1))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rep.estado,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTealDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                estadoSeleccionado = rep.estado
                                observacionesCambioEstado = ""
                                mostrarModalCambiarEstado = true
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandTealDark),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTealDark)
                        ) {
                            Text("Cambiar Estado del Reporte...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = Color(0xFFD32F2F), fontSize = 12.sp)
                }

                // Botones de Acción Rápida (Rechazar o Admitir)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { mostrarModalRechazo = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF9A9A)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Rechazar (Just.)", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.admitirYPriorizarReporte(
                                reporteId = rep.id,
                                prioridad = prioridadSeleccionada,
                                justificacion = "Incidencia admitida por Gestor",
                                onSuccess = { mostrarModalAsignar = true }
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark, contentColor = Color.White),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Admitir Reporte", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Rechazo con Justificación Obligatoria (M02_05 - Regla 1)
    if (mostrarModalRechazo) {
        AlertDialog(
            onDismissRequest = { mostrarModalRechazo = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rechazar Incidencia #$reporteId", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Motivo Técnico del Rechazo (Obligatorio):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = { motivoRechazo = it },
                        placeholder = { Text("Ej: El predio reportado es de carácter privado...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rechazarReporte(reporteId, motivoRechazo) {
                            mostrarModalRechazo = false
                            onAccionTerminada()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Confirmar Rechazo")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalRechazo = false }) {
                    Text("Cancelar", color = Color(0xFF6C757D))
                }
            }
        )
    }

    // Modal Asignar Operador (M02_07 - Regla 5)
    if (mostrarModalAsignar) {
        AlertDialog(
            onDismissRequest = { mostrarModalAsignar = false },
            title = {
                Text("Asignar Operador en Campo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SELECCIONAR OPERADOR EN CAMPO:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A8B88)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    operadores.forEach { op ->
                        val isSelected = operadorSeleccionadoId == op.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, if (isSelected) BrandTealDark else Color(0xFFDCE2E1), RoundedCornerShape(12.dp))
                                .clickable { operadorSeleccionadoId = op.id },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE8F0EF) else Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { operadorSeleccionadoId = op.id }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(op.nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                                    Text("${op.laboresActivas ?: 0} labores activas en campo", fontSize = 11.sp, color = Color(0xFF5A6665))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Instrucciones para la cuadrilla:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notasAsignacion,
                        onValueChange = { notasAsignacion = it },
                        placeholder = { Text("Ej: Coordinar con volqueta recolectora...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val opId = operadorSeleccionadoId ?: operadores.firstOrNull()?.id
                        if (opId != null) {
                            viewModel.asignarOperador(reporteId, opId, notasAsignacion) {
                                mostrarModalAsignar = false
                                onAccionTerminada()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirmar Asignación", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalAsignar = false }) {
                    Text("Omitir Asignación")
                }
            }
        )
    }

    // Modal para que el Gestor cambie el estado del reporte con observaciones
    if (mostrarModalCambiarEstado) {
        val estadosDisponibles = listOf("PENDIENTE", "EN_REVISION", "ADMITIDO", "EN_PROCESO", "ATENDIDO", "CERRADO", "RECHAZADO")
        AlertDialog(
            onDismissRequest = { mostrarModalCambiarEstado = false },
            title = {
                Text("Actualizar Estado del Reporte", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Selecciona el nuevo estado oficial:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        estadosDisponibles.chunked(2).forEach { rowEstados ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                rowEstados.forEach { est ->
                                    val isSel = estadoSeleccionado == est
                                    EstadoSelectorItem(
                                        estadoKey = est,
                                        isSelected = isSel,
                                        onClick = { estadoSeleccionado = est },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowEstados.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Observación o Justificación del Cambio:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = observacionesCambioEstado,
                        onValueChange = { observacionesCambioEstado = it },
                        placeholder = { Text("Ej: Aprobado tras validación técnica en terreno...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val obs = if (observacionesCambioEstado.isBlank()) "Transición de estado a ${estadoSeleccionado.replace("_", " ")} por Gestor" else observacionesCambioEstado
                        viewModel.cambiarEstadoReporte(reporteId, estadoSeleccionado, obs) {
                            mostrarModalCambiarEstado = false
                            onAccionTerminada()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Estado", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalCambiarEstado = false }) {
                    Text("Cancelar", color = Color(0xFF6C757D))
                }
            }
        )
    }
}
