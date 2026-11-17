import { Component, DestroyRef, ElementRef, afterNextRender, computed, effect, inject, signal, untracked, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import * as L from 'leaflet';
import { CiudadActivaApi } from '../../core/api/ciudad-activa.api';
import { ESTADO_LABEL, GRUPO_COLOR, GrupoEstado, grupoEstado } from '../../core/api/labels';
import { ESTADOS, EstadoReporte, Reporte } from '../../core/api/models';
import { asyncState } from '../../core/ui/async-state';
import { ReporteDetalleComponent } from '../../shared/reporte-detalle.component';
import { PageHeaderComponent } from '../../shared/ui.components';

const BOGOTA: L.LatLngExpression = [4.6486, -74.0838];

@Component({
  selector: 'app-mapa',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatSelectModule, MatButtonModule, MatIconModule, PageHeaderComponent, ReporteDetalleComponent],
  template: `
    <div class="page page--map">
      <ca-page-header codigo="G-04" seccion="Gestor" titulo="Mapa interactivo de incidencias" descripcion="OpenStreetMap con los reportes georreferenciados. Pulsa un pin para ver la ficha, el historial y comentar.">
        <button mat-stroked-button (click)="reportes.cargar()"><mat-icon>refresh</mat-icon> Actualizar</button>
      </ca-page-header>

      <div class="filters">
        <mat-form-field>
          <mat-label>Categoría</mat-label>
          <mat-select [formControl]="fCategoria">
            <mat-option [value]="null">Todas</mat-option>
            @for (c of categorias.data() ?? []; track c.id) {
              <mat-option [value]="c.id">{{ c.nombre }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Estado</mat-label>
          <mat-select [formControl]="fEstado">
            <mat-option [value]="null">Todos</mat-option>
            @for (e of estados; track e) {
              <mat-option [value]="e">{{ estadoLabel[e] }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <div class="legend" aria-label="Leyenda de colores">
          @for (g of leyenda; track g.grupo) {
            <span class="legend__i"><span class="legend__sw" [style.background]="g.color"></span>{{ g.label }} <b class="mono">{{ conteo()[g.grupo] }}</b></span>
          }
        </div>
      </div>

      <div class="stage" [class.stage--open]="seleccion() !== null">
        <div class="map-wrap ca-card ca-card--flush">
          <div #mapEl class="map" role="application" aria-label="Mapa de reportes"></div>
          @if (reportes.estado() === 'loading') {
            <div class="overlay">Cargando reportes georreferenciados…</div>
          } @else if (reportes.estado() === 'error') {
            <div class="overlay overlay--error">
              <strong>{{ reportes.error()?.title }}</strong>
              <span>{{ reportes.error()?.detail }}</span>
              <button mat-stroked-button (click)="reportes.cargar()">Reintentar</button>
            </div>
          } @else if (reportes.estado() === 'empty') {
            <div class="overlay">Sin reportes con coordenadas para estos filtros.</div>
          }
        </div>
        @if (seleccion(); as id) {
          <aside class="drawer ca-card ca-card--flush">
            <ca-reporte-detalle [reporteId]="id" [permitirComentar]="true" (cerrar)="seleccion.set(null)" />
          </aside>
        }
      </div>
    </div>
  `,
  styles: `
    .page--map { max-width: none; padding-bottom: 16px; }
    .filters { margin-bottom: 14px; align-items: center; }
    .legend { display: flex; flex-wrap: wrap; gap: 14px; padding: 8px 12px; border: 1px solid var(--ca-border); background: var(--ca-card); font-size: 13px; }
    .legend__i { display: inline-flex; align-items: center; gap: 6px; }
    .legend__sw { width: 10px; height: 10px; transform: rotate(45deg); border: 1.5px solid #0c1f19; }
    .stage { display: grid; grid-template-columns: 1fr; gap: 16px; height: calc(100vh - 300px); min-height: 440px; }
    .stage--open { grid-template-columns: 1fr minmax(340px, 440px); }
    .map-wrap { position: relative; overflow: hidden; }
    .map { position: absolute; inset: 0; }
    .overlay { position: absolute; left: 50%; top: 16px; transform: translateX(-50%); z-index: 500; background: var(--ca-card); border: 1px solid var(--ca-border); padding: 10px 16px; font-size: 14px; display: flex; gap: 10px; align-items: center; }
    .overlay--error { border-color: var(--ca-danger); background: var(--ca-danger-soft); flex-wrap: wrap; max-width: 80%; }
    .drawer { overflow: auto; }
    @media (max-width: 960px) {
      .stage, .stage--open { grid-template-columns: 1fr; height: auto; }
      .map-wrap { height: 60vh; }
      .drawer { max-height: none; }
    }
  `,
})
export class MapaComponent {
  private readonly api = inject(CiudadActivaApi);
  private readonly destroyRef = inject(DestroyRef);
  private readonly mapEl = viewChild.required<ElementRef<HTMLDivElement>>('mapEl');

