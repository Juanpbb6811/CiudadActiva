package com.ciudadactiva.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.R
import com.ciudadactiva.app.ui.navigation.Screen
import com.ciudadactiva.app.ui.theme.BackgroundLight
import com.ciudadactiva.app.ui.theme.BrandTealDark
import com.ciudadactiva.app.ui.theme.BrandTealLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1800)
        val session = CiudadActivaApplication.sessionManager
        if (session.isLoggedIn()) {
            when (session.getRol()) {
                "ROLE_GESTOR" -> onNavigateNext(Screen.GestorDashboard.route)
                "ROLE_OPERADOR" -> onNavigateNext(Screen.OperadorTareas.route)
                "ROLE_ADMIN" -> onNavigateNext(Screen.AdminPanel.route)
                else -> onNavigateNext(Screen.CiudadanoHome.route)
            }
        } else {
            onNavigateNext(Screen.Login.route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Logo circular
            Image(
                painter = painterResource(id = R.drawable.ic_logo_ciudadactiva),
                contentDescription = "Logo CiudadActiva",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ciudadactiva",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTealDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Plataforma Comunitaria de Incidencias Urbanas",
                fontSize = 14.sp,
                color = Color(0xFF5A6665)
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(CircleShape),
                color = BrandTealLight,
                trackColor = Color(0xFFDCE2E1)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Iniciando plataforma...",
                fontSize = 12.sp,
                color = Color(0xFF8B9695)
            )
        }
    }
}
