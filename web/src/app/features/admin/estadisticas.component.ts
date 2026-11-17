import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { CiudadActivaApi } from '../../core/api/ciudad-activa.api';
import { ESTADO_LABEL, PRIORIDAD_LABEL } from '../../core/api/labels';
import { ESTADOS, Estadisticas, PRIORIDADES } from '../../core/api/models';
import { asyncState } from '../../core/ui/async-state';
import { ThemeService } from '../../core/ui/theme.service';
import { KpiComponent, PageHeaderComponent, ViewStateComponent } from '../../shared/ui.components';

/** Validador cruzado CP10: fechaInicio <= fechaFin */
const rangoValido: ValidatorFn = (g: AbstractControl): ValidationErrors | null => {
  const ini = g.get('inicio')?.value as Date | null;
  const fin = g.get('fin')?.value as Date | null;
  return ini && fin && ini.getTime() > fin.getTime() ? { rangoInvalido: true } : null;
};

/** Paleta validada (scripts/validate_palette.js) en claro (#FFFFFF) y oscuro (#1B2924). */
const PALETA = {
  light: { atendidas: '#13895A', curso: '#3B6FD0', rechazadas: '#C9452E', serie: '#3F6B55', grid: 'rgba(82,102,94,0.16)', texto: '#52665E' },
  dark: { atendidas: '#2E9E6E', curso: '#5B8FE6', rechazadas: '#E0603F', serie: '#7CA38E', grid: 'rgba(167,208,185,0.14)', texto: '#A7D0B9' },
};

