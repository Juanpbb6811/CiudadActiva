import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { mensajeError } from '../../core/constants';
import { Reporte } from '../../core/models';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-operador-historial',
  standalone: true,
  imports: [DatePipe, TopBarComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  template: `
    <app-top-bar title="Historial" subtitle="Incidencias resueltas" />
    <div class="page">
      @if (error()) { <app-error-view [message]="error()" (retried)="cargar()" /> }
      @if (cargando()) { <app-loading [n]="3" /> }
      @else if (lista().length === 0) { <app-empty emoji="🗂️" title="Aún no hay incidencias cerradas" /> }
      @else {
        <div class="grid grid-auto">
          @for (r of lista(); track r.id) {
            <div class="card clickable" (click)="ver(r.id)">
              <div class="row between"><small class="kicker">#{{ r.id }} • {{ r.categoriaNombre }}</small><span class="chip" style="background:#dcedc8;color:#33691e;border-color:#aed581">RESUELTO</span></div>
              <h3 class="mt-sm">{{ r.titulo }}</h3>
              <p class="muted xs mt-sm">Cerrado: {{ r.fechaCierre | date: 'yyyy-MM-dd' }}</p>
            </div>
          }
        </div>
      }
    </div>`,
})
export class OperadorHistorialComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  lista = signal<Reporte[]>([]);
  cargando = signal(true);
  error = signal('');
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.api.listarReportes({ estado: 'CERRADO' }).subscribe({
      next: (l) => { this.lista.set(l ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  ver(id: number): void { this.router.navigate(['/operador/ficha', id]); }
}
