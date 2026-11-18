import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { ROL_LABEL } from '../core/api/labels';
import { AuthService } from '../core/auth/auth.service';
import { LoadingService } from '../core/ui/loading.service';
import { ThemeService } from '../core/ui/theme.service';

interface NavItem {
  ruta: string;
  label: string;
  icono: string;
  codigo: string;
}

const NAV_GESTOR: NavItem[] = [
  { ruta: '/gestor/triage', label: 'Triage operativo', icono: 'emergency', codigo: 'G-01' },
  { ruta: '/gestor/reportes', label: 'Bandeja de reportes', icono: 'inbox', codigo: 'G-02' },
  { ruta: '/gestor/asignaciones', label: 'Cuadrillas', icono: 'engineering', codigo: 'G-03' },
  { ruta: '/gestor/mapa', label: 'Mapa interactivo', icono: 'map', codigo: 'G-04' },
];
const NAV_ADMIN: NavItem[] = [
  { ruta: '/admin/estadisticas', label: 'Tablero estadístico', icono: 'monitoring', codigo: 'A-01' },
  { ruta: '/admin/usuarios', label: 'Usuarios y roles', icono: 'group', codigo: 'A-02' },
  { ruta: '/admin/categorias', label: 'Categorías', icono: 'category', codigo: 'A-03' },
  { ruta: '/admin/reportes', label: 'Auditoría de reportes', icono: 'fact_check', codigo: 'A-04' },
];

@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressBarModule,
  ],
  template: `
    <mat-sidenav-container class="shell">
      <mat-sidenav
        #nav
        class="nav"
        [mode]="esMovil() ? 'over' : 'side'"
        [opened]="!esMovil()"
        [fixedInViewport]="esMovil()"
      >
        <div class="brand">
          <img src="logo-ciudadactiva.svg" alt="" width="40" height="40" />
          <div>
            <div class="brand__name">CiudadActiva</div>
            <div class="eyebrow">{{ modulo() }}</div>
          </div>
        </div>

        <nav class="menu" aria-label="Navegación principal">
          <div class="eyebrow menu__title">Menú</div>
          @for (item of items(); track item.ruta) {
            <a
              class="menu__item"
              [routerLink]="item.ruta"
              routerLinkActive="is-active"
              (click)="esMovil() && nav.close()"
            >
              <mat-icon>{{ item.icono }}</mat-icon>
              <span class="menu__label">{{ item.label }}</span>
            </a>
          }
        </nav>

        <div class="nav__foot">
          <div class="user">
            <div class="user__avatar">{{ iniciales() }}</div>
            <div class="user__info">
              <div class="user__name">{{ auth.nombre() }}</div>
              <div class="user__role">{{ rolLabel() }}</div>
            </div>
          </div>
        </div>
      </mat-sidenav>

      <mat-sidenav-content class="content">
        <header class="topbar">
          @if (esMovil()) {
            <button mat-icon-button (click)="nav.toggle()" aria-label="Abrir menú">
              <mat-icon>menu</mat-icon>
            </button>
          }
          <span class="eyebrow">Portal de {{ modulo() }}</span>
          <span class="spacer"></span>
          <button
            mat-icon-button
            (click)="theme.alternar()"
            [matTooltip]="theme.tema() === 'dark' ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'"
            [attr.aria-label]="theme.tema() === 'dark' ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'"
          >
            <mat-icon>{{ theme.tema() === 'dark' ? 'light_mode' : 'dark_mode' }}</mat-icon>
          </button>
          <button mat-stroked-button (click)="auth.logout()">
            <mat-icon>logout</mat-icon>
            Salir
          </button>
          @if (loading.activo()) {
            <mat-progress-bar class="topbar__progress" mode="indeterminate" />
          }
        </header>
        <main>
          <router-outlet />
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: `
    .shell {
      height: 100vh;
      background: var(--ca-background);
    }
    .nav {
      width: 268px;
      background: var(--ca-surface);
      border-right: 1px solid var(--ca-border) !important;
      display: flex;
      flex-direction: column;
    }
    :host ::ng-deep .nav .mat-drawer-inner-container {
      display: flex;
      flex-direction: column;
    }
    .brand {
      display: flex;
      gap: 12px;
      align-items: center;
      padding: 22px 20px 18px;
      border-bottom: 1px solid var(--ca-border);
    }
    .brand__name {
      font-family: var(--ca-font-display);
      font-weight: 800;
      font-size: 19px;
      color: var(--ca-on-surface-strong);
    }
    .menu {
      padding: 18px 12px;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
    .menu__title {
      padding: 0 8px 10px;
    }
    .menu__item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 11px 10px;
      color: var(--ca-on-surface);
      text-decoration: none;
      border-left: 3px solid transparent;
      font-weight: 500;
    }
    .menu__item:hover {
      background: var(--ca-accent-soft);
    }
    .menu__item.is-active {
      background: var(--ca-card);
      border-left-color: var(--ca-sage);
      outline: 1px solid var(--ca-border);
      font-weight: 700;
    }
    .menu__item mat-icon {
      color: var(--ca-on-surface-variant);
    }
    .menu__item.is-active mat-icon {
      color: var(--ca-primary);
    }
    .menu__label {
      flex: 1;
    }
    .menu__code {
      color: var(--ca-on-surface-variant);
      font-size: 11px;
    }
    .nav__foot {
      margin-top: auto;
      padding: 16px 20px 20px;
      border-top: 1px solid var(--ca-border);
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .user {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .user__avatar {
      width: 38px;
      height: 38px;
      display: grid;
      place-items: center;
      background: var(--ca-cta);
      color: var(--ca-cta-text);
      font-family: var(--ca-font-display);
      font-weight: 800;
    }
    .user__name {
      font-weight: 700;
      font-size: 14px;
    }
    .user__role {
      font-size: 12px;
      color: var(--ca-on-surface-variant);
    }
    .content {
      background: var(--ca-background);
      background-image:
        linear-gradient(var(--ca-grid-line) 1px, transparent 1px),
        linear-gradient(90deg, var(--ca-grid-line) 1px, transparent 1px);
      background-size: 32px 32px;
    }
    .topbar {
      position: sticky;
      top: 0;
      z-index: 5;
      display: flex;
      align-items: center;
      gap: 8px;
      height: 60px;
      padding: 0 20px;
      background: var(--ca-surface);
      border-bottom: 1px solid var(--ca-border);
    }
    .topbar__progress {
      position: absolute;
      left: 0;
      right: 0;
      bottom: -2px;
      height: 3px;
    }
  `,
})
export class ShellComponent {
  protected readonly auth = inject(AuthService);
  protected readonly theme = inject(ThemeService);
  protected readonly loading = inject(LoadingService);
  protected readonly esMovil = toSignal(
    inject(BreakpointObserver)
      .observe('(max-width: 960px)')
      .pipe(map((r) => r.matches)),
    { initialValue: false },
  );

  protected readonly items = computed(() => (this.auth.rol() === 'ROLE_ADMIN' ? NAV_ADMIN : NAV_GESTOR));
  protected readonly modulo = computed(() =>
    this.auth.rol() === 'ROLE_ADMIN' ? 'gobierno y analítica' : 'gestión operativa',
  );
  protected readonly rolLabel = computed(() => {
    const r = this.auth.rol();
    return r ? ROL_LABEL[r] : '';
  });
  protected readonly iniciales = computed(() =>
    this.auth
      .nombre()
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((p) => p[0]?.toUpperCase())
      .join(''),
  );
}
