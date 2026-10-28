import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { labelEstado, mensajeError } from '../../core/constants';
import { EstadoReporte, Reporte } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { EstadoModalComponent } from '../../shared/estado-modal.component';
import { ReporteCardComponent } from '../../shared/reporte-card.component';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-gestor-bandeja',
  standalone: true,
  imports: [TopBarComponent, ReporteCardComponent, EstadoModalComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  templateUrl: './bandeja.component.html',
})
export class GestorBandejaComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  filtros = ['PENDIENTE', 'EN_REVISION', 'ADMITIDO', 'EN_PROCESO', 'ATENDIDO', 'RECHAZADO', 'CERRADO', 'TODOS'];
  filtro = signal('PENDIENTE');
  lista = signal<Reporte[]>([]);
  cargando = signal(true);
  error = signal('');
  editar = signal<Reporte | null>(null);
  guardando = signal(false);
  lbl = labelEstado;

  ngOnInit(): void { this.cargar(); }
  setFiltro(f: string): void { this.filtro.set(f); this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.api.listarReportes({ estado: this.filtro() === 'TODOS' ? undefined : this.filtro() }).subscribe({
      next: (l) => { this.lista.set(l ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  inspeccionar(id: number): void { this.router.navigate(['/gestor/inspeccion', id]); }

  guardarEstado(ev: { estado: EstadoReporte; observaciones: string }): void {
    const r = this.editar();
    if (!r) return;
    this.guardando.set(true);
    this.api.cambiarEstado(r.id, { estado: ev.estado, observaciones: ev.observaciones }).subscribe({
      next: () => { this.guardando.set(false); this.editar.set(null); this.toast.ok(`Reporte #${r.id} actualizado a ${labelEstado(ev.estado)}.`); this.cargar(); },
      error: (e) => { this.guardando.set(false); this.toast.error(mensajeError(e)); },
    });
  }
}
