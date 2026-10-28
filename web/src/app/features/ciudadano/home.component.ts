import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { categoriaEmoji, mensajeError } from '../../core/constants';
import { Categoria, Reporte } from '../../core/models';
import { NuevoReporteStore } from '../../core/nuevo-reporte.store';
import { SessionService } from '../../core/session.service';
import { SemaforoBadgeComponent, StatusChipComponent } from '../../shared/chips.component';
import { IconComponent } from '../../shared/icon.component';
import { ReporteCardComponent } from '../../shared/reporte-card.component';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';

@Component({
  selector: 'app-ciudadano-home',
  standalone: true,
  imports: [DatePipe, RouterLink, IconComponent, SemaforoBadgeComponent, StatusChipComponent, ReporteCardComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  templateUrl: './home.component.html',
})
export class CiudadanoHomeComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private store = inject(NuevoReporteStore);
  session = inject(SessionService);

  cargando = signal(true);
  error = signal('');
  categorias = signal<Categoria[]>([]);
  misReportes = signal<Reporte[]>([]);
  comunidad = signal<Reporte[]>([]);
  emoji = categoriaEmoji;

  activo = computed(() => this.misReportes().find((r) => r.estado !== 'CERRADO' && r.estado !== 'RECHAZADO') ?? null);
  /** Paso alcanzado del reporte activo: 1 Registrado · 2 En atención · 3 Cerrado */
  pasoActivo = computed(() => {
    const e = this.activo()?.estado;
    if (!e) return 0;
    return e === 'PENDIENTE' || e === 'EN_REVISION' ? 1 : 2;
  });
  muro = computed(() => this.comunidad().slice(0, 6));
  nombre = computed(() => (this.session.user()?.nombre ?? '').split(' ')[0]);

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    forkJoin({
      cats: this.api.listarCategorias(),
      mis: this.api.misReportes().pipe(catchError(() => of([] as Reporte[]))),
      todos: this.api.listarReportes().pipe(catchError(() => of([] as Reporte[]))),
    }).subscribe({
      next: ({ cats, mis, todos }) => {
        this.categorias.set(cats ?? []);
        this.misReportes.set(mis ?? []);
        this.comunidad.set(todos ?? []);
        this.cargando.set(false);
      },
      error: (e) => { this.error.set('Error al sincronizar datos: ' + mensajeError(e)); this.cargando.set(false); },
    });
  }

  reportarEn(cat: Categoria | null): void {
    if (cat) this.store.categoria.set(cat);
    this.router.navigate(['/ciudadano/nuevo-reporte/1']);
  }
  ver(id: number): void { this.router.navigate(['/ciudadano/seguimiento', id]); }
}
