package com.ciudadactiva.app.ui.screens.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.ui.components.CiudadActivaTopBar
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.TextDark
import com.ciudadactiva.app.ui.viewmodel.GestorViewModel

@Composable
fun GestorAsignacionesScreen(
    viewModel: GestorViewModel,
    onBackClick: () -> Unit
) {
    val asignaciones by viewModel.asignacionesActivas.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(bottom = 70.dp)
    ) {
        CiudadActivaTopBar(
            title = "Asignaciones Activas",
            roleTag = "CUADRILLAS",
            onBackClick = onBackClick
        )

        if (asignaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay asignaciones operativas en curso.",
                    color = Color(0xFF78909C),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(asignaciones) { asig ->
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reporte #${asig.reporteId}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78909C)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("EN TERRENO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = asig.reporteTitulo ?: "Incidencia Urbana",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Operador Responsable: ${asig.operadorNombre ?: "Cuadrilla Asignada"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandTealDark
                            )

                            if (!asig.notas.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Instrucciones: ${asig.notas}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF5A6665)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
