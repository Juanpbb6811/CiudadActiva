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

// ================== PANEL GLOBAL ADMIN (M04_01) ==================
@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel,
    onDirectorioUsuariosClick: () -> Unit,
    onCategoriasClick: () -> Unit,
    onAuditoriaClick: () -> Unit
) {
    val stats by viewModel.estadisticas.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(title = "Panel Global", roleTag = "ADMIN")

        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // 3 Tarjetas Superiores de Métricas
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

            // Incidencias por Categoría
            Card(
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Incidencias por Categoría", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)

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

            // Módulos Administrativos
            Text("Administración del Sistema", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A8B88))

            val menus = listOf(
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
