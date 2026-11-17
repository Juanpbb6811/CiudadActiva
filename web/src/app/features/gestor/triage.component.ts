import { DatePipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { switchMap, of, filter } from 'rxjs';
import { CiudadActivaApi } from '../../core/api/ciudad-activa.api';
import { PRIORIDAD_PESO, iconoCategoria, puedeAsignar, puedeValidar } from '../../core/api/labels';
import { Reporte } from '../../core/api/models';
import { asyncState } from '../../core/ui/async-state';
import { EstadoBadgeComponent, KpiComponent, PageHeaderComponent, PrioridadBadgeComponent, ViewStateComponent } from '../../shared/ui.components';
import { abrirAsignar, abrirDetalle, abrirValidar } from './dialogs';

@Component({
  selector: 'app-triage',
  imports: [DatePipe, RouterLink, MatButtonModule, MatIconModule, PageHeaderComponent, KpiComponent, ViewStateComponent, EstadoBadgeComponent, PrioridadBadgeComponent],
  template: `
    <div class="page">
      <ca-page-header codigo="G-01" seccion="Gestor" titulo="Triage operativo" descripcion="Panorama del día: qué validar, qué asignar y qué está en campo. Los casos críticos aparecen primero.">
        <button mat-stroked-button (click)="reportes.cargar()"><mat-icon>refresh</mat-icon> Actualizar</button>
      </ca-page-header>

      <ca-view-state [estado]="reportes.estado()" [error]="reportes.error()" (reintentar)="reportes.cargar()" tituloVacio="Aún no hay reportes" textoVacio="Cuando los ciudadanos registren incidencias desde la app aparecerán aquí.">
        <section class="grid-kpi">
          <a class="kpi-link" routerLink="/gestor/reportes" [queryParams]="{ estado: 'PENDIENTE' }">
            <ca-kpi etiqueta="Pendientes por validar" [valor]="kpi().pendientes" icono="hourglass_top" [acento]="true" [nota]="antiguedad()" />
          </a>
          <a class="kpi-link" routerLink="/gestor/reportes" [queryParams]="{ estado: 'ADMITIDO' }">
            <ca-kpi etiqueta="Por asignar · admitidos" [valor]="kpi().porAsignar" icono="assignment_ind" nota="Listos para cuadrilla" />
          </a>
          <a class="kpi-link" routerLink="/gestor/asignaciones">
            <ca-kpi etiqueta="En campo · en proceso" [valor]="kpi().enCampo" icono="engineering" [nota]="kpi().atendidos === 1 ? '1 atendido esperando cierre' : kpi().atendidos + ' atendidos esperando cierre'" />
          </a>
        </section>

        <section class="ca-card urgent">
          <div class="ca-card__head">
            <div>
              <div class="eyebrow urgent__eyebrow"><span class="pulse"></span> Requiere atención inmediata (Triage)</div>
              <h2>Casos críticos sin resolver</h2>
            </div>
            <span class="muted">{{ criticos().length }} de {{ totalCriticos() }}</span>
          </div>

          @for (r of criticos(); track r.id) {
            <article class="case">
              <div class="case__icon"><mat-icon>{{ icono(r.categoriaIcono) }}</mat-icon></div>
              <div class="case__main">
                <div class="case__top">
                  <span class="mono muted">{{ r.codigoSeguimiento }}</span>
                  <ca-estado-badge [estado]="r.estado" />
                  <ca-prioridad-badge [prioridad]="r.prioridad" />
                  @if (r.reportesCercanos && r.reportesCercanos > 1) {
                    <span class="badge cluster">{{ r.reportesCercanos }} reportes en 100 m</span>
                  }
                </div>
                <h3 class="case__title">{{ r.titulo }}</h3>
                <div class="muted case__meta">{{ r.categoriaNombre }} · {{ r.direccion || 'Sin dirección' }} · {{ r.creadoEn | date: 'd MMM, h:mm a' }}</div>
              </div>
              <div class="case__actions">
                <button mat-button (click)="ver(r)"><mat-icon>visibility</mat-icon> Ficha</button>
                <button class="btn-cta" mat-flat-button (click)="validarYAsignar(r)">
                  <mat-icon>bolt</mat-icon> {{ esValidable(r) ? 'Validar y Asignar' : 'Asignar' }}
                </button>
              </div>
            </article>
          } @empty {
            <div class="calm"><mat-icon>eco</mat-icon> Sin casos críticos pendientes. Buen trabajo.</div>
          }
        </section>

        <section class="quick">
          <a class="ca-card quick__item" routerLink="/gestor/reportes" [queryParams]="{ estado: 'PENDIENTE' }">
            <mat-icon>inbox</mat-icon>
            <h3>Bandeja de validación</h3>
            <p class="muted">Admite con prioridad oficial o rechaza con justificación.</p>
          </a>
          <a class="ca-card quick__item" routerLink="/gestor/asignaciones">
            <mat-icon>engineering</mat-icon>
            <h3>Gestión de cuadrillas</h3>
            <p class="muted">Carga por operador y reasignaciones sin perder historial.</p>
          </a>
          <a class="ca-card quick__item" routerLink="/gestor/mapa">
            <mat-icon>map</mat-icon>
            <h3>Mapa interactivo</h3>
            <p class="muted">Incidencias georreferenciadas por estado y categoría.</p>
          </a>
        </section>
      </ca-view-state>
    </div>
  `,
  styles: `
    .kpi-link { text-decoration: none; color: inherit; display: block; }
    .kpi-link:hover ::ng-deep .kpi { outline: 2px solid var(--ca-sage); }
    .urgent { margin-top: 24px; border-top: 4px solid var(--ca-danger); }
    .urgent__eyebrow { color: var(--ca-danger); display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
    .pulse { width: 8px; height: 8px; background: var(--ca-danger); animation: pulse 1.4s ease-in-out infinite; }
    @keyframes pulse { 50% { opacity: 0.25; } }
    @media (prefers-reduced-motion: reduce) { .pulse { animation: none; } }
    .case { display: grid; grid-template-columns: 44px 1fr auto; gap: 14px; align-items: center; padding: 14px 0; border-bottom: 1px solid var(--ca-border); }
    .case:last-child { border-bottom: none; }
    .case__icon { width: 44px; height: 44px; display: grid; place-items: center; border: 1px solid var(--ca-border); background: var(--ca-input); color: var(--ca-primary); }
    .case__top { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
    .case__title { margin: 6px 0 2px; font-size: 16px; }
    .case__meta { font-size: 13px; }
    .case__actions { display: flex; gap: 8px; align-items: center; }
    .cluster { color: var(--ca-on-surface-variant); }
    .calm { display: flex; align-items: center; gap: 10px; padding: 18px 0; color: var(--ca-primary); }
    .quick { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; margin-top: 24px; }
    .quick__item { text-decoration: none; color: inherit; display: flex; flex-direction: column; gap: 6px; }
    .quick__item:hover { border-color: var(--ca-sage); background: var(--ca-accent-soft); }
    .quick__item mat-icon { color: var(--ca-primary); margin-top: 6px; }
    .quick__item p { margin: 0; font-size: 14px; }
    @media (max-width: 760px) {
      .case { grid-template-columns: 1fr; }
      .case__icon { display: none; }
    }
  `,
})
export class TriageComponent {
  private readonly api = inject(CiudadActivaApi);
  private readonly dialog = inject(MatDialog);

  protected readonly reportes = asyncState(() => this.api.reportes());

  protected readonly kpi = computed(() => {
    const l = this.reportes.data() ?? [];
    const c = (f: (r: Reporte) => boolean) => l.filter(f).length;
    return {
      pendientes: c((r) => r.estado === 'PENDIENTE' || r.estado === 'EN_REVISION'),
      porAsignar: c((r) => r.estado === 'ADMITIDO'),
      enCampo: c((r) => r.estado === 'EN_PROCESO'),
      atendidos: c((r) => r.estado === 'ATENDIDO'),
    };
  });

  private readonly candidatos = computed(() =>
    (this.reportes.data() ?? [])
      .filter((r) => puedeValidar(r.estado) || puedeAsignar(r.estado))
      .filter(
        (r) =>
          r.prioridad === 'CRITICA' ||
          r.prioridad === 'ALTA' ||
          (r.analisisIaNivelUrgencia ?? 0) >= 4 ||
          ['ALTO', 'CRITICO', 'CRÍTICO'].includes((r.semaforoNivel ?? '').toUpperCase()),
      )
      .sort(
        (a, b) =>
          PRIORIDAD_PESO[b.prioridad] - PRIORIDAD_PESO[a.prioridad] ||
          (b.analisisIaNivelUrgencia ?? 0) - (a.analisisIaNivelUrgencia ?? 0) ||
          a.creadoEn.localeCompare(b.creadoEn),
      ),
  );
  protected readonly totalCriticos = computed(() => this.candidatos().length);
  protected readonly criticos = computed(() => this.candidatos().slice(0, 6));

  protected readonly antiguedad = computed(() => {
    const p = (this.reportes.data() ?? []).filter((r) => r.estado === 'PENDIENTE');
    if (!p.length) return 'Sin pendientes';
    const masViejo = p.reduce((a, b) => (a.creadoEn < b.creadoEn ? a : b));
    const dias = Math.floor((Date.now() - new Date(masViejo.creadoEn).getTime()) / 86_400_000);
    return dias <= 0 ? 'El más antiguo es de hoy' : `El más antiguo lleva ${dias} día${dias === 1 ? '' : 's'}`;
  });

  constructor() {
    this.reportes.cargar();
  }

  protected icono = iconoCategoria;
  protected esValidable = (r: Reporte) => puedeValidar(r.estado);

  protected ver(r: Reporte): void {
    abrirDetalle(this.dialog, r.id).subscribe();
  }

  /** Flujo encadenado: validar (admitir con prioridad) → asignar operador. */
  protected validarYAsignar(r: Reporte): void {
    const paso1$ = puedeValidar(r.estado) ? abrirValidar(this.dialog, r) : of(r);
    paso1$
      .pipe(
        filter((rep): rep is Reporte => !!rep),
        switchMap((rep) => (rep.estado === 'ADMITIDO' ? abrirAsignar(this.dialog, { reporte: rep }) : of(null))),
      )
      .subscribe({ complete: () => this.reportes.cargar() });
  }
}
