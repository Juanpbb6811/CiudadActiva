import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { DecimalPipe } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { ESTADOS, estadoStyle, labelEstado, mensajeError } from '../../core/constants';
import { EstadoReporte, Estadisticas, Reporte } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { SemaforoBadgeComponent } from '../../shared/chips.component';
import { EstadoModalComponent } from '../../shared/estado-modal.component';
import { IconComponent } from '../../shared/icon.component';
import { ModalComponent } from '../../shared/modal.component';
import { ReporteCardComponent } from '../../shared/reporte-card.component';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [DecimalPipe, TopBarComponent, SemaforoBadgeComponent, EstadoModalComponent, ModalComponent, ReporteCardComponent, IconComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  templateUrl: './panel.component.html',
})
export class AdminPanelComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  stats = signal<Estadisticas | null>(null);
  reportes = signal<Reporte[]>([]);
  filtro = signal('TODOS');
  filtros = ['TODOS', ...ESTADOS];
  cargando = signal(true);
  error = signal('');
  editar = signal<Reporte | null>(null);
  eliminar = signal<Reporte | null>(null);
  guardando = signal(false);
  est = estadoStyle; lbl = labelEstado; estados = ESTADOS;

  total = computed(() => this.stats()?.totalReportes ?? this.reportes().length);
  resueltos = computed(() => { const t = this.total(); return t ? Math.round(((this.stats()?.reportesAtendidosOCerrados ?? 0) / t) * 100) : 0; });
  categorias = computed(() => Object.entries(this.stats()?.semaforoPorCategoria ?? {}));
  pct = (v: number | undefined) => { const t = this.total(); return t ? Math.round(((v ?? 0) / t) * 100) : 0; };

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    forkJoin({ s: this.api.estadisticas(), r: this.api.listarReportes({ estado: this.filtro() === 'TODOS' ? undefined : this.filtro() }) }).subscribe({
      next: ({ s, r }) => { this.stats.set(s); this.reportes.set(r ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  setFiltro(f: string): void { this.filtro.set(f); this.cargar(); }
  inspeccionar(id: number): void { this.router.navigate(['/gestor/inspeccion', id]); }

  guardarEstado(ev: { estado: EstadoReporte; observaciones: string }): void {
    const r = this.editar(); if (!r) return;
    this.guardando.set(true);
    this.api.cambiarEstado(r.id, { estado: ev.estado, observaciones: ev.observaciones.trim() || 'Actualización de estado realizada por el Administrador Municipal' }).subscribe({
      next: () => { this.guardando.set(false); this.editar.set(null); this.toast.ok('Estado actualizado.'); this.cargar(); },
      error: (e) => { this.guardando.set(false); this.toast.error('Error al actualizar estado: ' + mensajeError(e)); },
    });
  }
  confirmarEliminar(): void {
    const r = this.eliminar(); if (!r) return;
    this.guardando.set(true);
    this.api.eliminarReporte(r.id).subscribe({
      next: () => { this.guardando.set(false); this.eliminar.set(null); this.toast.ok('Reporte eliminado.'); this.cargar(); },
      error: (e) => { this.guardando.set(false); this.eliminar.set(null); this.toast.error('Error al eliminar reporte: ' + mensajeError(e)); },
    });
  }
}
