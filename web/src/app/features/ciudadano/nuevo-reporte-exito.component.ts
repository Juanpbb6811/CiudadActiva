import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { NuevoReporteStore } from '../../core/nuevo-reporte.store';
import { PrioridadChipComponent } from '../../shared/chips.component';

@Component({
  selector: 'app-nuevo-reporte-exito',
  standalone: true,
  imports: [PrioridadChipComponent],
  template: `
    <div class="page page-narrow center" style="padding-top:48px">
      <div style="font-size:64px">✅</div>
      <h1 class="mt-sm">¡Incidencia Radicada!</h1>
      <p class="mt-sm"><strong>Radicado Oficial N.° {{ store.creado()?.id ?? '—' }}</strong></p>
      <p class="mt-sm"><span class="kicker">PENDIENTE DE VALIDACIÓN</span>
        @if (store.creado()?.prioridad) { · <app-prioridad-chip [prioridad]="store.creado()?.prioridad" /> }</p>
      <p class="muted small mt">Tu reporte ha sido registrado en la matriz inmutable de trazabilidad (RNF04). El sistema calculó su urgencia según los reportes del sector y un gestor validará su pertinencia técnica.</p>
      <div class="col gap mt-lg">
        @if (store.creado()) { <button class="btn primary" type="button" (click)="ver()">Ver Línea de Tiempo (RF07)</button> }
        <button class="btn ghost" type="button" (click)="inicio()">Volver al Menú Principal</button>
      </div>
    </div>`,
})
export class NuevoReporteExitoComponent {
  store = inject(NuevoReporteStore);
  private router = inject(Router);
  ver(): void { const id = this.store.creado()!.id; this.router.navigate(['/ciudadano/seguimiento', id], { replaceUrl: true }); }
  inicio(): void { this.router.navigate(['/ciudadano/inicio'], { replaceUrl: true }); }
}
