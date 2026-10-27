package com.ciudadactiva.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.AdminViewModel

import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning

// ================== PANEL GLOBAL ADMIN (M04_01) ==================
@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel,
    onDirectorioUsuariosClick: () -> Unit,
    onCategoriasClick: () -> Unit,
    onAuditoriaClick: () -> Unit,
    onPerfilClick: () -> Unit = {}
) {
    val stats by viewModel.estadisticas.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        CiudadActivaTopBar(
            title = "Panel Global",
            roleTag = "ADMIN",
            onProfileClick = onPerfilClick
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // 3 Tarjetas Superiores de Métricas (M04_01)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Card(modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(18.dp)), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("TOTAL CASOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90A4AE))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${stats?.totalReportes ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        Text("+12% mes", fontSize = 10.sp, color = Color(0xFF00796B), fontWeight = FontWeight.Bold)
                    }
                }

                Card(modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(18.dp)), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("RESUELTOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90A4AE))
                        Spacer(modifier = Modifier.height(4.dp))
                        val total = stats?.totalReportes ?: 1
                        val cerrados = stats?.reportesAtendidosOCerrados ?: 0
                        val pct = if (total > 0) (cerrados * 100 / total) else 0
                        Text("$pct%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
                        Text("Objetivo 85%", fontSize = 10.sp, color = Color(0xFF00796B))
                    }
                }

                Card(modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(18.dp)), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("TIEMPO PROM.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90A4AE))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${stats?.tiempoPromedioAtencionHoras ?: 0.0}h", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrandCopper)
                        Text("Atención", fontSize = 10.sp, color = Color(0xFF90A4AE))
                    }
                }
            }

            // ================== MATRIZ 1: PIPELINE Y ESTADOS DE REPORTES ==================
            Card(
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Matriz de Estados (Pipeline)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        Text("Total: ${stats?.totalReportes ?: 0}", fontSize = 11.sp, color = Color(0xFF78909C), fontWeight = FontWeight.SemiBold)
                    }

                    val totalReportes = stats?.totalReportes?.coerceAtLeast(1) ?: 1
                    val estadosPipeline = listOf(
                        Triple("PENDIENTE", "Pendiente de Triage", Color(0xFFE65100) to Color(0xFFFFF3E0)),
                        Triple("EN_REVISION", "En Revisión Técnica", Color(0xFFEF6C00) to Color(0xFFFFE0B2)),
                        Triple("ADMITIDO", "Admitido para Campo", Color(0xFF2E7D32) to Color(0xFFE8F5E9)),
                        Triple("EN_PROCESO", "En Proceso / Terreno", Color(0xFF1565C0) to Color(0xFFE3F2FD)),
                        Triple("ATENDIDO", "Atendido por Cuadrilla", Color(0xFF4527A0) to Color(0xFFEDE7F6)),
                        Triple("CERRADO", "Cerrado con Solución", Color(0xFF1B5E20) to Color(0xFFC8E6C9)),
                        Triple("RECHAZADO", "Rechazado (Regla 1)", Color(0xFFC62828) to Color(0xFFFFEBEE))
                    )

                    estadosPipeline.forEach { (estKey, estLabel, colorsPair) ->
                        val (textColor, bgColor) = colorsPair
                        val count = stats?.reportesPorEstado?.get(estKey) ?: 0
                        val ratio = (count.toFloat() / totalReportes.toFloat()).coerceIn(0f, 1f)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bgColor)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = estKey, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = estLabel, fontSize = 12.sp, color = TextDark)
                                }
                                Text(
                                    text = "$count (${(ratio * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTealDark
                                )
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                                color = textColor,
                                trackColor = Color(0xFFF1F5F4)
                            )
                        }
                    }
                }
            }

            // ================== MATRIZ 2: CRITICIDAD Y SEMÁFORO URBANO (RF03 / RF09) ==================
            Card(
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Matriz de Criticidad Urbana y Riesgo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)

                    // Semáforo Territorial Global
                    val semaforo = stats?.semaforoGlobal
                    val semColor = when (semaforo?.nivelAlerta) {
                        "ALTO", "CRITICO" -> Color(0xFFD32F2F)
                        "MEDIO", "MODERADO" -> Color(0xFFFFA000)
                        else -> Color(0xFF388E3C)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(semColor.copy(alpha = 0.12f))
                            .border(1.dp, semColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(semColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Semáforo Territorial: ${semaforo?.nivelAlerta ?: "BAJO"}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = semColor
                                )
                                Text(
                                    text = semaforo?.descripcion ?: "Nivel óptimo de atención en vías urbanas.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF455A64)
                                )
                            }
                        }
                    }

                    // Cuadrícula de Prioridades
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val prioridades = listOf(
                            Triple("CRITICA", Color(0xFFD32F2F), stats?.reportesPorPrioridad?.get("CRITICA") ?: 0),
                            Triple("ALTA", Color(0xFFE65100), stats?.reportesPorPrioridad?.get("ALTA") ?: 0),
                            Triple("MEDIA", Color(0xFFF57C00), stats?.reportesPorPrioridad?.get("MEDIA") ?: 0),
                            Triple("BAJA", Color(0xFF388E3C), stats?.reportesPorPrioridad?.get("BAJA") ?: 0)
                        )

                        prioridades.forEach { (prio, color, cant) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(prio, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("$cant", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
                                    Text("casos", fontSize = 9.sp, color = color)
                                }
                            }
                        }
                    }
                }
            }

            // ================== MATRIZ 3: DESEMPEÑO OPERATIVO Y SLA ==================
            Card(
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Matriz de Rendimiento y Cumplimiento SLA", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Tasa de Resolución", fontSize = 11.sp, color = Color(0xFF78909C))
                            val tot = stats?.totalReportes ?: 1
                            val res = stats?.reportesAtendidosOCerrados ?: 0
                            val pct = if (tot > 0) (res * 100 / tot) else 0
                            Text("$pct% / 85% meta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (pct >= 85) Color(0xFF2E7D32) else BrandCopper)
                        }

                        Column {
                            Text("Tiempo de Respuesta", fontSize = 11.sp, color = Color(0xFF78909C))
                            Text("${stats?.tiempoPromedioAtencionHoras ?: 0.0} horas prom.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        }

                        Column {
                            Text("Casos Pendientes", fontSize = 11.sp, color = Color(0xFF78909C))
                            Text("${stats?.reportesPendientes ?: 0} por validar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                    }
                }
            }

            // ================== MATRIZ 4: INCIDENCIAS POR CATEGORÍA ==================
            Card(
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Incidencias por Categoría", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)

                    stats?.reportesPorCategoria?.forEach { (cat, count) ->
                        val total = stats?.totalReportes?.coerceAtLeast(1) ?: 1
                        val pct = (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)

                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat, fontSize = 12.sp, color = TextDark)
                                Text("$count (${(pct * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = BrandTealDark,
                                trackColor = Color(0xFFEBEFF0)
                            )
                        }
                    }
                }
            }

            // ================== MÓDULOS ADMINISTRATIVOS Y PERFIL ==================
            Text("Administración del Sistema & Perfil", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))

            val menus = listOf(
                "Mi Perfil y Cambiar de Actor (RUP) →" to onPerfilClick,
                "Directorio de Usuarios y Roles →" to onDirectorioUsuariosClick,
                "Configurar Categorías de Incidencia →" to onCategoriasClick,
                "Matriz de Trazabilidad RUP (RNF04) →" to onAuditoriaClick
            )

            menus.forEach { (nombre, click) ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)).clickable(onClick = click),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ================== DIRECTORIO DE USUARIOS (M04_02) ==================
