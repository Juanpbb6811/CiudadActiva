import { Component, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, switchMap, of } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { mensajeError } from '../../core/constants';
import { EstadoReporte } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { IconComponent } from '../../shared/icon.component';
import { PhotoPickerComponent } from '../../shared/photo-picker.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-operador-avance',
  standalone: true,
  imports: [FormsModule, TopBarComponent, PhotoPickerComponent, IconComponent],
  template: `
    <app-top-bar title="Registrar Avance" [subtitle]="'Incidencia #' + id() + ' • Registro de campo'" [back]="true" fallback="/operador/tareas" />
    <div class="page page-narrow">
      <label class="field"><span>Observaciones técnicas del avance *</span>
        <textarea rows="5" [(ngModel)]="obs" placeholder="Ej: Se inspeccionó el cableado y se reemplazó balastro defectuoso..."></textarea>
        @if (intento() && !obs.trim()) { <small class="field-error">Debe especificar las observaciones técnicas del avance.</small> }
      </label>

      <h2 class="section-title" style="margin-top:6px">Fotografía de soporte en terreno</h2>
      <app-photo-picker [preview]="preview()" titulo="Tocar para tomar fotografía con la cámara" ayuda="Adjunta foto del trabajo realizado en sitio" (fileChange)="setFoto($event)" />

      <h2 class="section-title">Estado del reporte tras este avance</h2>
      <div class="row gap">
        <button type="button" class="btn grow" [class.primary]="nuevo() === 'EN_PROCESO'" [class.ghost]="nuevo() !== 'EN_PROCESO'" (click)="nuevo.set('EN_PROCESO')">En Proceso</button>
        <button type="button" class="btn grow" [class.primary]="nuevo() === 'ATENDIDO'" [class.ghost]="nuevo() !== 'ATENDIDO'" (click)="nuevo.set('ATENDIDO')">Atendido</button>
      </div>

      <button class="btn primary block mt-lg" type="button" [disabled]="guardando()" (click)="guardar()"><app-icon name="save" [size]="16" /> {{ guardando() ? 'Guardando…' : 'Guardar Avance en Trazabilidad' }}</button>
    </div>`,
})
export class OperadorAvanceComponent {
  id = input.required<string>();
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  obs = '';
  nuevo = signal<EstadoReporte>('EN_PROCESO');
  foto = signal<File | null>(null);
  preview = signal<string | null>(null);
  intento = signal(false);
  guardando = signal(false);

  setFoto(f: File | null): void {
    const p = this.preview(); if (p) URL.revokeObjectURL(p);
    this.foto.set(f); this.preview.set(f ? URL.createObjectURL(f) : null);
  }

  guardar(): void {
    this.intento.set(true);
    if (!this.obs.trim()) return;
    const id = +this.id();
    this.guardando.set(true);
    const subir$: Observable<unknown> = this.foto() ? this.api.subirFotoSolucion(id, this.foto()!, this.obs.trim()) : of(null);
    subir$.pipe(switchMap(() => this.api.registrarAvance(id, { nuevoEstado: this.nuevo(), accion: 'AVANCE_OPERATIVO', observaciones: this.obs.trim() }))).subscribe({
      next: () => { this.guardando.set(false); this.toast.ok('Avance registrado en la trazabilidad.'); this.router.navigate(['/operador/tareas']); },
      error: (e) => { this.guardando.set(false); this.toast.error(mensajeError(e)); },
    });
  }
}
