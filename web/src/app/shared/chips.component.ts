import { Component, computed, input } from '@angular/core';
import { estadoStyle, labelEstado, prioridadStyle } from '../core/constants';
import { Semaforo } from '../core/models';

@Component({
  selector: 'app-status-chip',
  standalone: true,
  template: `<span class="chip" [style.background]="st().bg" [style.color]="st().text" [style.border-color]="st().border">{{ label() }}</span>`,
})
export class StatusChipComponent {
  estado = input.required<string>();
  st = computed(() => estadoStyle(this.estado()));
  label = computed(() => labelEstado(this.estado()));
}

@Component({
  selector: 'app-prioridad-chip',
  standalone: true,
  template: `<span class="chip chip-sm" [style.background]="st().bg" [style.color]="st().text" [style.border-color]="'transparent'">PRIORIDAD {{ p() }}</span>`,
})
export class PrioridadChipComponent {
  prioridad = input<string | null | undefined>('MEDIA');
  p = computed(() => (this.prioridad() ?? 'MEDIA').toUpperCase());
  st = computed(() => prioridadStyle(this.p()));
}

@Component({
  selector: 'app-semaforo-badge',
  standalone: true,
  template: `
    <span class="semaforo" [title]="s()?.descripcion ?? ''">
      <i class="dot" [style.background]="s()?.colorHex ?? '#4CAF50'"></i>
      @if (showLabel()) { <span>{{ s()?.nivelAlerta?.replace('_', ' ') ?? 'SIN REPORTES' }}</span> }
    </span>`,
  styles: [`
    .semaforo{display:inline-flex;align-items:center;gap:6px;font-size:11px;font-weight:700;color:var(--teal-dark)}
    .dot{width:12px;height:12px;border-radius:50%;display:inline-block;box-shadow:0 0 0 3px rgba(0,0,0,.06)}
  `],
})
export class SemaforoBadgeComponent {
  semaforo = input<Semaforo | null | undefined>(null);
  hex = input<string | null | undefined>(null);
  nivel = input<string | null | undefined>(null);
  showLabel = input(true);
  s = computed<Partial<Semaforo> | null>(() => this.semaforo() ?? (this.hex() ? { colorHex: this.hex()!, nivelAlerta: this.nivel() ?? '' } : null));
}
