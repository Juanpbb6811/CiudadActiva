import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { labelEstado, mensajeError } from '../../core/constants';
import { Asignacion, EstadoReporte, Reporte } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { EstadoModalComponent } from '../../shared/estado-modal.component';
import { IconComponent } from '../../shared/icon.component';
import { ReporteCardComponent } from '../../shared/reporte-card.component';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-operador-tareas',
  standalone: true,
  imports: [TopBarComponent, ReporteCardComponent, EstadoModalComponent, IconComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  templateUrl: './tareas.component.html',
})
export class OperadorTareasComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  tab = signal<'TODOS' | 'MIS_TAREAS'>('TODOS');
  filtros = ['TODOS', 'ADMITIDO', 'EN_PROCESO', 'ATENDIDO'];
  filtro = signal('TODOS');
  tareas = signal<Asignacion[]>([]);
  reportes = signal<Reporte[]>([]);
  cargando = signal(true);
  error = signal('');
  editar = signal<Reporte | null>(null);
  guardando = signal(false);
  lbl = labelEstado;
  /** El operador solo transiciona entre estados de ejecución en terreno. */
  opciones: EstadoReporte[] = ['EN_PROCESO', 'ATENDIDO'];
  visibles = computed(() => this.reportes().filter((r) => r.estado !== 'CERRADO' && r.estado !== 'RECHAZADO' && r.estado !== 'PENDIENTE' && r.estado !== 'EN_REVISION'));

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    forkJoin({
      t: this.api.misAsignaciones(),
      r: this.api.listarReportes({ estado: this.filtro() === 'TODOS' ? undefined : this.filtro() }),
    }).subscribe({
      next: ({ t, r }) => { this.tareas.set(t ?? []); this.reportes.set(r ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set('Error al sincronizar tareas: ' + mensajeError(e)); this.cargando.set(false); },
    });
  }
  setFiltro(f: string): void { this.filtro.set(f); this.cargar(); }

  ficha(id: number): void { this.router.navigate(['/operador/ficha', id]); }
  avance(id: number): void { this.router.navigate(['/operador/avance', id]); }
  cierre(id: number): void { this.router.navigate(['/operador/cierre', id]); }

  guardarEstado(ev: { estado: EstadoReporte; observaciones: string }): void {
    const r = this.editar();
    if (!r) return;
    this.guardando.set(true);
    this.api.cambiarEstado(r.id, { estado: ev.estado, observaciones: ev.observaciones.trim() || 'Actualización operativa de estado en terreno' }).subscribe({
      next: () => { this.guardando.set(false); this.editar.set(null); this.toast.ok('Estado actualizado.'); this.cargar(); },
      error: (e) => { this.guardando.set(false); this.toast.error(mensajeError(e)); },
    });
  }
}