@Composable
fun AdminUsuariosScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    val usuarios by viewModel.usuarios.collectAsState()
    var mostrarModalCrear by remember { mutableStateOf(false) }

    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoEmail by remember { mutableStateOf("") }
    var nuevoPass by remember { mutableStateOf("123456") }
    var nuevoRol by remember { mutableStateOf("ROLE_OPERADOR") }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(title = "Directorio Usuarios", roleTag = "USUARIOS", onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(usuarios) { u ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(u.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(u.email, fontSize = 12.sp, color = Color(0xFF6C757D))
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE0F2F1))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(u.rol.replace("ROLE_", ""), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                            }
                        }

                        IconButton(onClick = { viewModel.desactivarUsuario(u.id) {} }) {
                            Icon(Icons.Default.Delete, contentDescription = "Desactivar", tint = Color(0xFFEF5350))
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            GradientButton(
                text = "+ Crear Nuevo Usuario",
                onClick = { mostrarModalCrear = true }
            )
        }
    }

    if (mostrarModalCrear) {
        AlertDialog(
            onDismissRequest = { mostrarModalCrear = false },
            title = { Text("Crear Usuario Administrativo", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = nuevoEmail, onValueChange = { nuevoEmail = it }, label = { Text("Correo") })
                    OutlinedTextField(value = nuevoPass, onValueChange = { nuevoPass = it }, label = { Text("Contraseña") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ROLE_GESTOR", "ROLE_OPERADOR", "ROLE_ADMIN").forEach { r ->
                            val isSel = nuevoRol == r
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) BrandTealDark else Color(0xFFEBEFF0))
                                    .clickable { nuevoRol = r }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(r.replace("ROLE_", ""), color = if (isSel) Color.White else TextDark, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.crearUsuario(nuevoNombre, nuevoEmail, nuevoPass, nuevoRol, null, null) {
                        mostrarModalCrear = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalCrear = false }) { Text("Cancelar") }
            }
        )
    }
}

