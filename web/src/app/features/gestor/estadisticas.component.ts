import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { ESTADOS, PRIORIDADES, estadoStyle, labelEstado, mensajeError, prioridadStyle } from '../../core/constants';
import { Estadisticas } from '../../core/models';
import { SemaforoBadgeComponent } from '../../shared/chips.component';
import { ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-gestor-estadisticas',
  standalone: true,
  imports: [DecimalPipe, TopBarComponent, SemaforoBadgeComponent, LoadingComponent, ErrorViewComponent],
  template: `
    <app-top-bar title="Estadísticas" subtitle="Desempeño y semáforo urbano" />
    <div class="page">
      @if (error()) { <app-error-view [message]="error()" (retried)="cargar()" /> }
      @if (cargando()) { <app-loading [n]="3" /> }
      @if (s(); as s) {
        <h2 class="section-title" style="margin-top:6px">Semáforo global de saturación urbana</h2>
        <div class="card row gap" [style.border-left]="'6px solid ' + (s.semaforoGlobal?.colorHex ?? '#4CAF50')">
          <span style="font-size:34px" [style.color]="s.semaforoGlobal?.colorHex ?? '#4CAF50'">●</span>
          <div class="grow"><strong>{{ s.semaforoGlobal?.nivelAlerta?.replace('_', ' ') ?? 'SIN REPORTES' }}</strong>
            <p class="muted small">{{ s.semaforoGlobal?.descripcion ?? 'Sin incidencias' }}</p>
            <div class="bar-track mt-sm"><div class="bar-fill" [style.width.%]="s.semaforoGlobal?.porcentajeSaturacion ?? 0" [style.background]="s.semaforoGlobal?.colorHex ?? '#4CAF50'"></div></div></div>
        </div>

        <h2 class="section-title">Indicadores clave de desempeño</h2>
        <div class="grid grid-2">
          <div class="card kpi"><b>{{ s.totalReportes }}</b><small>TOTAL DE INCIDENTES</small></div>
          <div class="card kpi"><b style="color:#2e7d32">{{ s.reportesAtendidosOCerrados }}</b><small>ATENDIDOS O RESUELTOS</small></div>
          <div class="card kpi"><b style="color:#e65100">{{ s.reportesPendientes }}</b><small>PENDIENTES DE CIERRE</small></div>
          <div class="card kpi"><b>{{ s.tiempoPromedioAtencionHoras | number }}</b><small>HORAS PROM. DE SOLUCIÓN</small></div>
        </div>

        <div class="grid grid-2 mt-lg" style="align-items:start">
          <div class="card"><h3>Reportes por estado</h3>
            @for (e of estados; track e) { <div class="mt-sm"><div class="row between small"><span>{{ lbl(e) }}</span><b>{{ s.reportesPorEstado[e] }}</b></div>
              <div class="bar-track"><div class="bar-fill" [style.width.%]="pct(s.reportesPorEstado[e], s.totalReportes)" [style.background]="est(e).text"></div></div></div> }
          </div>
          <div class="card"><h3>Reportes por prioridad</h3>
            @for (p of prioridades; track p) { <div class="mt-sm"><div class="row between small"><span>{{ p }}</span><b>{{ s.reportesPorPrioridad[p] }}</b></div>
              <div class="bar-track"><div class="bar-fill" [style.width.%]="pct(s.reportesPorPrioridad[p], s.totalReportes)" [style.background]="pri(p).text"></div></div></div> }
          </div>
        </div>

        <h2 class="section-title">Estado semafórico por categoría</h2>
        <div class="grid grid-auto">
          @for (c of categorias(); track c[0]) {
            <div class="card row between"><div><strong class="small">{{ c[0] }}</strong><p class="muted xs">{{ c[1].cantidadReportes }} reportes · {{ c[1].descripcion }}</p></div><app-semaforo-badge [semaforo]="c[1]" /></div>
          } @empty { <p class="muted small">Sin datos por categoría.</p> }
        </div>
      }
    </div>`,
})
export class GestorEstadisticasComponent implements OnInit {
  private api = inject(ApiService);
  s = signal<Estadisticas | null>(null);
  cargando = signal(true);
  error = signal('');
  estados = ESTADOS; prioridades = PRIORIDADES; lbl = labelEstado; est = estadoStyle; pri = prioridadStyle;
  categorias = computed(() => Object.entries(this.s()?.semaforoPorCategoria ?? {}));
  pct = (v: number | undefined, t: number) => (t ? Math.round(((v ?? 0) / t) * 100) : 0);

  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.api.estadisticas().subscribe({
      next: (s) => { this.s.set(s); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
}
