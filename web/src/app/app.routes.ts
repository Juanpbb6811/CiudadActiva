import { Routes } from '@angular/router';
import { authGuard, guestGuard, inicioRedirectGuard, roleGuard } from './core/auth/guards';
import { ShellComponent } from './layout/shell.component';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    title: 'Ingreso · CiudadActiva',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'acceso-denegado',
    title: 'Acceso restringido · CiudadActiva',
    loadComponent: () => import('./features/auth/acceso-denegado.component').then((m) => m.AccesoDenegadoComponent),
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      // ============ GESTOR · Módulo operativo ============
      {
        path: 'gestor',
        canActivate: [roleGuard('ROLE_GESTOR')],
        children: [
          {
            path: 'triage',
            title: 'Triage operativo · CiudadActiva',
            loadComponent: () => import('./features/gestor/triage.component').then((m) => m.TriageComponent),
          },
          {
            path: 'reportes',
            title: 'Bandeja de reportes · CiudadActiva',
            loadComponent: () => import('./features/gestor/bandeja.component').then((m) => m.BandejaComponent),
          },
          {
            path: 'asignaciones',
            title: 'Cuadrillas y asignaciones · CiudadActiva',
            loadComponent: () =>
              import('./features/gestor/asignaciones.component').then((m) => m.AsignacionesComponent),
          },
          {
            path: 'mapa',
            title: 'Mapa interactivo · CiudadActiva',
            loadComponent: () => import('./features/gestor/mapa.component').then((m) => m.MapaComponent),
          },
          { path: '', pathMatch: 'full', redirectTo: 'triage' },
        ],
      },
      // ============ ADMINISTRADOR · Gobierno y analítica ============
      {
        path: 'admin',
        canActivate: [roleGuard('ROLE_ADMIN')],
        children: [
          {
            path: 'estadisticas',
            title: 'Tablero estadístico · CiudadActiva',
            loadComponent: () =>
              import('./features/admin/estadisticas.component').then((m) => m.EstadisticasComponent),
          },
          {
            path: 'usuarios',
            title: 'Usuarios y roles · CiudadActiva',
            loadComponent: () => import('./features/admin/usuarios.component').then((m) => m.UsuariosComponent),
          },
          {
            path: 'categorias',
            title: 'Categorías · CiudadActiva',
            loadComponent: () => import('./features/admin/categorias.component').then((m) => m.CategoriasComponent),
          },
          {
            path: 'reportes',
            title: 'Auditoría de reportes · CiudadActiva',
            loadComponent: () => import('./features/admin/auditoria.component').then((m) => m.AuditoriaComponent),
          },
          { path: '', pathMatch: 'full', redirectTo: 'estadisticas' },
        ],
      },
      { path: '', pathMatch: 'full', canActivate: [inicioRedirectGuard], children: [] },
    ],
  },
  { path: '**', redirectTo: '' },
];
