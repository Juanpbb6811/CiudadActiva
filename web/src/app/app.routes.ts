import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/guards';
import { ShellComponent } from './shared/shell.component';

/**
 * Mapa de rutas = Screen.kt + AppNavigation.kt.
 * data.nav: 'tab' (pantalla principal con barra), 'detail' (sin barra en móvil), 'none' (asistente sin menú).
 */
export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/landing/landing.component').then((m) => m.LandingComponent) },
  { path: 'portal', loadComponent: () => import('./features/landing/landing.component').then((m) => m.LandingComponent) },
  { path: 'splash', loadComponent: () => import('./features/auth/splash.component').then((m) => m.SplashComponent) },
  { path: 'login', canActivate: [guestGuard], loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent) },
  { path: 'register', canActivate: [guestGuard], loadComponent: () => import('./features/auth/register.component').then((m) => m.RegisterComponent) },

  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      // ================== CIUDADANO ==================
      {
        path: 'ciudadano',
        canActivate: [roleGuard('ROLE_CIUDADANO')],
        children: [
          { path: 'inicio', data: { nav: 'tab' }, loadComponent: () => import('./features/ciudadano/home.component').then((m) => m.CiudadanoHomeComponent) },
          { path: 'nuevo-reporte/1', data: { nav: 'none' }, loadComponent: () => import('./features/ciudadano/nuevo-reporte-paso1.component').then((m) => m.NuevoReportePaso1Component) },
          { path: 'nuevo-reporte/2', data: { nav: 'none' }, loadComponent: () => import('./features/ciudadano/nuevo-reporte-paso2.component').then((m) => m.NuevoReportePaso2Component) },
          { path: 'nuevo-reporte/3', data: { nav: 'none' }, loadComponent: () => import('./features/ciudadano/nuevo-reporte-paso3.component').then((m) => m.NuevoReportePaso3Component) },
          { path: 'nuevo-reporte/exito', data: { nav: 'none' }, loadComponent: () => import('./features/ciudadano/nuevo-reporte-exito.component').then((m) => m.NuevoReporteExitoComponent) },
          { path: 'mis-reportes', data: { nav: 'tab' }, loadComponent: () => import('./features/ciudadano/mis-reportes.component').then((m) => m.MisReportesComponent) },
          { path: 'mapa', data: { nav: 'tab' }, loadComponent: () => import('./features/ciudadano/mapa.component').then((m) => m.MapaExploradorComponent) },
          { path: 'seguimiento/:id', data: { nav: 'detail' }, loadComponent: () => import('./features/ciudadano/seguimiento.component').then((m) => m.SeguimientoComponent) },
          { path: 'comentarios/:id', data: { nav: 'detail' }, loadComponent: () => import('./features/ciudadano/comentarios.component').then((m) => m.ComentariosComponent) },
          { path: '', pathMatch: 'full', redirectTo: 'inicio' },
        ],
      },

      // ================== GESTOR ==================
      {
        path: 'gestor',
        canActivate: [roleGuard('ROLE_GESTOR', 'ROLE_ADMIN')],
        children: [
          { path: 'triage', data: { nav: 'tab' }, loadComponent: () => import('./features/gestor/dashboard.component').then((m) => m.GestorDashboardComponent) },
          { path: 'validacion', data: { nav: 'tab' }, loadComponent: () => import('./features/gestor/bandeja.component').then((m) => m.GestorBandejaComponent) },
          { path: 'inspeccion/:id', data: { nav: 'detail' }, loadComponent: () => import('./features/gestor/ficha.component').then((m) => m.GestorFichaComponent) },
          { path: 'asignaciones', data: { nav: 'tab' }, loadComponent: () => import('./features/gestor/asignaciones.component').then((m) => m.GestorAsignacionesComponent) },
          { path: 'estadisticas', data: { nav: 'tab' }, loadComponent: () => import('./features/gestor/estadisticas.component').then((m) => m.GestorEstadisticasComponent) },
          { path: '', pathMatch: 'full', redirectTo: 'validacion' },
        ],
      },

      // ================== OPERADOR ==================
      {
        path: 'operador',
        canActivate: [roleGuard('ROLE_OPERADOR', 'ROLE_ADMIN')],
        children: [
          { path: 'tareas', data: { nav: 'tab' }, loadComponent: () => import('./features/operador/tareas.component').then((m) => m.OperadorTareasComponent) },
          { path: 'ficha/:id', data: { nav: 'detail' }, loadComponent: () => import('./features/operador/ficha.component').then((m) => m.OperadorFichaComponent) },
          { path: 'avance/:id', data: { nav: 'detail' }, loadComponent: () => import('./features/operador/avance.component').then((m) => m.OperadorAvanceComponent) },
          { path: 'cierre/:id', data: { nav: 'detail' }, loadComponent: () => import('./features/operador/cierre.component').then((m) => m.OperadorCierreComponent) },
          { path: 'historial', data: { nav: 'tab' }, loadComponent: () => import('./features/operador/historial.component').then((m) => m.OperadorHistorialComponent) },
          { path: '', pathMatch: 'full', redirectTo: 'tareas' },
        ],
      },

      // ================== ADMIN ==================
      {
        path: 'admin',
        canActivate: [roleGuard('ROLE_ADMIN')],
        children: [
          { path: 'panel', data: { nav: 'tab' }, loadComponent: () => import('./features/admin/panel.component').then((m) => m.AdminPanelComponent) },
          { path: 'usuarios', data: { nav: 'tab' }, loadComponent: () => import('./features/admin/usuarios.component').then((m) => m.AdminUsuariosComponent) },
          { path: 'categorias', data: { nav: 'tab' }, loadComponent: () => import('./features/admin/categorias.component').then((m) => m.AdminCategoriasComponent) },
          { path: 'auditoria', data: { nav: 'tab' }, loadComponent: () => import('./features/admin/auditoria.component').then((m) => m.AdminAuditoriaComponent) },
          { path: '', pathMatch: 'full', redirectTo: 'panel' },
        ],
      },

      // ================== PERFIL (todos los roles) ==================
      { path: 'perfil', data: { nav: 'tab' }, loadComponent: () => import('./features/perfil/perfil.component').then((m) => m.PerfilComponent) },
    ],
  },
  { path: '**', redirectTo: 'splash' },
];
