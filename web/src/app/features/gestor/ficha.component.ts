import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiConfigService } from '../../core/api-config.service';
import { ApiService } from '../../core/api.service';
import { PRIORIDADES, labelEstado, mensajeError, prioridadStyle } from '../../core/constants';
import { EstadoReporte, Prioridad, ReporteDetalle, Usuario } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { SemaforoBadgeComponent, StatusChipComponent } from '../../shared/chips.component';
import { EstadoModalComponent } from '../../shared/estado-modal.component';
import { MapViewComponent } from '../../shared/map-view.component';
import { ModalComponent } from '../../shared/modal.component';
import { ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-gestor-ficha',
  standalone: true,
  imports: [DatePipe, FormsModule, TopBarComponent, StatusChipComponent, SemaforoBadgeComponent, ModalComponent, EstadoModalComponent, MapViewComponent, LoadingComponent, ErrorViewComponent],
  templateUrl: './ficha.component.html',
})
export class GestorFichaComponent implements OnInit {
  id = input.required<string>();
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  cfg = inject(ApiConfigService);

  rep = signal<ReporteDetalle | null>(null);
  cargando = signal(true);
  error = signal('');
  trabajando = signal(false);
  prioridad = signal<Prioridad>('MEDIA');
  prioridades = PRIORIDADES;
  pst = prioridadStyle;
  lbl = labelEstado;

  modal = signal<'NINGUNO' | 'RECHAZO' | 'ASIGNAR' | 'ESTADO'>('NINGUNO');
  justificacion = '';
  errorRechazo = signal('');
  operadores = signal<Usuario[]>([]);
  operadorSel = signal<number | null>(null);
  notas = '';

  /** Un reporte aún no validado se puede admitir o rechazar; después solo se prioriza/asigna/cambia de estado. */
  porValidar = computed(() => { const e = this.rep()?.estado; return e === 'PENDIENTE' || e === 'EN_REVISION'; });
  markers = computed(() => { const r = this.rep(); return r?.latitud != null && r?.longitud != null ? [{ id: r.id, lat: r.latitud, lng: r.longitud, color: r.semaforoColorHex || '#4CAF50' }] : []; });

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.api.detalleReporte(+this.id()).subscribe({
      next: (r) => { this.rep.set(r); this.prioridad.set(r.prioridad ?? 'MEDIA'); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }

  private fin(msg: string): void { this.trabajando.set(false); this.modal.set('NINGUNO'); this.toast.ok(msg); this.cargar(); }
  private falla(e: unknown): void { this.trabajando.set(false); this.toast.error(mensajeError(e)); }

  // Regla 1: el rechazo exige justificación
  rechazar(): void {
    if (!this.justificacion.trim()) { this.errorRechazo.set('Regla de Negocio 1: La justificación técnica de rechazo es obligatoria.'); return; }
    this.trabajando.set(true);
    this.api.validarReporte(+this.id(), { admitido: false, justificacion: this.justificacion.trim() }).subscribe({
      next: () => { this.trabajando.set(false); this.toast.ok('Reporte rechazado.'); this.router.navigate(['/gestor/validacion']); },
      error: (e) => this.falla(e),
    });
  }

  // Regla 2: solo el gestor fija la prioridad oficial
  admitir(): void {
    this.trabajando.set(true);
    this.api.validarReporte(+this.id(), { admitido: true, prioridad: this.prioridad() }).subscribe({
      next: () => { this.trabajando.set(false); this.toast.ok('Reporte admitido. Ahora puedes asignar un operador.'); this.cargar(); this.abrirAsignar(); },
      error: (e) => this.falla(e),
    });
  }

  guardarPrioridad(): void {
    this.trabajando.set(true);
    this.api.cambiarPrioridad(+this.id(), { prioridad: this.prioridad() }).subscribe({ next: () => this.fin('Prioridad actualizada.'), error: (e) => this.falla(e) });
  }

  // Regla 5: asignación de operador en campo
  abrirAsignar(): void {
    this.modal.set('ASIGNAR');
    this.operadorSel.set(null);
    this.api.listarOperadores().subscribe({ next: (o) => this.operadores.set(o ?? []), error: (e) => this.toast.error(mensajeError(e)) });
  }
  asignar(): void {
    const op = this.operadorSel();
    if (op == null) return;
    this.trabajando.set(true);
    this.api.asignarOperador(+this.id(), { operadorId: op, notas: this.notas.trim() || null }).subscribe({
      next: () => { this.notas = ''; this.fin('Operador asignado correctamente.'); }, error: (e) => this.falla(e),
    });
  }
  cancelarAsignacion(): void {
    this.trabajando.set(true);
    this.api.cancelarAsignacion(+this.id()).subscribe({ next: () => this.fin('Asignación cancelada.'), error: (e) => this.falla(e) });
  }

  cambiarEstado(ev: { estado: EstadoReporte; observaciones: string }): void {
    this.trabajando.set(true);
    this.api.cambiarEstado(+this.id(), { estado: ev.estado, observaciones: ev.observaciones }).subscribe({ next: () => this.fin('Estado actualizado.'), error: (e) => this.falla(e) });
  }
}
