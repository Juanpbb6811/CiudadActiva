package com.ciudadactiva.app.ui.screens.ciudadano

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
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
    val comentarios by viewModel.comentariosList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var nuevoComentarioTexto by remember { mutableStateOf("") }

    LaunchedEffect(reporteId) {
        viewModel.cargarDetalleReporte(reporteId)
    }

    if (isLoading && detalle == null) {
        LoadingSkeleton()
        return
    }

    val rep = detalle

    fun resolverUrlFoto(url: String?): String? {
        if (url.isNullOrBlank()) return null
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("data:")) return url
        val baseUrl = CiudadActivaApplication.sessionManager.getBaseUrl().trimEnd('/')
        val path = url.trimStart('/')
        return "$baseUrl/$path"
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (rep != null) {
                // Tarjeta Principal de Información
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${rep.categoriaNombre?.uppercase() ?: "GENERAL"} • ${rep.direccion ?: "Sector Urbano"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7A8B88)
                                )
                                StatusChip(status = rep.estado)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = rep.titulo,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTealDark
                            )

                            if (!rep.descripcion.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = rep.descripcion,
                                    fontSize = 13.sp,
                                    color = Color(0xFF424242),
                                    lineHeight = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Foto del reporte si existe
                            val fotoUrlCompleta = resolverUrlFoto(rep.imagenUrl)
                            if (!fotoUrlCompleta.isNullOrBlank()) {
                                AsyncImage(
                                    model = fotoUrlCompleta,
                                    contentDescription = "Foto de la incidencia",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFE0E0E0)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF7A8B88),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reportado por: ${rep.ciudadanoNombre ?: "Ciudadano"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF7A8B88)
                                    )
                                }
                                PrioridadChip(prioridad = rep.prioridad)
                            }
                        }
                    }
                }

                // Justificación de rechazo si aplica
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
                                        text = "Justificación del Gestor",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = rep.justificacionRechazo ?: "Incidencia no admitida para cuadrilla municipal.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF424242)
                                )
                            }
                        }
                    }
                }

                // Trazabilidad
                item {
                    Text(
                        text = "HISTORIAL Y TRAZABILIDAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A8B88),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                itemsIndexed(rep.lineaDeTiempo) { index, seg ->
                    Row(modifier = Modifier.fillMaxWidth()) {
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
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .border(2.dp, Color.White, CircleShape)
                            )

                            if (index < rep.lineaDeTiempo.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(50.dp)
                                        .background(Color(0xFFCFD8DC))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f).padding(bottom = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = seg.accion?.replace("_", " ") ?: "Evento",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTealDark
                                )
                                Text(
                                    text = seg.creadoEn?.substring(0, 10) ?: "",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B9695)
                                )
                            }
                            Text(
                                text = seg.observaciones ?: "Acción registrada en trazabilidad",
                                fontSize = 12.sp,
                                color = Color(0xFF424242)
                            )
                        }
                    }
                }

                // SECCIÓN COMENTARIOS Y FEEDBACK COMUNITARIO
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMENTARIOS DE LA COMUNIDAD (${comentarios.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTealDark
                        )
                        Text(
                            text = "Ver todos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandCopper,
                            modifier = Modifier.clickable { onComentariosClick(reporteId) }
                        )
                    }
                }

                if (comentarios.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F8F7))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Aún no hay comentarios en este reporte.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7A8B88)
                                )
                                Text(
                                    text = "Sé el primero en dejar una nota o actualización a la comunidad.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    }
                } else {
                    items(comentarios) { com ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val initials = com.usuarioNombre.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(BrandTealDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(initials, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = com.usuarioNombre,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = com.creadoEn?.substring(0, 10) ?: "",
                                            fontSize = 10.sp,
                                            color = Color(0xFF8B9695)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\"${com.contenido}\"",
                                    fontSize = 12.sp,
                                    color = Color(0xFF424242)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Formulario inferior para publicar comentario directo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nuevoComentarioTexto,
                    onValueChange = { nuevoComentarioTexto = it },
                    placeholder = { Text("Escribe un comentario sobre este reporte...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (nuevoComentarioTexto.isNotBlank()) {
                            viewModel.agregarComentario(reporteId, nuevoComentarioTexto.trim())
                            nuevoComentarioTexto = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (nuevoComentarioTexto.isNotBlank()) BrandTealDark else Color(0xFFCFD8DC))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
