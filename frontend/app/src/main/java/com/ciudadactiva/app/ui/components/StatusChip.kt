package com.ciudadactiva.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.ui.theme.*

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "PENDIENTE", "CREACION_REPORTE" -> StatusPendienteBg to StatusPendienteText
        "ADMITIDO", "VALIDACION_ADMITIDA" -> StatusAdmitidoBg to StatusAdmitidoText
        "EN_PROCESO", "EN_ATENCION", "CAMBIO_ESTADO" -> StatusEnProcesoBg to StatusEnProcesoText
        "ATENDIDO", "CERRADO", "CIERRE_REPORTE" -> StatusCerradoBg to StatusCerradoText
        "RECHAZADO", "RECHAZO_REPORTE" -> StatusRechazadoBg to StatusRechazadoText
        else -> Color(0xFFEEEEEE) to Color(0xFF616161)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.replace("_", " "),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
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
