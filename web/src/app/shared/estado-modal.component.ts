import { Component, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ESTADOS } from '../core/constants';
import { EstadoReporte } from '../core/models';
import { ModalComponent } from './modal.component';
import { IconComponent } from './icon.component';
import { estadoStyle, labelEstado } from '../core/constants';

/**
 * Modal "Actualizar Estado" reutilizado por Gestor, Operador y Admin
 * (EstadoSelectorItem + botón "Guardar Estado" de la app Android).
 */
@Component({
  selector: 'app-estado-modal',
  standalone: true,
  imports: [FormsModule, ModalComponent, IconComponent],
  template: `
    <app-modal [title]="titulo()" (closed)="cancelar.emit()">
      <p class="muted">Selecciona el nuevo estado oficial:</p>
      <div class="estado-grid">
        @for (e of opciones(); track e) {
          <button type="button" class="estado-opt" [class.sel]="sel() === e"
            [style.background]="sel() === e ? 'var(--teal-dark)' : st(e).bg"
            [style.color]="sel() === e ? '#fff' : st(e).text"
            [style.border-color]="sel() === e ? 'var(--teal-dark)' : st(e).border"
            (click)="sel.set(e)">
            <i [style.background]="sel() === e ? '#fff' : st(e).text"></i>{{ lbl(e) }}
          </button>
        }
      </div>
      <label class="field">
        <span>{{ etiquetaObs() }}</span>
        <textarea rows="3" [(ngModel)]="obs" [placeholder]="placeholder()"></textarea>
      </label>
      <ng-container footer>
        <button class="btn ghost" type="button" (click)="cancelar.emit()">Cancelar</button>
        <button class="btn primary" type="button" [disabled]="guardando()" (click)="guardar.emit({ estado: sel(), observaciones: obs })">
          <app-icon name="save" [size]="16" /> Guardar Estado
        </button>
      </ng-container>
    </app-modal>`,
})
export class EstadoModalComponent {
  titulo = input('Actualizar Estado');
  actual = input<EstadoReporte>('PENDIENTE');
  opciones = input<EstadoReporte[]>(ESTADOS);
  etiquetaObs = input('Observaciones / Justificación:');
  placeholder = input('Ej: Aprobado para cuadrilla técnica...');
  guardando = input(false);
  guardar = output<{ estado: EstadoReporte; observaciones: string }>();
  cancelar = output<void>();

  sel = signal<EstadoReporte>('PENDIENTE');
  obs = '';
  st = estadoStyle;
  lbl = labelEstado;
  ngOnInit() { this.sel.set(this.actual()); }
}
