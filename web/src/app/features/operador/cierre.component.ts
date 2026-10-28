import { Component, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { switchMap } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { mensajeError } from '../../core/constants';
import { ToastService } from '../../core/toast.service';
import { IconComponent } from '../../shared/icon.component';
import { PhotoPickerComponent } from '../../shared/photo-picker.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-operador-cierre',
  standalone: true,
  imports: [FormsModule, TopBarComponent, PhotoPickerComponent, IconComponent],
  template: `
    <app-top-bar title="Cierre de Incidencia" [subtitle]="'Incidencia #' + id()" [back]="true" fallback="/operador/tareas" />
    <div class="page page-narrow">
      @if (regla3()) {
        <div class="card" style="border:2px solid #c62828">
          <h2 style="color:#c62828">⛔ Acción Bloqueada</h2>
          <p class="kicker mt-sm">VALIDACIÓN EN SERVIDOR (BusinessRuleException):</p>
          <p class="small mt-sm">{{ regla3Msg() }}</p>
          <p class="small mt-sm"><strong>✕ Avances del operador en Seguimiento: 0 registros</strong><br />Debes registrar primero tus avances de trabajo en terreno.</p>
          <div class="col gap mt">
            <button class="btn primary" type="button" (click)="irAvance()">Registrar Avance Primero →</button>
            <button class="btn ghost" type="button" (click)="volver()">Volver al Listado</button>
          </div>
        </div>
      } @else {
        <h2 class="section-title" style="margin-top:6px">Evidencia de solución obligatoria * (cámara móvil)</h2>
        <app-photo-picker [preview]="preview()" titulo="Toma la fotografía de la obra terminada" ayuda="Obligatoria para cerrar la incidencia" (fileChange)="setFoto($event)" />

        <label class="field mt-lg"><span>Detalle técnico de intervención *</span>
          <textarea rows="5" [(ngModel)]="obs" placeholder="Ej: Cambio total de luminaria y pruebas de voltaje conformes..."></textarea></label>
        @if (error()) { <div class="alert error">{{ error() }}</div> }
        <button class="btn primary block mt" type="button" [disabled]="guardando()" (click)="cerrar()"><app-icon name="check-circle" [size]="16" /> {{ guardando() ? 'Cerrando…' : 'Finalizar y Cerrar Incidencia' }}</button>
      }
    </div>`,
})
export class OperadorCierreComponent {
  id = input.required<string>();
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  obs = '';
  foto = signal<File | null>(null);
  preview = signal<string | null>(null);
  guardando = signal(false);
  error = signal('');
  regla3 = signal(false);
  regla3Msg = signal('');

  setFoto(f: File | null): void {
    const p = this.preview(); if (p) URL.revokeObjectURL(p);
    this.foto.set(f); this.preview.set(f ? URL.createObjectURL(f) : null);
  }

  /**
   * Cierre en dos pasos (el backend los separa):
   *  1) POST /foto-solucion  -> guarda la evidencia (Regla: evidencia visual obligatoria)
   *  2) PATCH /cerrar        -> valida la Regla 3 (al menos un avance operativo previo) y cierra.
   */
  cerrar(): void {
    this.error.set('');
    if (!this.foto()) { this.error.set('Debe adjuntar la fotografía tomada como evidencia de la solución en campo.'); return; }
    if (!this.obs.trim()) { this.error.set('Describe el detalle técnico de la intervención.'); return; }
    const id = +this.id();
    this.guardando.set(true);
    this.api.subirFotoSolucion(id, this.foto()!, this.obs.trim()).pipe(
      switchMap((r) => this.api.cerrarReporte(id, { evidenciaCierreUrl: r.evidenciaCierreUrl || `/api/reportes/${id}/foto-solucion`, observaciones: this.obs.trim() })),
    ).subscribe({
      next: () => { this.guardando.set(false); this.toast.ok('Incidencia cerrada con evidencia.'); this.router.navigate(['/operador/tareas']); },
      error: (e) => {
        this.guardando.set(false);
        const m = mensajeError(e);
        if (m.includes('al menos una acción de atención')) { this.regla3Msg.set(m); this.regla3.set(true); } else this.error.set(m);
      },
    });
  }
  irAvance(): void { this.router.navigate(['/operador/avance', this.id()]); }
  volver(): void { this.router.navigate(['/operador/tareas']); }
}
