package com.ciudadactiva.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.ui.theme.*

data class EstadoConfig(
    val bg: Color,
    val text: Color,
    val border: Color
)

fun getEstadoConfig(status: String): EstadoConfig {
    return when (status.uppercase().replace(" ", "_")) {
        "PENDIENTE", "CREACION_REPORTE" -> EstadoConfig(StatusPendienteBg, StatusPendienteText, StatusPendienteBorder)
        "EN_REVISION" -> EstadoConfig(StatusEnRevisionBg, StatusEnRevisionText, StatusEnRevisionBorder)
        "ADMITIDO", "VALIDACION_ADMITIDA" -> EstadoConfig(StatusAdmitidoBg, StatusAdmitidoText, StatusAdmitidoBorder)
        "EN_PROCESO", "EN_ATENCION", "CAMBIO_ESTADO" -> EstadoConfig(StatusEnProcesoBg, StatusEnProcesoText, StatusEnProcesoBorder)
        "ATENDIDO" -> EstadoConfig(StatusAtendidoBg, StatusAtendidoText, StatusAtendidoBorder)
        "CERRADO", "CIERRE_REPORTE" -> EstadoConfig(StatusCerradoBg, StatusCerradoText, StatusCerradoBorder)
        "RECHAZADO", "RECHAZO_REPORTE" -> EstadoConfig(StatusRechazadoBg, StatusRechazadoText, StatusRechazadoBorder)
        else -> EstadoConfig(Color(0xFFEEEEEE), Color(0xFF616161), Color(0xFFBDBDBD))
    }
}

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val config = getEstadoConfig(status)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(config.bg)
            .border(1.dp, config.border, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.replace("_", " "),
            color = config.text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EstadoSelectorItem(
    estadoKey: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config = getEstadoConfig(estadoKey)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) BrandTealDark else config.bg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) BrandTealDark else config.border,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else config.text)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = estadoKey.replace("_", " "),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else config.text
            )
        }
    }
}

@Composable
fun PrioridadChip(
    prioridad: String?,
    modifier: Modifier = Modifier
) {
    val prio = prioridad?.uppercase() ?: "MEDIA"
    val (bgColor, textColor) = when (prio) {
        "BAJA" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "MEDIA" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "ALTA" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "CRITICA" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFF5F5F5) to Color(0xFF424242)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "PRIORIDAD $prio",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
