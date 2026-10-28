import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { mensajeError } from '../../core/constants';
import { Categoria, Reporte } from '../../core/models';
import { StatusChipComponent } from '../../shared/chips.component';
import { IconComponent } from '../../shared/icon.component';
import { MapMarker, MapViewComponent } from '../../shared/map-view.component';
import { ErrorViewComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-mapa-explorador',
  standalone: true,
  imports: [FormsModule, TopBarComponent, MapViewComponent, StatusChipComponent, IconComponent, ErrorViewComponent],
  templateUrl: './mapa.component.html',
})
export class MapaExploradorComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);

  reportes = signal<Reporte[]>([]);
  categorias = signal<Categoria[]>([]);
  busqueda = signal('');
  catSel = signal<number | null>(null);
  seleccionado = signal<Reporte | null>(null);
  error = signal('');
  cargando = signal(true);

  filtrados = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    const c = this.catSel();
    return this.reportes().filter((r) =>
      r.latitud != null && r.longitud != null &&
      (c == null || r.categoriaId === c) &&
      (!q || (r.direccion ?? '').toLowerCase().includes(q) || r.titulo.toLowerCase().includes(q) || (r.categoriaNombre ?? '').toLowerCase().includes(q)));
  });
  markers = computed<MapMarker[]>(() => this.filtrados().map((r) => ({ id: r.id, lat: r.latitud!, lng: r.longitud!, color: r.semaforoColorHex || '#4CAF50', label: r.titulo })));
  conteo = (id: number) => this.reportes().filter((r) => r.categoriaId === id).length;

  ngOnInit(): void {
    forkJoin({ reps: this.api.listarReportes(), cats: this.api.listarCategorias() }).subscribe({
      next: ({ reps, cats }) => { this.reportes.set(reps ?? []); this.categorias.set(cats ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }

  onMarker(id: number): void { this.seleccionado.set(this.reportes().find((r) => r.id === id) ?? null); }
  detalle(id: number): void { this.router.navigate(['/ciudadano/seguimiento', id]); }
}
