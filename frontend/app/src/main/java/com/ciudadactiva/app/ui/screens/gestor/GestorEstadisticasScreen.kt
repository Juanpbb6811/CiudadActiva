package com.ciudadactiva.app.ui.screens.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.components.SemaforoBadge
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.GestorViewModel

@Composable
fun GestorEstadisticasScreen(
    viewModel: GestorViewModel,
    onBackClick: () -> Unit
) {
    val stats by viewModel.estadisticas.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(
            title = "Estadísticas y Semáforos",
            roleTag = "GESTIÓN",
            onBackClick = onBackClick
        )

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Semáforo Global (1 a 10 reportes escala de saturación)
            stats?.semaforoGlobal?.let { semGlobal ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "SEMÁFORO GLOBAL DE SATURACIÓN URBANA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A8B88)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SemaforoBadge(semaforo = semGlobal, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Métricas de Rendimiento
            if (stats != null) {
                val s = stats!!
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "INDICADORES CLAVE DE DESEMPEÑO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A8B88)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total de Incidentes Registrados:", fontSize = 13.sp, color = TextDark)
                            Text("${s.totalReportes}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Casos Atendidos o Resueltos:", fontSize = 13.sp, color = TextDark)
                            Text("${s.reportesAtendidosOCerrados}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Incidentes Pendientes de Cierre:", fontSize = 13.sp, color = TextDark)
                            Text("${s.reportesPendientes}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tiempo Promedio de Solución:", fontSize = 13.sp, color = TextDark)
                            Text("${s.tiempoPromedioAtencionHoras} horas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        }
                    }
                }
            }

            // Semáforos por Categoría
            Text(
                text = "ESTADO SEMAFÓRICO POR CATEGORÍA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A8B88)
            )

            stats?.semaforoPorCategoria?.forEach { (nombreCat, semCat) ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(nombreCat, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandTealDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        SemaforoBadge(semaforo = semCat, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
