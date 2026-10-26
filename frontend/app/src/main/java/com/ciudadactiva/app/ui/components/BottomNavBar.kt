package com.ciudadactiva.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.ui.navigation.Screen
import com.ciudadactiva.app.ui.theme.BrandCopper
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.BrandTealLight

data class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun CiudadActivaBottomBar(
    currentRoute: String?,
    userRole: String?,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit = {}
) {
    val items = when (userRole) {
        "ROLE_GESTOR" -> listOf(
            NavItem(Screen.GestorDashboard.route, "Validar", Icons.Outlined.CheckCircle, Icons.Filled.CheckCircle),
            NavItem(Screen.GestorAsignaciones.route, "Asignar", Icons.Outlined.People, Icons.Filled.People),
            NavItem(Screen.GestorEstadisticas.route, "Stats", Icons.Outlined.BarChart, Icons.Filled.BarChart),
            NavItem(Screen.CiudadanoPerfil.route, "Perfil", Icons.Outlined.Person, Icons.Filled.Person)
        )
        "ROLE_OPERADOR" -> listOf(
            NavItem(Screen.OperadorTareas.route, "Tareas", Icons.Outlined.Assignment, Icons.Filled.Assignment),
            NavItem(Screen.OperadorHistorial.route, "Historial", Icons.Outlined.History, Icons.Filled.History),
            NavItem(Screen.CiudadanoPerfil.route, "Perfil", Icons.Outlined.Person, Icons.Filled.Person)
        )
        "ROLE_ADMIN" -> listOf(
            NavItem(Screen.AdminPanel.route, "Panel", Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
            NavItem(Screen.AdminUsuarios.route, "Usuarios", Icons.Outlined.Group, Icons.Filled.Group),
            NavItem(Screen.AdminCategorias.route, "Categorías", Icons.Outlined.Category, Icons.Filled.Category),
            NavItem(Screen.AdminAuditoria.route, "Auditoría", Icons.Outlined.Security, Icons.Filled.Security)
        )
        else -> listOf(
            NavItem(Screen.CiudadanoHome.route, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
            NavItem(Screen.MapaExplorador.route, "Mapa", Icons.Outlined.Map, Icons.Filled.Map),
            // El FAB central se ubica entre Mapa y Mis Reportes
            NavItem(Screen.MisReportes.route, "Reportes", Icons.Outlined.Description, Icons.Filled.Description),
            NavItem(Screen.CiudadanoPerfil.route, "Perfil", Icons.Outlined.Person, Icons.Filled.Person)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .navigationBarsPadding()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isCiudadano = userRole == "ROLE_CIUDADANO" || userRole == null

            items.forEachIndexed { index, item ->
                // Para ciudadano, insertar el espacio del FAB central después del 2do item (Mapa)
                if (isCiudadano && index == 2) {
                    // Botón Central FAB (+)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .offset(y = (-10).dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(BrandTealLight, BrandCopper)))
                            .clickable(onClick = onFabClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo Reporte",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                val isSelected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) BrandTealDark else Color(0xFF9E9E9E),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        fontSize = 10.sp,
                        color = if (isSelected) BrandTealDark else Color(0xFF9E9E9E)
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(width = 16.dp, height = 2.dp)
                                .background(BrandCopper, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
