package com.ciudadactiva.app.ui.screens.ciudadano

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.CiudadanoViewModel

@Composable
fun ComentariosScreen(
    reporteId: Long,
    viewModel: CiudadanoViewModel,
    onBackClick: () -> Unit
) {
    val comentarios by viewModel.comentariosList.collectAsState()
    var nuevoTexto by remember { mutableStateOf("") }
    val currentUserId = CiudadActivaApplication.sessionManager.getUserId()

    LaunchedEffect(reporteId) {
        viewModel.cargarDetalleReporte(reporteId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        CiudadActivaTopBar(
            title = "Comentarios",
            roleTag = "#$reporteId",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(comentarios) { com ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Círculo con iniciales
                            val initials = com.usuarioNombre.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BrandTealDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${com.usuarioNombre} (${com.usuarioRol?.replace("ROLE_", "")?.lowercase()?.capitalize() ?: "Usuario"})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = com.creadoEn?.substring(0, 10) ?: "",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B9695)
                                )
                            }

                            // Botón Eliminar si es el autor o Admin
                            if (com.usuarioId == currentUserId) {
                                Text(
                                    text = "Eliminar",
                                    fontSize = 12.sp,
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        viewModel.eliminarComentario(reporteId, com.id)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "\"${com.contenido}\"",
                            fontSize = 13.sp,
                            color = Color(0xFF424242),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Formulario inferior para escribir retroalimentación (M01_13)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Escribir Retroalimentación",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandTealDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nuevoTexto,
                    onValueChange = { nuevoTexto = it },
                    placeholder = { Text("Escribe una nota o agradecimiento...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.agregarComentario(reporteId, nuevoTexto)
                        nuevoTexto = ""
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Enviar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
