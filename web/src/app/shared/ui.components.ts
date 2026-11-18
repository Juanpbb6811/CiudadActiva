import { Component, computed, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ESTADO_LABEL, GRUPO_COLOR, PRIORIDAD_LABEL, grupoEstado } from '../core/api/labels';
import { EstadoReporte, PrioridadReporte } from '../core/api/models';
import { Estado } from '../core/ui/async-state';
import { ProblemDetail } from '../core/http/problem-detail';

/** Encabezado de página: sección + título + descripción + acciones. */
@Component({
  selector: 'ca-page-header',
  template: `
    <header class="ph">
      <div class="ph__text">
        <div class="eyebrow">{{ seccion() }}</div>
        <h1>{{ titulo() }}</h1>
        @if (descripcion()) {
          <p class="muted ph__desc">{{ descripcion() }}</p>
        }
      </div>
      <div class="ph__actions"><ng-content /></div>
    </header>
  `,
  styles: `
    .ph {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 16px;
      flex-wrap: wrap;
      padding-bottom: 18px;
      margin-bottom: 24px;
      border-bottom: 2px solid var(--ca-on-surface-strong);
    }
    .ph__text h1 {
      margin-top: 6px;
    }
    .ph__desc {
      margin: 6px 0 0;
      max-width: 72ch;
    }
    .ph__actions {
      display: flex;
      gap: 10px;
      align-items: center;
      flex-wrap: wrap;
    }
  `,
})
export class PageHeaderComponent {
  codigo = input.required<string>();
  seccion = input<string>('');
  titulo = input.required<string>();
  descripcion = input<string>('');
}

/** Estados visuales uniformes: Loading, Empty, Error. Proyecta el contenido cuando está listo. */
@Component({
  selector: 'ca-view-state',
  imports: [MatProgressSpinnerModule, MatIconModule, MatButtonModule],
  template: `
    @switch (estado()) {
      @case ('loading') {
        <div class="vs" role="status" aria-live="polite">
          <mat-spinner diameter="36" />
          <div class="vs__title">{{ textoCarga() }}</div>
        </div>
      }
      @case ('error') {
        <div class="vs vs--error" role="alert">
          <mat-icon>report</mat-icon>
          <div class="vs__title">{{ error()?.title || 'No se pudo cargar' }}</div>
          <p class="vs__text">{{ error()?.detail }}</p>
          <button mat-stroked-button (click)="reintentar.emit()"><mat-icon>refresh</mat-icon> Reintentar</button>
        </div>
      }
      @case ('empty') {
        <div class="vs">
          <mat-icon>{{ iconoVacio() }}</mat-icon>
          <div class="vs__title">{{ tituloVacio() }}</div>
          <p class="vs__text">{{ textoVacio() }}</p>
          <ng-content select="[vacio]" />
        </div>
      }
      @default {
        <ng-content />
      }
    }
  `,
  styles: `
    .vs {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      gap: 10px;
      padding: 48px 24px;
      border: 1px dashed var(--ca-border);
      background: var(--ca-card);
    }
    .vs > mat-icon {
      font-size: 36px;
      width: 36px;
      height: 36px;
      color: var(--ca-on-surface-variant);
    }
    .vs__title {
      font-family: var(--ca-font-display);
      font-weight: 700;
      font-size: 17px;
      color: var(--ca-on-surface-strong);
    }
    .vs__text {
      margin: 0;
      max-width: 52ch;
      color: var(--ca-on-surface-variant);
    }
    .vs--error {
      border-style: solid;
      border-color: var(--ca-danger);
      background: var(--ca-danger-soft);
    }
    .vs--error > mat-icon {
      color: var(--ca-danger);
    }
  `,
})
export class ViewStateComponent {
  estado = input.required<Estado>();
  error = input<ProblemDetail | null>(null);
  textoCarga = input('Cargando información…');
  tituloVacio = input('Sin registros');
  textoVacio = input('No hay elementos que coincidan con los filtros actuales.');
  iconoVacio = input('inventory_2');
  reintentar = output<void>();
}

@Component({
  selector: 'ca-estado-badge',
  template: `<span class="badge" [style.color]="color()" [attr.title]="estado()">{{ label() }}</span>`,
})
export class EstadoBadgeComponent {
  estado = input.required<EstadoReporte>();
  protected readonly label = computed(() => ESTADO_LABEL[this.estado()] ?? this.estado());
  protected readonly color = computed(() => {
    const e = this.estado();
    if (e === 'RECHAZADO') return 'var(--ca-danger)';
    const g = grupoEstado(e);
    return g === 'pendiente' ? 'var(--ca-warn)' : GRUPO_COLOR[g];
  });
}

@Component({
  selector: 'ca-prioridad-badge',
  template: `<span class="badge" [style.color]="color()">{{ label() }}</span>`,
})
export class PrioridadBadgeComponent {
  prioridad = input.required<PrioridadReporte | null | undefined>();
  protected readonly label = computed(() => {
    const p = this.prioridad();
    return p ? PRIORIDAD_LABEL[p] : 'Sin prioridad';
  });
  protected readonly color = computed(() => {
    switch (this.prioridad()) {
      case 'CRITICA':
        return 'var(--ca-danger)';
      case 'ALTA':
        return '#C46A2B';
      case 'MEDIA':
        return 'var(--ca-warn)';
      case 'BAJA':
        return 'var(--ca-primary)';
      default:
        return 'var(--ca-on-surface-variant)';
    }
  });
}

/** Tarjeta KPI con número grande en Epilogue. */
@Component({
  selector: 'ca-kpi',
  imports: [MatIconModule],
  template: `
    <div class="kpi" [class.kpi--accent]="acento()">
      <div class="kpi__top">
        <span class="eyebrow">{{ etiqueta() }}</span>
        @if (icono()) {
          <mat-icon>{{ icono() }}</mat-icon>
        }
      </div>
      <div class="kpi-value">{{ valor() }}<small>{{ sufijo() }}</small></div>
      @if (nota()) {
        <div class="kpi__note">{{ nota() }}</div>
      }
    </div>
  `,
  styles: `
    .kpi {
      background: var(--ca-card);
      border: 1px solid var(--ca-border);
      padding: 18px 20px 16px;
      height: 100%;
      box-sizing: border-box;
      position: relative;
    }
    .kpi--accent::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      right: 0;
      height: 4px;
      background: var(--ca-cta);
    }
    .kpi__top {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .kpi__top mat-icon {
      color: var(--ca-on-surface-variant);
    }
    .kpi-value {
      font-size: 42px;
      font-weight: 800;
      line-height: 1.1;
      margin-top: 10px;
    }
    .kpi-value small {
      font-size: 16px;
      font-weight: 600;
      margin-left: 4px;
      color: var(--ca-on-surface-variant);
    }
    .kpi__note {
      font-size: 13px;
      color: var(--ca-on-surface-variant);
      margin-top: 4px;
    }
  `,
})
export class KpiComponent {
  etiqueta = input.required<string>();
  valor = input.required<string | number>();
  sufijo = input('');
  nota = input('');
  icono = input('');
  acento = input(false);
}
