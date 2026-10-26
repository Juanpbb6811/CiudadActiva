package com.ciudadactiva.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ciudadactiva.app.CiudadActivaApplication
import com.ciudadactiva.app.ui.components.CiudadActivaBottomBar
import com.ciudadactiva.app.ui.screens.admin.*
import com.ciudadactiva.app.ui.screens.auth.*
import com.ciudadactiva.app.ui.screens.ciudadano.*
import com.ciudadactiva.app.ui.screens.gestor.*
import com.ciudadactiva.app.ui.screens.operador.*
import com.ciudadactiva.app.ui.viewmodel.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val ciudadanoViewModel: CiudadanoViewModel = viewModel()
    val gestorViewModel: GestorViewModel = viewModel()
    val operadorViewModel: OperadorViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()

    val session = CiudadActivaApplication.sessionManager
    val userRole = session.getRol()

    // Solo mostrar barra de navegación en pantallas principales (no en Splash, Login, Register o Wizards de 3 pasos)
    val showBottomBar = currentRoute in listOf(
        Screen.CiudadanoHome.route,
        Screen.MisReportes.route,
        Screen.MapaExplorador.route,
        Screen.CiudadanoPerfil.route,
        Screen.GestorDashboard.route,
        Screen.GestorAsignaciones.route,
        Screen.GestorEstadisticas.route,
        Screen.OperadorTareas.route,
        Screen.OperadorHistorial.route,
        Screen.AdminPanel.route,
        Screen.AdminUsuarios.route,
        Screen.AdminCategorias.route,
        Screen.AdminAuditoria.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CiudadActivaBottomBar(
                    currentRoute = currentRoute,
                    userRole = userRole,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.CiudadanoHome.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onFabClick = {
                        navController.navigate(Screen.NuevoReportePaso1.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ================== RUTAS AUTENTICACIÓN ==================
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateNext = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.CiudadanoHome.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ================== RUTAS CIUDADANO ==================
            composable(Screen.CiudadanoHome.route) {
                CiudadanoHomeScreen(
                    viewModel = ciudadanoViewModel,
                    onNuevoReporteClick = { navController.navigate(Screen.NuevoReportePaso1.route) },
                    onCategoriaClick = { cat ->
                        ciudadanoViewModel.nuevoReporteCategoria.value = cat
                        navController.navigate(Screen.NuevoReportePaso1.route)
                    },
                    onVerReporteClick = { id ->
                        navController.navigate(Screen.SeguimientoTimeline.createRoute(id))
                    },
                    onVerMapaClick = { navController.navigate(Screen.MapaExplorador.route) }
                )
            }

            composable(Screen.NuevoReportePaso1.route) {
                NuevoReportePaso1Screen(
                    viewModel = ciudadanoViewModel,
                    onNext = { navController.navigate(Screen.NuevoReportePaso2.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NuevoReportePaso2.route) {
                NuevoReportePaso2Screen(
                    viewModel = ciudadanoViewModel,
                    onNext = { navController.navigate(Screen.NuevoReportePaso3.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NuevoReportePaso3.route) {
                NuevoReportePaso3Screen(
                    viewModel = ciudadanoViewModel,
                    onSuccess = { navController.navigate(Screen.NuevoReporteExito.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NuevoReporteExito.route) {
                NuevoReporteExitoScreen(
                    viewModel = ciudadanoViewModel,
                    onVerSeguimiento = { id ->
                        navController.navigate(Screen.SeguimientoTimeline.createRoute(id)) {
                            popUpTo(Screen.CiudadanoHome.route)
                        }
                    },
                    onVolverInicio = {
                        navController.navigate(Screen.CiudadanoHome.route) {
                            popUpTo(Screen.CiudadanoHome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.MisReportes.route) {
                MisReportesScreen(
                    viewModel = ciudadanoViewModel,
                    onVerReporteClick = { id ->
                        navController.navigate(Screen.SeguimientoTimeline.createRoute(id))
                    },
                    onNuevoReporteClick = { navController.navigate(Screen.NuevoReportePaso1.route) }
                )
            }

            composable(Screen.MapaExplorador.route) {
                MapaExploradorScreen(
                    viewModel = ciudadanoViewModel,
                    onVerReporteDetalle = { id ->
                        navController.navigate(Screen.SeguimientoTimeline.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.SeguimientoTimeline.route,
                arguments = listOf(navArgument("reporteId") { type = NavType.LongType })
            ) { backStack ->
                val reporteId = backStack.arguments?.getLong("reporteId") ?: 1L
                SeguimientoTimelineScreen(
                    reporteId = reporteId,
                    viewModel = ciudadanoViewModel,
                    onComentariosClick = { id ->
                        navController.navigate(Screen.Comentarios.createRoute(id))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Comentarios.route,
                arguments = listOf(navArgument("reporteId") { type = NavType.LongType })
            ) { backStack ->
                val reporteId = backStack.arguments?.getLong("reporteId") ?: 1L
                ComentariosScreen(
                    reporteId = reporteId,
                    viewModel = ciudadanoViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.CiudadanoPerfil.route) {
                CiudadanoPerfilScreen(
                    onCerrarSesion = {
                        authViewModel.cerrarSesion()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ================== RUTAS GESTOR ==================
            composable(Screen.GestorDashboard.route) {
                GestorDashboardScreen(
                    viewModel = gestorViewModel,
                    onVerBandejaValidacion = { navController.navigate(Screen.GestorBandejaValidacion.route) },
                    onVerAsignaciones = { navController.navigate(Screen.GestorAsignaciones.route) },
                    onVerEstadisticas = { navController.navigate(Screen.GestorEstadisticas.route) },
                    onInspeccionarReporte = { id ->
                        navController.navigate(Screen.GestorFichaInspeccion.createRoute(id))
                    }
                )
            }

            composable(Screen.GestorBandejaValidacion.route) {
                GestorDashboardScreen(
                    viewModel = gestorViewModel,
                    onVerBandejaValidacion = {},
                    onVerAsignaciones = { navController.navigate(Screen.GestorAsignaciones.route) },
                    onVerEstadisticas = { navController.navigate(Screen.GestorEstadisticas.route) },
                    onInspeccionarReporte = { id ->
                        navController.navigate(Screen.GestorFichaInspeccion.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.GestorFichaInspeccion.route,
                arguments = listOf(navArgument("reporteId") { type = NavType.LongType })
            ) { backStack ->
                val reporteId = backStack.arguments?.getLong("reporteId") ?: 1L
                GestorFichaInspeccionScreen(
                    reporteId = reporteId,
                    viewModel = gestorViewModel,
                    onAccionTerminada = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.GestorAsignaciones.route) {
                GestorAsignacionesScreen(
                    viewModel = gestorViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.GestorEstadisticas.route) {
                GestorEstadisticasScreen(
                    viewModel = gestorViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ================== RUTAS OPERADOR ==================
            composable(Screen.OperadorTareas.route) {
                OperadorTareasScreen(
                    viewModel = operadorViewModel,
                    onRegistrarAvanceClick = { id ->
                        navController.navigate(Screen.OperadorAvance.createRoute(id))
                    },
                    onCerrarReporteClick = { id ->
                        navController.navigate(Screen.OperadorCierre.createRoute(id))
                    },
                    onFichaClick = { id ->
                        navController.navigate(Screen.OperadorFicha.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.OperadorFicha.route,
                arguments = listOf(navArgument("reporteId") { type = NavType.LongType })
            ) { backStack ->
                val reporteId = backStack.arguments?.getLong("reporteId") ?: 1L
                OperadorFichaScreen(
                    reporteId = reporteId,
                    viewModel = operadorViewModel,
                    onRegistrarAvance = { id -> navController.navigate(Screen.OperadorAvance.createRoute(id)) },
                    onCerrarReporte = { id -> navController.navigate(Screen.OperadorCierre.createRoute(id)) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.OperadorAvance.route,
                arguments = listOf(navArgument("reporteId") { type = NavType.LongType })
            ) { backStack ->
                val reporteId = backStack.arguments?.getLong("reporteId") ?: 1L
                OperadorAvanceScreen(
                    reporteId = reporteId,
                    viewModel = operadorViewModel,
                    onSuccess = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.OperadorCierre.route,
                arguments = listOf(navArgument("reporteId") { type = NavType.LongType })
            ) { backStack ->
                val reporteId = backStack.arguments?.getLong("reporteId") ?: 1L
                OperadorCierreScreen(
                    reporteId = reporteId,
                    viewModel = operadorViewModel,
                    onCierreExitoso = {
                        navController.navigate(Screen.OperadorTareas.route) {
                            popUpTo(Screen.OperadorTareas.route) { inclusive = true }
                        }
                    },
                    onIrAAvances = {
                        navController.navigate(Screen.OperadorAvance.createRoute(reporteId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.OperadorHistorial.route) {
                OperadorHistorialScreen(
                    viewModel = operadorViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ================== RUTAS ADMIN ==================
            composable(Screen.AdminPanel.route) {
                AdminPanelScreen(
                    viewModel = adminViewModel,
                    onDirectorioUsuariosClick = { navController.navigate(Screen.AdminUsuarios.route) },
                    onCategoriasClick = { navController.navigate(Screen.AdminCategorias.route) },
                    onAuditoriaClick = { navController.navigate(Screen.AdminAuditoria.route) }
                )
            }

            composable(Screen.AdminUsuarios.route) {
                AdminUsuariosScreen(
                    viewModel = adminViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminCategorias.route) {
                AdminCategoriasScreen(
                    viewModel = adminViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminAuditoria.route) {
                AdminAuditoriaScreen(
                    viewModel = adminViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
