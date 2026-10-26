package com.ciudadactiva.app.ui.screens.operador

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.components.PrioridadChip
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.OperadorViewModel

// ================== PANEL DE TRABAJO DEL OPERADOR (M03_01) ==================
@Composable
fun OperadorTareasScreen(
    viewModel: OperadorViewModel,
    onRegistrarAvanceClick: (Long) -> Unit,
    onCerrarReporteClick: (Long) -> Unit,
    onFichaClick: (Long) -> Unit
) {
    val tareas by viewModel.tareasAsignadas.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(title = "Mis Tareas", roleTag = "OPERADOR")

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "ORDENES DE SERVICIO PARA HOY (${tareas.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A8B88)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (tareas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFFB0BEC5), modifier = Modifier.size(60.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No tienes tareas pendientes asignadas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        Text("Las nuevas órdenes de trabajo de campo asignadas por los gestores aparecerán aquí.", fontSize = 12.sp, color = Color(0xFF78909C), textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(tareas) { tarea ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(3.dp, RoundedCornerShape(20.dp))
                                .clickable { onFichaClick(tarea.reporteId) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                PrioridadChip(prioridad = "ALTA")
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "#${tarea.reporteId} • ${tarea.reporteTitulo ?: "Incidencia en Campo"}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tarea.notas ?: "Atención y diagnóstico programado para cuadrilla",
                                    fontSize = 12.sp,
                                    color = Color(0xFF5A6665)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { onRegistrarAvanceClick(tarea.reporteId) },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandTealDark),
                                        shape = RoundedCornerShape(22.dp)
                                    ) {
                                        Text("Registrar Avance", color = BrandTealDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onCerrarReporteClick(tarea.reporteId) },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark),
                                        shape = RoundedCornerShape(22.dp)
                                    ) {
                                        Text("Cerrar con Foto →", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================== FICHA DE CAMPO CON GPS (M03_03) ==================
@Composable
fun OperadorFichaScreen(
    reporteId: Long,
    viewModel: OperadorViewModel,
    onRegistrarAvance: (Long) -> Unit,
    onCerrarReporte: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val ficha by viewModel.fichaSeleccionada.collectAsState()

    LaunchedEffect(reporteId) {
        viewModel.cargarFicha(reporteId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 30.dp)
    ) {
        CiudadActivaTopBar(title = "Ficha de Campo", roleTag = "#$reporteId", onBackClick = onBackClick)

        if (ficha != null) {
            val f = ficha!!
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("ORDEN DE TRABAJO EN TERRENO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(f.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        Text(f.direccion ?: "Ubicación en vía", fontSize = 13.sp, color = Color(0xFF5A6665))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón de GPS simulado
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEBE6DC))
                                .clickable { /* Simulación de GPS */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = BrandTealDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Tocar para abrir GPS / Navegación", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("FOTO REPORTE ORIGINAL:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F4))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Foto de evidencia original en BD (Disponible)", fontSize = 12.sp, color = BrandTealDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onRegistrarAvance(f.id) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandTealDark),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("+ Registrar Avance Operativo", color = BrandTealDark, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onCerrarReporte(f.id) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Proceder al Cierre con Evidencia", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ================== REGISTRAR AVANCE DE CAMPO (M03_04 - RF06) ==================
@Composable
fun OperadorAvanceScreen(
    reporteId: Long,
    viewModel: OperadorViewModel,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var observaciones by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        CiudadActivaTopBar(title = "Registrar Avance", roleTag = "RF06", onBackClick = onBackClick)

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("INCIDENCIA #$reporteId", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))

                    Column {
                        Text("Observaciones Técnicas del Avance *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = observaciones,
                            onValueChange = { observaciones = it },
                            placeholder = { Text("Ej: Se inspeccionó el cableado y se reemplazó balastro defectuoso...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 5
                        )
                    }

                    GradientButton(
                        text = "Guardar Avance en Trazabilidad",
                        enabled = observaciones.isNotBlank(),
                        isLoading = isLoading,
                        onClick = {
                            viewModel.registrarAvance(reporteId, observaciones, onSuccess)
                        }
                    )
                }
            }
        }
    }
}

