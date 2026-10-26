package com.ciudadactiva.app.ui.screens.ciudadano

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.R
import com.ciudadactiva.app.data.model.CategoriaModel
import com.ciudadactiva.app.data.model.ReporteResponse
import com.ciudadactiva.app.ui.components.LoadingSkeleton
import com.ciudadactiva.app.ui.components.SemaforoBadge
import com.ciudadactiva.app.ui.components.StatusChip
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.CiudadanoViewModel

@Composable
fun CiudadanoHomeScreen(
    viewModel: CiudadanoViewModel,
    onNuevoReporteClick: () -> Unit,
    onCategoriaClick: (CategoriaModel) -> Unit,
    onVerReporteClick: (Long) -> Unit,
    onVerMapaClick: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val reporteActivo by viewModel.reporteActivo.collectAsState()
    val reportesCercanos by viewModel.reportesCercanos.collectAsState()

    if (isLoading && categorias.isEmpty()) {
        LoadingSkeleton()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Top Logo + Saludo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_ciudadactiva),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "ciudadactiva",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTealDark
            )
        }

        Text(
            text = "Hola, ¿qué quieres\nreportar hoy?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = BrandTealDark,
            lineHeight = 32.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Tarjeta Principal de Acción Rápida (M01_01)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .clickable(onClick = onNuevoReporteClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reportar ahora",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandTealDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Solo toma una foto o escribe",
                        fontSize = 13.sp,
                        color = Color(0xFF6C757D)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00796B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ubicación lista • Cra 5 #12-30",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00796B)
                        )
                    }
                }

                // Botón Cámara
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Cámara",
                        tint = BrandTealDark,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Fila REPORTE RÁPIDO
        Text(
            text = "REPORTE RÁPIDO Y SEMÁFORO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7A8B88),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(categorias) { cat ->
                Card(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { onCategoriaClick(cat) }
                        .shadow(2.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (cat.icono) {
                                    "road" -> Icons.Default.DirectionsCar
                                    "lightbulb" -> Icons.Default.Lightbulb
                                    "trash" -> Icons.Default.Delete
                                    "park" -> Icons.Default.Park
                                    "water" -> Icons.Default.WaterDrop
                                    else -> Icons.Default.Traffic
                                },
                                contentDescription = cat.nombre,
                                tint = BrandTealDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = cat.nombre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        cat.semaforo?.let { sem ->
                            SemaforoBadge(
                                semaforo = sem,
                                mostrarDetalle = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TU REPORTE ACTIVO (M01_01)
        if (reporteActivo != null) {
            val activo = reporteActivo!!
            Text(
                text = "TU REPORTE ACTIVO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A8B88),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp))
                    .clickable { onVerReporteClick(activo.id) },
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
                            text = "N.° ${activo.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B9695)
                        )
                        StatusChip(status = activo.estado)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = activo.titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Indicador de pasos visual (Registrado -> En atención -> Cerrado)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isEnAtencion = activo.estado == "ADMITIDO" || activo.estado == "EN_PROCESO" || activo.estado == "ATENDIDO"
                        val isCerrado = activo.estado == "CERRADO"

                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(BrandTealDark))
                        Box(modifier = Modifier.weight(1f).height(3.dp).background(if (isEnAtencion || isCerrado) BrandTealDark else Color(0xFFDCE2E1)))
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isEnAtencion || isCerrado) BrandCopper else Color(0xFFB0BEC5)))
                        Box(modifier = Modifier.weight(1f).height(3.dp).background(if (isCerrado) BrandTealDark else Color(0xFFDCE2E1)))
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isCerrado) BrandTealDark else Color(0xFFCFD8DC)))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Registrado", fontSize = 10.sp, color = Color(0xFF8B9695))
                        Text("En atención", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandCopper)
                        Text("Cerrado", fontSize = 10.sp, color = Color(0xFF8B9695))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cerca de ti (Radar / Mapa Preview)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cerca de ti",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTealDark
            )
            Text(
                text = "${reportesCercanos.size} reportes • Ver mapa",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandCopper,
                modifier = Modifier.clickable(onClick = onVerMapaClick)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vista previa simulada de Mapa (según Mockup M01_01)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 20.dp)
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .clickable(onClick = onVerMapaClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEBE6DC))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Pin central
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(BrandTealDark)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Cra 5 #12-30 (Tu ubicación)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandTealDark
                    )
                }
            }
        }
    }
}
