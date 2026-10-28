import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { mensajeError } from '../../core/constants';
import { Reporte } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { ToastService } from '../../core/toast.service';
import { IconComponent } from '../../shared/icon.component';
import { ModalComponent } from '../../shared/modal.component';
import { ReporteCardComponent } from '../../shared/reporte-card.component';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-mis-reportes',
  standalone: true,
  imports: [TopBarComponent, ReporteCardComponent, ModalComponent, IconComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  templateUrl: './mis-reportes.component.html',
})
export class MisReportesComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  session = inject(SessionService);

  tab = signal<'MIOS' | 'COMUNIDAD'>('MIOS');
  cargando = signal(true);
  error = signal('');
  mios = signal<Reporte[]>([]);
  comunidad = signal<Reporte[]>([]);
  lista = computed(() => (this.tab() === 'MIOS' ? this.mios() : this.comunidad()));
  aRetirar = signal<Reporte | null>(null);
  retirando = signal(false);

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    forkJoin({ mis: this.api.misReportes(), todos: this.api.listarReportes() }).subscribe({
      next: ({ mis, todos }) => { this.mios.set(mis ?? []); this.comunidad.set(todos ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }

  ver(id: number): void { this.router.navigate(['/ciudadano/seguimiento', id]); }
  nuevo(): void { this.router.navigate(['/ciudadano/nuevo-reporte/1']); }
  esMio(r: Reporte): boolean { return r.ciudadanoId === this.session.user()?.id; }

  confirmarRetiro(): void {
    const r = this.aRetirar();
    if (!r) return;
    this.retirando.set(true);
    this.api.eliminarReporte(r.id).subscribe({
      next: () => {
        this.retirando.set(false); this.aRetirar.set(null);
        this.toast.ok('Reporte retirado.');
        this.cargar();
      },
      error: (e) => {
        this.retirando.set(false); this.aRetirar.set(null);
        const m = mensajeError(e);
        this.toast.error(m.includes('PENDIENTE') ? 'Solo se pueden retirar reportes en estado PENDIENTE.' : 'No se pudo retirar el reporte.');
      },
    });
  }
}
