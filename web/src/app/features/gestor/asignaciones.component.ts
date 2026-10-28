import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { mensajeError } from '../../core/constants';
import { Asignacion } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-gestor-asignaciones',
  standalone: true,
  imports: [DatePipe, TopBarComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  template: `
    <app-top-bar title="Asignaciones" subtitle="Operadores en terreno" />
    <div class="page">
      @if (error()) { <app-error-view [message]="error()" (retried)="cargar()" /> }
      @if (cargando()) { <app-loading [n]="3" /> }
      @else if (lista().length === 0) { <app-empty emoji="🧰" title="No hay asignaciones operativas en curso." /> }
      @else {
        <div class="grid grid-auto">
          @for (a of lista(); track a.id) {
            <div class="card">
              <div class="row between"><strong>Reporte #{{ a.reporteId }}</strong><span class="chip" style="background:#e3f2fd;color:#1565c0;border-color:#90caf9">EN TERRENO</span></div>
              <p class="small mt-sm">{{ a.reporteTitulo }}</p>
              <p class="small mt-sm">👷 Operador responsable: <strong>{{ a.operadorNombre ?? '—' }}</strong></p>
              @if (a.notas) { <p class="muted small">Instrucciones: {{ a.notas }}</p> }
              <p class="muted xs mt-sm">Asignado {{ a.creadoEn | date: 'dd MMM yyyy, HH:mm' }}</p>
              <div class="row gap-sm mt-sm">
                <button class="btn ghost btn-sm" type="button" (click)="ver(a.reporteId)">Inspeccionar</button>
                <button class="btn danger-ghost btn-sm" type="button" (click)="cancelar(a)">Cancelar asignación</button>
              </div>
            </div>
          }
        </div>
      }
    </div>`,
})
export class GestorAsignacionesComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  lista = signal<Asignacion[]>([]);
  cargando = signal(true);
  error = signal('');

  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.api.listarAsignacionesActivas().subscribe({
      next: (l) => { this.lista.set(l ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  ver(id: number): void { this.router.navigate(['/gestor/inspeccion', id]); }
  cancelar(a: Asignacion): void {
    this.api.cancelarAsignacion(a.reporteId).subscribe({
      next: () => { this.toast.ok('Asignación cancelada.'); this.cargar(); },
      error: (e) => this.toast.error(mensajeError(e)),
    });
  }
}