@Component({
  selector: 'app-estadisticas',
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    BaseChartDirective,
    PageHeaderComponent,
    KpiComponent,
    ViewStateComponent,
  ],
  // Chart.js solo se carga con esta vista (lazy)
  providers: [provideCharts(withDefaultRegisterables())],
  template: `
    <div class="page">
      <ca-page-header codigo="A-01" seccion="Administrador" titulo="Tablero estadístico" descripcion="Indicadores de gestión por rango de fechas de registro: volumen por categoría, proporción atendidas frente a rechazadas y tiempo medio de resolución." />

      <form class="ca-card range" [formGroup]="rango" (ngSubmit)="aplicar()">
        <mat-form-field class="range__field">
          <mat-label>Rango de fechas</mat-label>
          <mat-date-range-input [rangePicker]="picker">
            <input matStartDate formControlName="inicio" placeholder="Fecha inicio" />
            <input matEndDate formControlName="fin" placeholder="Fecha fin" />
          </mat-date-range-input>
          <mat-hint>DD/MM/AAAA – DD/MM/AAAA</mat-hint>
          <mat-datepicker-toggle matIconSuffix [for]="picker" />
          <mat-date-range-picker #picker />
        </mat-form-field>
        <div class="presets" role="group" aria-label="Rangos rápidos">
          <button mat-button type="button" (click)="preset(7)">7 días</button>
          <button mat-button type="button" (click)="preset(30)">30 días</button>
          <button mat-button type="button" (click)="preset(90)">90 días</button>
          <button mat-button type="button" (click)="presetAnio()">Este año</button>
          <button mat-button type="button" (click)="presetTodo()">Todo</button>
        </div>
        <span class="spacer"></span>
        <button class="btn-cta" mat-flat-button type="submit" [disabled]="rango.invalid"><mat-icon>query_stats</mat-icon> Consultar</button>
        @if (rango.hasError('rangoInvalido')) {
          <div class="range__err" role="alert">La fecha de inicio no puede ser posterior a la fecha de fin.</div>
        }
      </form>

      <ca-view-state [estado]="stats.estado()" [error]="stats.error()" (reintentar)="stats.cargar()" textoCarga="Calculando indicadores…">
        @if (stats.data(); as s) {
          @if (s.totalReportes === 0) {
            <div class="ca-card empty">
              <mat-icon>inventory_2</mat-icon>
              <div><strong>Sin reportes en el rango seleccionado.</strong> <span class="muted">Amplía el rango o elige “Todo”.</span></div>
            </div>
          }
          <section class="grid-kpi">
            <ca-kpi etiqueta="Reportes en el rango" [valor]="s.totalReportes" icono="description" [acento]="true" [nota]="periodoTxt()" />
            <ca-kpi etiqueta="Atendidas" [valor]="(s.porcentajeAtendidas ?? 0 | number: '1.0-1') ?? '0'" sufijo="%" icono="task_alt" [nota]="s.reportesAtendidosOCerrados + ' atendidas o cerradas'" />
            <ca-kpi etiqueta="Rechazadas" [valor]="(s.porcentajeRechazadas ?? 0 | number: '1.0-1') ?? '0'" sufijo="%" icono="block" [nota]="(s.reportesRechazados ?? 0) + ' rechazadas con justificación'" />
            <ca-kpi etiqueta="Tiempo medio de resolución" [valor]="(s.tiempoPromedioAtencionHoras | number: '1.0-1') ?? '0'" sufijo="h" icono="timer" [nota]="tiempoNota(s)" />
          </section>

          <section class="charts">
            <article class="ca-card chart chart--wide">
              <div class="ca-card__head">
                <div>
                  <h2>Reportes por categoría</h2>
                  <span class="muted small">Ordenado de mayor a menor</span>
                </div>
                <mat-button-toggle-group [value]="vistaCat()" (change)="vistaCat.set($event.value)" hideSingleSelectionIndicator aria-label="Vista">
                  <mat-button-toggle value="grafico">Gráfico</mat-button-toggle>
                  <mat-button-toggle value="tabla">Tabla</mat-button-toggle>
                </mat-button-toggle-group>
              </div>
              @if (vistaCat() === 'grafico') {
                <div class="chart__canvas" [style.height.px]="alturaCat()">
                  <canvas baseChart type="bar" [data]="datosCategoria()" [options]="opcionesCategoria()" aria-label="Gráfico de barras de reportes por categoría"></canvas>
                </div>
              } @else {
                <table class="tbl">
                  <thead><tr><th>Categoría</th><th class="num">Reportes</th><th class="num">% del total</th></tr></thead>
                  <tbody>
                    @for (c of categoriasOrdenadas(); track c.nombre) {
                      <tr><td>{{ c.nombre }}</td><td class="num">{{ c.total }}</td><td class="num">{{ pct(c.total, s.totalReportes) }}%</td></tr>
                    }
                  </tbody>
                </table>
              }
            </article>

            <article class="ca-card chart">
              <div class="ca-card__head"><h2>Atendidas vs. rechazadas</h2></div>
              <div class="prop" role="img" [attr.aria-label]="propAria()">
                @for (seg of proporcion(); track seg.clave) {
                  @if (seg.valor > 0) {
                    <div class="prop__seg" [style.flex-grow]="seg.valor" [style.background]="seg.color" [attr.title]="seg.label + ': ' + seg.valor"></div>
                  }
                }
              </div>
              <ul class="legend">
                @for (seg of proporcion(); track seg.clave) {
                  <li>
                    <span class="legend__sw" [style.background]="seg.color"></span>
                    <span class="legend__l">{{ seg.label }}</span>
                    <span class="legend__v">{{ seg.valor }}</span>
                    <span class="legend__p muted">{{ pct(seg.valor, s.totalReportes) }}%</span>
                  </li>
                }
              </ul>
              <p class="muted small">Atendidas = ATENDIDO + CERRADO. En curso = resto de estados no finales.</p>
            </article>

            <article class="ca-card chart">
              <div class="ca-card__head"><h2>Distribución por estado</h2></div>
              <div class="chart__canvas" style="height: 260px">
                <canvas baseChart type="bar" [data]="datosEstado()" [options]="opcionesEstado()" aria-label="Gráfico de reportes por estado"></canvas>
              </div>
            </article>

            <article class="ca-card chart">
              <div class="ca-card__head"><h2>Por prioridad oficial</h2></div>
              <ul class="bars">
                @for (p of prioridadFilas(); track p.label) {
                  <li>
                    <span class="bars__l">{{ p.label }}</span>
                    <span class="bars__track"><span class="bars__fill" [style.width.%]="p.ancho"></span></span>
                    <span class="bars__v">{{ p.valor }}</span>
                  </li>
                }
              </ul>
            </article>
          </section>
        }
      </ca-view-state>
    </div>
  `,
  styles: `
    .range { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin-bottom: 20px; }
    .range__field { width: 300px; }
    .presets { display: flex; flex-wrap: wrap; gap: 2px; }
    .range__err { flex-basis: 100%; color: var(--ca-danger); font-weight: 600; font-size: 14px; }
    .empty { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
    .charts { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-top: 16px; }
    .chart--wide { grid-column: 1 / -1; }
    .chart__canvas { position: relative; width: 100%; }
    .small { font-size: 12px; }
    .tbl { width: 100%; border-collapse: collapse; font-size: 14px; }
    .tbl th { text-align: left; font-size: 11px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--ca-on-surface-variant); padding: 8px; background: var(--ca-input); }
    .tbl td { padding: 8px; border-bottom: 1px solid var(--ca-border); }
    .num { text-align: right !important; font-variant-numeric: tabular-nums; }
    .prop { display: flex; gap: 2px; height: 40px; border: 1px solid var(--ca-border); background: var(--ca-card); padding: 2px; margin: 8px 0 16px; }
    .prop__seg { min-width: 3px; }
    .legend { list-style: none; padding: 0; margin: 0 0 12px; display: flex; flex-direction: column; gap: 8px; }
    .legend li { display: grid; grid-template-columns: 14px 1fr auto 60px; gap: 10px; align-items: center; font-size: 14px; }
    .legend__sw { width: 12px; height: 12px; }
    .legend__v { font-family: var(--ca-font-display); font-weight: 700; font-variant-numeric: tabular-nums; }
    .legend__p { text-align: right; font-variant-numeric: tabular-nums; }
    .bars { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 14px; }
    .bars li { display: grid; grid-template-columns: 70px 1fr 40px; gap: 10px; align-items: center; font-size: 14px; }
    .bars__track { height: 14px; background: var(--ca-input); border: 1px solid var(--ca-border); }
    .bars__fill { display: block; height: 100%; background: var(--ca-sage); }
    .bars__v { text-align: right; font-weight: 700; font-variant-numeric: tabular-nums; }
    @media (max-width: 900px) { .charts { grid-template-columns: 1fr; } }
  `,
})
export class EstadisticasComponent {
  private readonly api = inject(CiudadActivaApi);
  private readonly theme = inject(ThemeService);

