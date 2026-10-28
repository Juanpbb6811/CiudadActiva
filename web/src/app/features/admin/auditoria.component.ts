import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { estadoStyle, labelEstado, mensajeError } from '../../core/constants';
import { Seguimiento } from '../../core/models';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-admin-auditoria',
  standalone: true,
  imports: [DatePipe, TopBarComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  template: `
    <app-top-bar title="Auditoría" subtitle="Matriz de trazabilidad inmutable (RNF04)" />
    <div class="page">
      @if (error()) { <app-error-view [message]="error()" (retried)="cargar()" /> }
      @if (cargando()) { <app-loading [n]="4" /> }
      @else if (lista().length === 0) { <app-empty emoji="🛡️" title="No se registran eventos de auditoría." /> }
      @else {
        <div class="grid grid-auto">
          @for (s of lista(); track s.id) {
            <div class="card">
              <div class="row between wrap gap-sm">
                <span class="chip" [style.background]="st(s.estadoNuevo ?? s.accion).bg" [style.color]="st(s.estadoNuevo ?? s.accion).text" [style.border-color]="st(s.estadoNuevo ?? s.accion).border">
                  TRANSICIÓN: {{ lbl(s.estadoAnterior ?? '—') }} → {{ lbl(s.estadoNuevo ?? s.accion) }}</span>
                <small class="muted">{{ s.creadoEn | date: 'dd MMM yyyy, HH:mm' }}</small>
              </div>
              <p class="small mt-sm"><strong>Reporte #{{ s.reporteId }}</strong> • Usuario: {{ s.usuarioEmail ?? s.usuarioNombre ?? 'Sistema' }}</p>
              @if (s.observaciones) { <p class="muted small">Nota: {{ s.observaciones }}</p> }
              <p class="hash mt-sm">Hash: {{ s.hashTrazabilidad ?? '—' }}</p>
            </div>
          }
        </div>
      }
    </div>`,
})
export class AdminAuditoriaComponent implements OnInit {
  private api = inject(ApiService);
  lista = signal<Seguimiento[]>([]);
  cargando = signal(true);
  error = signal('');
  st = estadoStyle; lbl = labelEstado;
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.api.auditoria().subscribe({
      next: (l) => { this.lista.set(l ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set('Error al cargar matriz de auditoría: ' + mensajeError(e)); this.cargando.set(false); },
    });
  }
}
