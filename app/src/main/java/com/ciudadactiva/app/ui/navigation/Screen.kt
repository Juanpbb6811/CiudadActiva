package com.ciudadactiva.app.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Ciudadano
    data object CiudadanoHome : Screen("ciudadano_home")
    data object NuevoReportePaso1 : Screen("nuevo_reporte_paso1")
    data object NuevoReportePaso2 : Screen("nuevo_reporte_paso2")
    data object NuevoReportePaso3 : Screen("nuevo_reporte_paso3")
    data object NuevoReporteExito : Screen("nuevo_reporte_exito")
    data object MisReportes : Screen("mis_reportes")
    data object MapaExplorador : Screen("mapa_explorador")
    data object SeguimientoTimeline : Screen("seguimiento/{reporteId}") {
        fun createRoute(reporteId: Long) = "seguimiento/$reporteId"
    }
    data object Comentarios : Screen("comentarios/{reporteId}") {
        fun createRoute(reporteId: Long) = "comentarios/$reporteId"
    }
    data object CiudadanoPerfil : Screen("ciudadano_perfil")

    // Gestor
    data object GestorDashboard : Screen("gestor_dashboard")
    data object GestorBandejaValidacion : Screen("gestor_validacion")
    data object GestorFichaInspeccion : Screen("gestor_inspeccion/{reporteId}") {
        fun createRoute(reporteId: Long) = "gestor_inspeccion/$reporteId"
    }
    data object GestorAsignaciones : Screen("gestor_asignaciones")
    data object GestorEstadisticas : Screen("gestor_estadisticas")

    // Operador
    data object OperadorTareas : Screen("operador_tareas")
    data object OperadorFicha : Screen("operador_ficha/{reporteId}") {
        fun createRoute(reporteId: Long) = "operador_ficha/$reporteId"
    }
    data object OperadorAvance : Screen("operador_avance/{reporteId}") {
        fun createRoute(reporteId: Long) = "operador_avance/$reporteId"
    }
    data object OperadorCierre : Screen("operador_cierre/{reporteId}") {
        fun createRoute(reporteId: Long) = "operador_cierre/$reporteId"
    }
    data object OperadorHistorial : Screen("operador_historial")

    // Admin
    data object AdminPanel : Screen("admin_panel")
    data object AdminUsuarios : Screen("admin_usuarios")
    data object AdminCategorias : Screen("admin_categorias")
    data object AdminAuditoria : Screen("admin_auditoria")
}
