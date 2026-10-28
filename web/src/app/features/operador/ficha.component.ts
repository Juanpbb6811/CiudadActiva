import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { ApiConfigService } from '../../core/api-config.service';
import { ApiService } from '../../core/api.service';
import { EstadoReporte, ReporteDetalle } from '../../core/models';
import { labelEstado, mensajeError } from '../../core/constants';
import { ToastService } from '../../core/toast.service';
import { Router } from '@angular/router';
import { SemaforoBadgeComponent, StatusChipComponent, PrioridadChipComponent } from '../../shared/chips.component';
import { IconComponent } from '../../shared/icon.component';
import { MapViewComponent } from '../../shared/map-view.component';
import { ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-operador-ficha',
  standalone: true,
  imports: [DatePipe, TopBarComponent, StatusChipComponent, PrioridadChipComponent, SemaforoBadgeComponent, IconComponent, MapViewComponent, LoadingComponent, ErrorViewComponent],
  template: `
    <app-top-bar title="Orden de Trabajo" [subtitle]="'Incidencia #' + id()" [back]="true" fallback="/operador/tareas" />
    <div class="page page-narrow">
      @if (cargando()) { <app-loading [n]="3" /> }
      @if (error()) { <app-error-view [message]="error()" (retried)="cargar()" /> }
      @if (rep(); as r) {
        <small class="kicker">ORDEN DE TRABAJO EN TERRENO</small>
        <div class="card mt-sm">
          <div class="row between wrap gap-sm"><small class="kicker">{{ (r.categoriaNombre ?? '').toUpperCase() }}</small><app-status-chip [estado]="r.estado" /></div>
          <h2 class="mt-sm">{{ r.titulo }}</h2>
          <p class="mt-sm">{{ r.descripcion }}</p>
          <div class="row between wrap gap-sm mt"><app-semaforo-badge [hex]="r.semaforoColorHex" [nivel]="r.semaforoNivel" />@if (r.prioridad) { <app-prioridad-chip [prioridad]="r.prioridad" /> }</div>
          @if (r.asignacionActual?.notas) { <div class="alert info mt"><span><strong>Instrucciones del gestor:</strong> {{ r.asignacionActual!.notas }}</span></div> }
        </div>

        @if (r.latitud != null && r.longitud != null) {
          <a class="card clickable row between mt" target="_blank" rel="noopener" [href]="'https://www.google.com/maps/dir/?api=1&destination=' + r.latitud + ',' + r.longitud">
            <div><strong>📍 {{ r.direccion ?? 'Ubicación del incidente' }}</strong><p class="muted small">Tocar para abrir GPS / Navegación</p></div><app-icon name="navigation" />
          </a>
          <div class="mt"><app-map-view [markers]="markers()" [lat]="r.latitud" [lng]="r.longitud" [zoom]="16" height="200px" /></div>
        }

        <h2 class="section-title">Foto reporte original (ciudadano)</h2>
        @if (r.tieneFoto) { <img class="reporte-img" style="height:auto;border-radius:16px" [src]="cfg.fotoReporte(r.id)" alt="Foto original" /> }
        @else { <div class="card muted center small">Sin foto adjunta por el ciudadano</div> }

        @if (r.estado !== 'CERRADO' && r.estado !== 'RECHAZADO') {
          <h2 class="section-title">Estado operativo</h2>
          <div class="row gap">
            <button class="btn ghost grow" type="button" [disabled]="trabajando()" (click)="marcar('EN_PROCESO')" [class.primary]="r.estado === 'EN_PROCESO'">En Proceso</button>
            <button class="btn ghost grow" type="button" [disabled]="trabajando()" (click)="marcar('ATENDIDO')" [class.primary]="r.estado === 'ATENDIDO'">Atendido</button>
          </div>
          <div class="col gap mt-lg">
            <button class="btn ghost" type="button" (click)="ir('avance')">+ Registrar Avance Operativo</button>
            <button class="btn primary" type="button" (click)="ir('cierre')">Proceder al Cierre con Evidencia</button>
          </div>
        } @else { <div class="alert ok mt">Este reporte está {{ lbl(r.estado) }}.</div> }

        @if (r.lineaDeTiempo.length) {
          <h2 class="section-title">Registro de campo</h2>
          <div class="timeline">
            @for (s of r.lineaDeTiempo; track s.id) {
              <div class="tl-item"><span class="tl-dot" style="background:var(--teal-light)"></span>
                <div class="card"><div class="row between"><strong class="small">{{ lbl(s.estadoNuevo ?? s.accion) }}</strong><small class="muted">{{ s.creadoEn | date: 'dd MMM, HH:mm' }}</small></div>
                  @if (s.observaciones) { <p class="small mt-sm">{{ s.observaciones }}</p> }<p class="xs muted">{{ s.usuarioNombre }}</p></div></div>
            }
          </div>
        }
      }
    </div>`,
})
export class OperadorFichaComponent implements OnInit {
  id = input.required<string>();
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  cfg = inject(ApiConfigService);
  rep = signal<ReporteDetalle | null>(null);
  cargando = signal(true);
  error = signal('');
  trabajando = signal(false);
  lbl = labelEstado;
  markers = computed(() => { const r = this.rep(); return r?.latitud != null && r?.longitud != null ? [{ id: r.id, lat: r.latitud, lng: r.longitud, color: r.semaforoColorHex || '#4CAF50' }] : []; });

  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.api.detalleReporte(+this.id()).subscribe({
      next: (r) => { this.rep.set(r); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  marcar(estado: EstadoReporte): void {
    this.trabajando.set(true);
    this.api.cambiarEstado(+this.id(), { estado, observaciones: 'Actualización operativa de estado en terreno' }).subscribe({
      next: () => { this.trabajando.set(false); this.toast.ok('Estado actualizado.'); this.cargar(); },
      error: (e) => { this.trabajando.set(false); this.toast.error(mensajeError(e)); },
    });
  }
  ir(dest: 'avance' | 'cierre'): void { this.router.navigate(['/operador', dest, this.id()]); }
}