// ================== CONFIGURAR CATEGORÍAS (M04_05) ==================
@Composable
fun AdminCategoriasScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    val categorias by viewModel.categorias.collectAsState()
    var mostrarModalCrear by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaDesc by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(title = "Categorías", roleTag = "CATÁLOGO", onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categorias) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandTealDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat.nombre.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(cat.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(cat.descripcion ?: "Catálogo oficial", fontSize = 11.sp, color = Color(0xFF6C757D))
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            GradientButton(
                text = "+ Crear Nueva Categoría",
                onClick = { mostrarModalCrear = true }
            )
        }
    }

    if (mostrarModalCrear) {
        AlertDialog(
            onDismissRequest = { mostrarModalCrear = false },
            title = { Text("Nueva Categoría", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = nuevaDesc, onValueChange = { nuevaDesc = it }, label = { Text("Descripción") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.crearCategoria(nuevoNombre, nuevaDesc, "traffic") {
                        mostrarModalCrear = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalCrear = false }) { Text("Cancelar") }
            }
        )
    }
}

// ================== MATRIZ DE TRAZABILIDAD RUP (M04_06) ==================
@Composable
fun AdminAuditoriaScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    val auditoria by viewModel.auditoriaTrazabilidad.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(title = "Auditoría RUP", roleTag = "RNF04", onBackClick = onBackClick)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "MATRIZ DE TRAZABILIDAD INMUTABLE (RNF04)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A8B88)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(auditoria) { seg ->
                    val isRechazo = seg.estadoNuevo == "RECHAZADO"

                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TRANSICIÓN: ${seg.estadoAnterior ?: "INICIO"} → ${seg.estadoNuevo}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRechazo) Color(0xFFC62828) else BrandTealDark
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Reporte #${seg.reporteId} • Usuario: ${seg.usuarioEmail ?: "sistema"}",
                                    fontSize = 12.sp,
                                    color = TextDark
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Hash: ${seg.hashTrazabilidad ?: "sha256:7f8a9e2b..."} • ${seg.creadoEn?.substring(0, 10) ?: ""}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF78909C)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isRechazo) Color(0xFFD32F2F) else Color(0xFF00796B))
                            )
                        }
                    }
                }
            }
        }
    }
}
