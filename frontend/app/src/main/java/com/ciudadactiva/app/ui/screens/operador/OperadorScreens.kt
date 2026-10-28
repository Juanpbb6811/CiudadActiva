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
import androidx.compose.material.icons.filled.Save
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
import com.ciudadactiva.app.ui.components.EstadoSelectorItem
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.ciudadactiva.app.data.api.ApiClient
import com.ciudadactiva.app.ui.components.GradientButton
import com.ciudadactiva.app.ui.components.PrioridadChip
import com.ciudadactiva.app.ui.components.rememberCameraLauncher
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.OperadorViewModel

import com.ciudadactiva.app.data.model.ReporteResponse
import com.ciudadactiva.app.ui.components.StatusChip
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh

// ================== PANEL DE TRABAJO DEL OPERADOR (M03_01) ==================
@Composable
fun OperadorTareasScreen(
    viewModel: OperadorViewModel,
    onRegistrarAvanceClick: (Long) -> Unit,
    onCerrarReporteClick: (Long) -> Unit,
    onFichaClick: (Long) -> Unit
) {
    val tareas by viewModel.tareasAsignadas.collectAsState()
    val reportesCampo by viewModel.todosReportesCampo.collectAsState()
    val tabActivo by viewModel.tabSeleccionado.collectAsState()
    val filtroEstado by viewModel.filtroEstadoCampo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var reporteAEditar by remember { mutableStateOf<ReporteResponse?>(null) }
    var nuevoEstadoModal by remember { mutableStateOf("EN_PROCESO") }
    var observacionesModal by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarTareas()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(title = "Mis Labores", roleTag = "OPERADOR")

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Selector de Modo: Mis Asignaciones vs Todos los Reportes de Campo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEBEFF0))
                    .padding(4.dp)
            ) {
                val isMisAsig = tabActivo == "MIS_TAREAS"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isMisAsig) Color.White else Color.Transparent)
                        .clickable { viewModel.setTab("MIS_TAREAS") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mis Asignaciones (${tareas.size})",
                        fontSize = 11.sp,
                        fontWeight = if (isMisAsig) FontWeight.Bold else FontWeight.Normal,
                        color = if (isMisAsig) BrandTealDark else Color(0xFF607D8B)
                    )
                }

                val isTodos = tabActivo == "TODOS"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isTodos) Color.White else Color.Transparent)
                        .clickable { viewModel.setTab("TODOS") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reportes en Terreno (${reportesCampo.size})",
                        fontSize = 11.sp,
                        fontWeight = if (isTodos) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTodos) BrandTealDark else Color(0xFF607D8B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Si está en "TODOS", mostrar chips de filtro por estado
            if (tabActivo == "TODOS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filtros = listOf("TODOS", "ADMITIDO", "EN_PROCESO", "PENDIENTE", "ATENDIDO")
                    filtros.forEach { f ->
                        val isSel = filtroEstado == f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSel) BrandTealDark else Color.White)
                                .border(1.dp, if (isSel) BrandTealDark else Color(0xFFDCE2E1), RoundedCornerShape(14.dp))
                                .clickable { viewModel.setFiltroEstado(f) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = f,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else Color(0xFF5A6665)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ================= VISTA 1: MIS ASIGNACIONES =================
            if (tabActivo == "MIS_TAREAS") {
                if (tareas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFFB0BEC5), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No tienes tareas específicas asignadas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Puedes ver y atender los reportes activos del municipio en la pestaña 'Reportes en Terreno'.",
                                fontSize = 12.sp,
                                color = Color(0xFF78909C),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.setTab("TODOS") },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark, contentColor = Color.White),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Ver Reportes en Terreno →", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
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
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark, contentColor = Color.White),
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
            } else {
                // ================= VISTA 2: TODOS LOS REPORTES EN TERRENO =================
                if (reportesCampo.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFFB0BEC5), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No hay reportes con este estado", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(reportesCampo) { rep ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(3.dp, RoundedCornerShape(20.dp))
                                    .clickable { onFichaClick(rep.id) },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#${rep.id} • ${rep.categoriaNombre?.uppercase() ?: "GENERAL"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))
                                        StatusChip(status = rep.estado)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = rep.titulo,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${rep.direccion ?: "Sector Urbano"} • Reporta: ${rep.ciudadanoNombre ?: "Ciudadano"}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6C757D)
                                    )

                                    if (rep.tieneFoto == true || !rep.imagenUrl.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val url = if (rep.imagenUrl?.startsWith("http") == true) {
                                            rep.imagenUrl
                                        } else {
                                            "${ApiClient.getBaseUrl()}api/reportes/${rep.id}/foto"
                                        }
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Evidencia ciudadana",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                reporteAEditar = rep
                                                nuevoEstadoModal = rep.estado
                                                observacionesModal = ""
                                            },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp), tint = BrandTealDark)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Estado", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                                        }

                                        Button(
                                            onClick = { onRegistrarAvanceClick(rep.id) },
                                            modifier = Modifier.weight(1.3f).height(40.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark, contentColor = Color.White),
                                            shape = RoundedCornerShape(14.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Avance / Foto", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { onCerrarReporteClick(rep.id) },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B), contentColor = Color.White),
                                            shape = RoundedCornerShape(14.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Cerrar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

    // Modal para que el Operador actualice el estado directamente
    if (reporteAEditar != null) {
        AlertDialog(
            onDismissRequest = { reporteAEditar = null },
            title = { Text("Actualizar Estado Operativo #${reporteAEditar!!.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandTealDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Selecciona el nuevo estado conforme al avance en sitio:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    val opciones = listOf("EN_PROCESO", "ATENDIDO", "CERRADO")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        opciones.forEach { est ->
                            val isSel = nuevoEstadoModal == est
                            EstadoSelectorItem(
                                estadoKey = est,
                                isSelected = isSel,
                                onClick = { nuevoEstadoModal = est },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Detalle de las labores realizadas:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = observacionesModal,
                        onValueChange = { observacionesModal = it },
                        placeholder = { Text("Ej: Labores en sitio concluidas...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val repId = reporteAEditar!!.id
                        viewModel.cambiarEstado(repId, nuevoEstadoModal, observacionesModal) {
                            reporteAEditar = null
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
                TextButton(onClick = { reporteAEditar = null }) {
                    Text("Cancelar", color = Color(0xFF6C757D))
                }
            }
        )
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

                        Text("FOTO REPORTE ORIGINAL (CIUDADANO):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))
                        Spacer(modifier = Modifier.height(6.dp))
                        val fotoUrl = if (f.tieneFoto == true) "${ApiClient.getBaseUrl()}api/reportes/${f.id}/foto" else f.imagenUrl
                        if (!fotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = fotoUrl,
                                contentDescription = "Foto original del reporte",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F4)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
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
                                    Text("Sin foto adjunta por el ciudadano", fontSize = 12.sp, color = BrandTealDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Cambio rápido de estado en campo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.cambiarEstado(f.id, "EN_PROCESO", "Operador inició labores en terreno") {}
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1976D2))
                            ) {
                                Text("En Proceso", color = Color(0xFF1976D2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.cambiarEstado(f.id, "ATENDIDO", "Operador finalizó labores físicas en terreno") {}
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF388E3C))
                            ) {
                                Text("Atendido", color = Color(0xFF388E3C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
                            colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark, contentColor = Color.White),
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
    var fotoSoporte by remember { mutableStateOf<Bitmap?>(null) }
    var nuevoEstadoSeleccionado by remember { mutableStateOf("EN_PROCESO") }
    val isLoading by viewModel.isLoading.collectAsState()

    val tomarFotoLauncher = rememberCameraLauncher(
        onPhotoTaken = { bitmap ->
            fotoSoporte = bitmap
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp)
    ) {
        CiudadActivaTopBar(title = "Registrar Avance", roleTag = "OPERATIVO", onBackClick = onBackClick)

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("INCIDENCIA #$reporteId • REGISTRO DE CAMPO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))

                    Column {
                        Text("Observaciones Técnicas del Avance *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = observaciones,
                            onValueChange = { observaciones = it },
                            placeholder = { Text("Ej: Se inspeccionó el cableado y se reemplazó balastro defectuoso...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 5
                        )
                    }

                    // Fotografía de Soporte en Terreno (M03_04)
                    Column {
                        Text("Fotografía de Soporte en Terreno", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))

                        if (fotoSoporte != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.5.dp, BrandTealDark, RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    bitmap = fotoSoporte!!.asImageBitmap(),
                                    contentDescription = "Evidencia de avance",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Button(
                                    onClick = { tomarFotoLauncher() },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark, contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tomar otra foto", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF1F5F4))
                                    .border(1.5.dp, BrandTealDark.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .clickable { tomarFotoLauncher() },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tocar para tomar fotografía con la cámara",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandTealDark
                                    )
                                    Text(
                                        text = "Adjunta foto del trabajo realizado en sitio",
                                        fontSize = 10.sp,
                                        color = Color(0xFF78909C)
                                    )
                                }
                            }
                        }
                    }

                    // Transición de estado opcional
                    Column {
                        Text("Estado del Reporte tras este avance:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val opciones = listOf("EN_PROCESO" to "En Proceso", "ATENDIDO" to "Atendido (Listo)")
                            opciones.forEach { (est, label) ->
                                val isSel = nuevoEstadoSeleccionado == est
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) BrandTealDark else Color(0xFFF1F5F4))
                                        .clickable { nuevoEstadoSeleccionado = est }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSel) "$label ✓" else label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF5A6665)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    GradientButton(
                        text = "Guardar Avance en Trazabilidad",
                        enabled = observaciones.isNotBlank(),
                        isLoading = isLoading,
                        onClick = {
                            viewModel.registrarAvanceConEvidencia(
                                reporteId = reporteId,
                                observaciones = observaciones,
                                fotoSoporte = fotoSoporte,
                                nuevoEstado = nuevoEstadoSeleccionado,
                                onSuccess = onSuccess
                            )
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

    val tomarFotoSolucion = rememberCameraLauncher(
        onPhotoTaken = { bitmap ->
            fotoSolucion = bitmap
        }
    )

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
                    text = "Para proceder con el cierre de un reporte, es necesario registrar previamente al menos un avance de labores en terreno.",
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
                        Text("REQUISITO DE ATENCIÓN EN TERRENO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("✕ Avances del operador en seguimiento: 0 registros", fontSize = 12.sp, color = Color(0xFFD32F2F))
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
                            .clickable { tomarFotoSolucion() },
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