  protected readonly rango = new FormGroup(
    { inicio: new FormControl<Date | null>(null), fin: new FormControl<Date | null>(null) },
    { validators: rangoValido },
  );
  private readonly consulta = signal<{ ini: string | null; fin: string | null }>({ ini: null, fin: null });
  protected readonly stats = asyncState(() => this.api.estadisticas(this.consulta().ini, this.consulta().fin), () => false);
  protected readonly vistaCat = signal<'grafico' | 'tabla'>('grafico');

  private readonly pal = computed(() => PALETA[this.theme.tema()]);

  constructor() {
    this.presetAnio();
  }

  // ---------- rango ----------
  protected preset(dias: number): void {
    const fin = new Date();
    const ini = new Date();
    ini.setDate(fin.getDate() - (dias - 1));
    this.rango.setValue({ inicio: ini, fin });
    this.aplicar();
  }
  protected presetAnio(): void {
    const hoy = new Date();
    this.rango.setValue({ inicio: new Date(hoy.getFullYear(), 0, 1), fin: hoy });
    this.aplicar();
  }
  protected presetTodo(): void {
    this.rango.setValue({ inicio: null, fin: null });
    this.aplicar();
  }
  protected aplicar(): void {
    if (this.rango.invalid) return; // CP10: validación en frontend (el backend también responde 400)
    const { inicio, fin } = this.rango.getRawValue();
    this.consulta.set({ ini: iso(inicio), fin: iso(fin) });
    this.stats.cargar();
  }
  protected periodoTxt(): string {
    const { ini, fin } = this.consulta();
    if (!ini && !fin) return 'Todo el histórico';
    return `${ini ? fmt(ini) : 'inicio'} → ${fin ? fmt(fin) : 'hoy'}`;
  }
  protected tiempoNota(s: Estadisticas): string {
    const n = s.reportesCerradosConTiempo ?? 0;
    const dias = s.tiempoPromedioAtencionHoras / 24;
    const base = n ? `Sobre ${n} reporte${n === 1 ? '' : 's'} cerrado${n === 1 ? '' : 's'}` : 'Aún sin cierres en el rango';
    return dias >= 2 ? `${base} (≈ ${dias.toLocaleString('es-CO', { maximumFractionDigits: 1 })} días)` : base;
  }
  protected pct(v: number, total: number): string {
    return (total ? (v * 100) / total : 0).toLocaleString('es-CO', { maximumFractionDigits: 1 });
  }