// ================== CIERRE DE INCIDENCIA CON EVIDENCIA Y ALERTA REGLA 3 (M03_05 & M03_06) ==================
@Composable
fun OperadorCierreScreen(
    reporteId: Long,
    viewModel: OperadorViewModel,
    onCierreExitoso: () -> Unit,
    onIrAAvances: () -> Unit,
    onBackClick: () -> Unit
) {
    var observaciones by remember { mutableStateOf("") }
    var fotoSolucion by remember { mutableStateOf<Bitmap?>(null) }
    val regla3Bloqueada by viewModel.regla3Bloqueada.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoSolucion = bitmap
        }
    }

    // SI LA REGLA 3 FUE VIOLADA POR INTENTAR CERRAR SIN AVANCE PREVIO (M03_06)
    if (regla3Bloqueada) {
        Box(
            modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(46.dp))
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text("Acción Bloqueada", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"Un reporte NO PUEDE cerrarse si no registra previamente al menos una acción de atención por parte del operador asignado.\"",
                    fontSize = 13.sp,
                    color = Color(0xFF424242),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("VALIDACIÓN EN SERVIDOR (BusinessRuleException):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("✕ Avances del operador en Seguimiento: 0 registros", fontSize = 12.sp, color = Color(0xFFD32F2F))
                        Text("Debes registrar primero tus avances de trabajo en terreno.", fontSize = 12.sp, color = Color(0xFF5A6665))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                GradientButton(
                    text = "Registrar Avance Primero →",
                    onClick = onIrAAvances
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onBackClick) {
                    Text("Volver al Listado", color = BrandTealDark, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    // FORMULARIO DE CIERRE NORMAL (M03_05)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        CiudadActivaTopBar(title = "Cierre de Incidencia", roleTag = "CIERRE", onBackClick = onBackClick)

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Evidencia de Solución Obligatoria * (Cámara Móvil)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)

                    // Cuadro de fotografía
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .border(1.dp, Color(0xFF74A596), RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE4ECEB))
                            .clickable { cameraLauncher.launch() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoSolucion != null) {
                            Image(
                                bitmap = fotoSolucion!!.asImageBitmap(),
                                contentDescription = "Foto solución",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Toma la fotografía de la obra terminada", fontSize = 12.sp, color = BrandTealDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column {
                        Text("Detalle Técnico de Intervención *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = observaciones,
                            onValueChange = { observaciones = it },
                            placeholder = { Text("Ej: Cambio total de luminaria y pruebas de voltaje conformes...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    GradientButton(
                        text = "Finalizar y Cerrar Incidencia",
                        isLoading = isLoading,
                        enabled = observaciones.isNotBlank(),
                        onClick = {
                            viewModel.cerrarIncidenciaConEvidencia(
                                reporteId = reporteId,
                                observaciones = observaciones,
                                fotoSolucion = fotoSolucion,
                                onSuccess = onCierreExitoso,
                                onRegla3Violation = {}
                            )
                        }
                    )
                }
            }
        }
    }
}

// ================== HISTORIAL DE RESUELTOS (M03_07) ==================
@Composable
fun OperadorHistorialScreen(
    viewModel: OperadorViewModel,
    onBackClick: () -> Unit
) {
    val resueltos by viewModel.tareasResueltas.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(title = "Historial Resueltos", roleTag = "HISTORIAL", onBackClick = onBackClick)

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(resueltos) { rep ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("#${rep.id} • ${rep.categoriaNombre ?: "General"}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78909C))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("RESUELTO", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(rep.titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Cerrado: ${rep.fechaCierre?.substring(0, 10) ?: "Recientemente"}", fontSize = 11.sp, color = Color(0xFF8B9695))
                    }
                }
            }
        }
    }
}
