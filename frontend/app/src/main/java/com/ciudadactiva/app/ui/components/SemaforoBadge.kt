package com.ciudadactiva.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.data.model.SemaforoModel
import com.ciudadactiva.app.ui.theme.TextDark

@Composable
fun SemaforoBadge(
    semaforo: SemaforoModel,
    modifier: Modifier = Modifier,
    mostrarDetalle: Boolean = true
) {
    val color = semaforo.toComposeColor()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F4))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Círculo del semáforo con pulsación de color
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Text(
                text = "${semaforo.cantidadReportes} reportes",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextDark
            )

            Spacer(modifier = Modifier.weight(1f))

            // Chip con el nombre del nivel de alerta
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = semaforo.nivelAlerta,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        if (mostrarDetalle) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (semaforo.porcentajeSaturacion / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = Color(0xFFDCE2E1)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = semaforo.descripcion,
                fontSize = 11.sp,
                color = Color(0xFF5A6665),
                lineHeight = 14.sp
            )
        }
    }
}