  // ---------- datos derivados ----------
  protected readonly categoriasOrdenadas = computed(() =>
    Object.entries(this.stats.data()?.reportesPorCategoria ?? {})
      .map(([nombre, total]) => ({ nombre, total }))
      .sort((a, b) => b.total - a.total),
  );
  protected readonly alturaCat = computed(() => Math.max(180, this.categoriasOrdenadas().length * 40 + 40));

  protected readonly proporcion = computed(() => {
    const s = this.stats.data();
    const p = this.pal();
    const at = s?.reportesAtendidosOCerrados ?? 0;
    const re = s?.reportesRechazados ?? s?.reportesPorEstado?.['RECHAZADO'] ?? 0;
    const cu = Math.max(0, (s?.totalReportes ?? 0) - at - re);
    return [
      { clave: 'at', label: 'Atendidas', valor: at, color: p.atendidas },
      { clave: 'cu', label: 'En curso', valor: cu, color: p.curso },
      { clave: 're', label: 'Rechazadas', valor: re, color: p.rechazadas },
    ];
  });
  protected readonly propAria = computed(() => this.proporcion().map((s) => `${s.label} ${s.valor}`).join(', '));

  protected readonly prioridadFilas = computed(() => {
    const m = this.stats.data()?.reportesPorPrioridad ?? {};
    const max = Math.max(1, ...PRIORIDADES.map((p) => m[p] ?? 0));
    return [...PRIORIDADES].reverse().map((p) => ({ label: PRIORIDAD_LABEL[p], valor: m[p] ?? 0, ancho: ((m[p] ?? 0) / max) * 100 }));
  });

  protected readonly datosCategoria = computed<ChartConfiguration<'bar'>['data']>(() => ({
    labels: this.categoriasOrdenadas().map((c) => c.nombre),
    datasets: [{ label: 'Reportes', data: this.categoriasOrdenadas().map((c) => c.total), backgroundColor: this.pal().serie, borderWidth: 0, barThickness: 22, borderRadius: 0 }],
  }));
  protected readonly opcionesCategoria = computed(() => this.opciones('y'));

  protected readonly datosEstado = computed<ChartConfiguration<'bar'>['data']>(() => {
    const m = this.stats.data()?.reportesPorEstado ?? {};
    return {
      labels: ESTADOS.map((e) => ESTADO_LABEL[e].split(' · ')[0]),
      datasets: [{ label: 'Reportes', data: ESTADOS.map((e) => m[e] ?? 0), backgroundColor: this.pal().serie, borderWidth: 0, maxBarThickness: 36, borderRadius: 0 }],
    };
  });
  protected readonly opcionesEstado = computed(() => this.opciones('x'));

  private opciones(eje: 'x' | 'y'): ChartConfiguration<'bar'>['options'] {
    const p = this.pal();
    const fuente = { family: 'Hanken Grotesk, sans-serif', size: 12 };
    const valor = { grid: { color: p.grid }, border: { display: false }, ticks: { color: p.texto, font: fuente, precision: 0 }, beginAtZero: true };
    const cat = { grid: { display: false }, border: { color: p.grid }, ticks: { color: p.texto, font: fuente } };
    return {
      indexAxis: eje,
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 300 },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: this.theme.tema() === 'dark' ? '#22342E' : '#0C1F19',
          titleFont: { family: 'Epilogue, sans-serif', weight: 'bold' },
          bodyFont: fuente,
          cornerRadius: 0,
          displayColors: false,
          callbacks: { label: (c) => `${c.formattedValue} reportes` },
        },
      },
      scales: eje === 'y' ? { x: valor, y: cat } : { x: cat, y: valor },
    };
  }
}

function iso(d: Date | null): string | null {
  if (!d) return null;
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
}
function fmt(isoStr: string): string {
  const [y, m, d] = isoStr.split('-');
  return `${d}/${m}/${y}`;
}
