import { Component, computed, inject, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ApiConfigService } from '../core/api-config.service';
import { categoriaEmoji } from '../core/constants';
import { Reporte } from '../core/models';
import { StatusChipComponent, PrioridadChipComponent, SemaforoBadgeComponent } from './chips.component';

/** Tarjeta de reporte reutilizable: muro comunitario, mis reportes, bandejas, historial. */
@Component({
  selector: 'app-reporte-card',
  standalone: true,
  imports: [DatePipe, StatusChipComponent, PrioridadChipComponent, SemaforoBadgeComponent],
  template: `
    <article class="card reporte" [class.clickable]="clickable()" (click)="clickable() && abrir.emit(r().id)">
      @if (mostrarFoto() && r().tieneFoto) {
        <img class="reporte-img" [src]="cfg.fotoReporte(r().id)" alt="Foto del reporte" loading="lazy" />
      }
      <div class="reporte-body">
        <div class="row between wrap gap-sm">
          <small class="kicker">#{{ r().id }} • {{ emoji() }} {{ (r().categoriaNombre ?? 'Sin categoría').toUpperCase() }}</small>
          <app-status-chip [estado]="r().estado" />
        </div>
        <h3>{{ r().titulo }}</h3>
        @if (r().direccion) { <p class="muted small">📍 {{ r().direccion }}</p> }
        <div class="row between wrap gap-sm mt-sm">
          <app-semaforo-badge [hex]="r().semaforoColorHex" [nivel]="r().semaforoNivel" />
          @if (r().prioridad) { <app-prioridad-chip [prioridad]="r().prioridad" /> }
        </div>
        @if (mostrarAutor()) {
          <p class="muted xs mt-sm">Reportado por: {{ r().ciudadanoNombre ?? 'Anónimo' }} · {{ r().creadoEn | date: 'dd MMM yyyy, HH:mm' }}</p>
        }
        <ng-content />
      </div>
    </article>`,
})
export class ReporteCardComponent {
  r = input.required<Reporte>();
  clickable = input(false);
  mostrarFoto = input(true);
  mostrarAutor = input(false);
  abrir = output<number>();
  cfg = inject(ApiConfigService);
  emoji = computed(() => categoriaEmoji(this.r().categoriaIcono));
}
