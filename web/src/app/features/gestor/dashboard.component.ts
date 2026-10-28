import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { PRIORIDADES, mensajeError } from '../../core/constants';
import { Asignacion, Estadisticas, Reporte } from '../../core/models';
import { PrioridadChipComponent } from '../../shared/chips.component';
import { IconComponent } from '../../shared/icon.component';
import { ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-gestor-dashboard',
  standalone: true,
  imports: [RouterLink, TopBarComponent, IconComponent, PrioridadChipComponent, LoadingComponent, ErrorViewComponent],
  templateUrl: './dashboard.component.html',
})
export class GestorDashboardComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  cargando = signal(true);
  error = signal('');
  pendientes = signal<Reporte[]>([]);
  asignaciones = signal<Asignacion[]>([]);
  stats = signal<Estadisticas | null>(null);

  admitidos = computed(() => this.stats()?.reportesPorEstado?.['ADMITIDO'] ?? 0);
  critico = computed(() => [...this.pendientes()].sort((a, b) => PRIORIDADES.indexOf(b.prioridad ?? 'BAJA') - PRIORIDADES.indexOf(a.prioridad ?? 'BAJA'))[0] ?? null);

  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    forkJoin({ p: this.api.listarReportes({ estado: 'PENDIENTE' }), a: this.api.listarAsignacionesActivas(), s: this.api.estadisticas() }).subscribe({
      next: ({ p, a, s }) => { this.pendientes.set(p ?? []); this.asignaciones.set(a ?? []); this.stats.set(s); this.cargando.set(false); },
      error: (e) => { this.error.set('Error al sincronizar bandeja de gestor: ' + mensajeError(e)); this.cargando.set(false); },
    });
  }
  inspeccionar(id: number): void { this.router.navigate(['/gestor/inspeccion', id]); }
}