  protected readonly estados = ESTADOS;
  protected readonly estadoLabel = ESTADO_LABEL;
  protected readonly leyenda: { grupo: GrupoEstado; label: string; color: string }[] = [
    { grupo: 'pendiente', label: 'Creado / pendiente', color: GRUPO_COLOR.pendiente },
    { grupo: 'proceso', label: 'Asignado / en proceso', color: GRUPO_COLOR.proceso },
    { grupo: 'cerrado', label: 'Cerrado', color: GRUPO_COLOR.cerrado },
    { grupo: 'rechazado', label: 'Rechazado', color: GRUPO_COLOR.rechazado },
  ];

  protected readonly fCategoria = new FormControl<number | null>(null);
  protected readonly fEstado = new FormControl<EstadoReporte | null>(null);
  protected readonly categorias = asyncState(() => this.api.categoriasActivas());
  protected readonly reportes = asyncState(() => this.api.reportesGeolocalizados(this.fCategoria.value, this.fEstado.value));
  protected readonly seleccion = signal<number | null>(null);

  protected readonly conteo = computed(() => {
    const c: Record<GrupoEstado, number> = { pendiente: 0, proceso: 0, cerrado: 0, rechazado: 0 };
    for (const r of this.reportes.data() ?? []) c[grupoEstado(r.estado)]++;
    return c;
  });

  private map: L.Map | null = null;
  private capa = L.layerGroup();
  private primerAjuste = true;

  constructor() {
    this.categorias.cargar();
    this.reportes.cargar();
    this.fCategoria.valueChanges.subscribe(() => this.reportes.cargar());
    this.fEstado.valueChanges.subscribe(() => this.reportes.cargar());

    afterNextRender(() => {
      this.map = L.map(this.mapEl().nativeElement, { zoomControl: true, attributionControl: true }).setView(BOGOTA, 12);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; colaboradores de <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      }).addTo(this.map);
      this.capa.addTo(this.map);
      this.pintar(this.reportes.data() ?? [], this.seleccion());
    });

    effect(() => {
      const datos = this.reportes.data() ?? [];
      const sel = this.seleccion();
      untracked(() => this.pintar(datos, sel));
    });

    // Recalcular tamaño cuando se abre/cierra el panel lateral
    effect(() => {
      this.seleccion();
      setTimeout(() => this.map?.invalidateSize(), 50);
    });

    this.destroyRef.onDestroy(() => this.map?.remove());
  }

  private pintar(reportes: Reporte[], sel: number | null): void {
    if (!this.map) return;
    this.capa.clearLayers();
    const puntos: L.LatLngExpression[] = [];
    for (const r of reportes) {
      if (r.latitud == null || r.longitud == null) continue;
      const color = GRUPO_COLOR[grupoEstado(r.estado)];
      const icon = L.divIcon({
        className: '',
        html: `<div class="pin-hit"><div class="pin${r.id === sel ? ' pin--sel' : ''}" style="background:${color}"></div></div>`,
        iconSize: [30, 30],
        iconAnchor: [15, 15],
      });
      const m = L.marker([r.latitud, r.longitud], { icon, title: `${r.codigoSeguimiento} · ${r.titulo}`, keyboard: true });
      m.bindTooltip(`<strong>${escapar(r.titulo)}</strong><br>${escapar(r.categoriaNombre)} · ${ESTADO_LABEL[r.estado]}`, { direction: 'top', offset: [0, -12] });
      m.on('click', () => this.seleccion.set(r.id));
      m.addTo(this.capa);
      // Evita que el foco del pin desplace el contenedor entre mousedown y mouseup (el clic se perdía).
      // Con teclado sigue funcionando: Tab enfoca el pin y Enter lo abre.
      m.getElement()?.addEventListener('mousedown', (ev) => ev.preventDefault());
      puntos.push([r.latitud, r.longitud]);
    }
    if (this.primerAjuste && puntos.length) {
      this.map.fitBounds(L.latLngBounds(puntos), { padding: [40, 40], maxZoom: 15 });
      this.primerAjuste = false;
    }
  }
}

function escapar(s: string | null | undefined): string {
  return (s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]!);
}
