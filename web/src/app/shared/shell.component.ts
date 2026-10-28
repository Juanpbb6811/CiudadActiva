import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { ROL_LABEL } from '../core/constants';
import { SessionService } from '../core/session.service';
import { IconComponent } from './icon.component';

interface NavItem { path: string; label: string; icon: string; }

/**
 * Layout autenticado. Equivale a Scaffold + CiudadActivaBottomBar de AppNavigation.kt:
 *  - Móvil: barra inferior (con FAB "+" para el ciudadano) solo en las pantallas principales.
 *  - Escritorio (>=900px): menú lateral permanente.
 *  - route.data.nav = 'tab' (principal) | 'detail' (sin barra en móvil) | 'none' (asistente, sin menú).
 */
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, IconComponent],
  template: `
    <div class="shell" [class.nav-none]="navMode() === 'none'" [class.nav-detail]="navMode() === 'detail'">
      <aside class="sidebar">
        <div class="brand"><img src="/app-icon.svg" alt="CiudadActiva Logo" class="logo" /><span>ciudad<b>activa</b></span></div>
        <nav>
          @for (i of items(); track i.path) {
            <a [routerLink]="i.path" routerLinkActive="active"><app-icon [name]="i.icon" [size]="20" />{{ i.label }}</a>
          }
          @if (esCiudadano()) {
            <a routerLink="/ciudadano/nuevo-reporte/1" class="nav-cta"><app-icon name="plus" [size]="20" />Nuevo reporte</a>
          }
        </nav>
        <div class="side-user">
          <div class="avatar">{{ inicial() }}</div>
          <div><strong>{{ session.user()?.nombre }}</strong><small>{{ rolLabel() }}</small></div>
        </div>
      </aside>

      <main class="content"><router-outlet /></main>

      <nav class="bottombar" [class.with-fab]="esCiudadano()">
        @for (i of items(); track i.path; let idx = $index) {
          @if (esCiudadano() && idx === 2) { <span class="fab-gap"></span> }
          <a [routerLink]="i.path" routerLinkActive="active" [attr.aria-label]="i.label">
            <app-icon [name]="i.icon" [size]="22" /><span>{{ i.label }}</span><i class="bar"></i>
          </a>
        }
        @if (esCiudadano()) {
          <a routerLink="/ciudadano/nuevo-reporte/1" class="fab" aria-label="Nuevo reporte"><app-icon name="plus" [size]="30" [stroke]="2.5" /></a>
        }
      </nav>
    </div>`,
})
export class ShellComponent {
  session = inject(SessionService);
  private router = inject(Router);
  navMode = signal<'tab' | 'detail' | 'none'>('tab');

  esCiudadano = computed(() => (this.session.rol() ?? 'ROLE_CIUDADANO') === 'ROLE_CIUDADANO');
  rolLabel = computed(() => ROL_LABEL[this.session.rol() ?? 'ROLE_CIUDADANO']);
  inicial = computed(() => (this.session.user()?.nombre ?? '?').charAt(0).toUpperCase());

  items = computed<NavItem[]>(() => {
    switch (this.session.rol()) {
      case 'ROLE_GESTOR': return [
        { path: '/gestor/validacion', label: 'Reportes', icon: 'check-circle' },
        { path: '/gestor/triage', label: 'Triage', icon: 'dashboard' },
        { path: '/gestor/asignaciones', label: 'Asignar', icon: 'users' },
        { path: '/gestor/estadisticas', label: 'Estadísticas', icon: 'chart' },
        { path: '/perfil', label: 'Perfil', icon: 'user' },
      ];
      case 'ROLE_OPERADOR': return [
        { path: '/operador/tareas', label: 'Tareas', icon: 'clipboard' },
        { path: '/operador/historial', label: 'Historial', icon: 'history' },
        { path: '/perfil', label: 'Perfil', icon: 'user' },
      ];
      case 'ROLE_ADMIN': return [
        { path: '/admin/panel', label: 'Panel', icon: 'dashboard' },
        { path: '/admin/usuarios', label: 'Usuarios', icon: 'users' },
        { path: '/admin/categorias', label: 'Categorías', icon: 'layers' },
        { path: '/admin/auditoria', label: 'Auditoría', icon: 'shield' },
        { path: '/perfil', label: 'Perfil', icon: 'user' },
      ];
      default: return [
        { path: '/ciudadano/inicio', label: 'Inicio', icon: 'home' },
        { path: '/ciudadano/mapa', label: 'Mapa', icon: 'map' },
        { path: '/ciudadano/mis-reportes', label: 'Reportes', icon: 'file' },
        { path: '/perfil', label: 'Perfil', icon: 'user' },
      ];
    }
  });

  constructor() {
    const read = () => {
      let r = this.router.routerState.snapshot.root;
      while (r.firstChild) r = r.firstChild;
      this.navMode.set((r.data['nav'] as 'tab' | 'detail' | 'none') ?? 'tab');
    };
    read();
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(read);
  }
}
