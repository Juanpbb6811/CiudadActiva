import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { categoriaEmoji, mensajeError } from '../../core/constants';
import { Categoria } from '../../core/models';
import { NuevoReporteStore } from '../../core/nuevo-reporte.store';
import { ErrorViewComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-nuevo-reporte-paso1',
  standalone: true,
  imports: [FormsModule, TopBarComponent, ErrorViewComponent],
  templateUrl: './nuevo-reporte-paso1.component.html',
})
export class NuevoReportePaso1Component implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  store = inject(NuevoReporteStore);

  categorias = signal<Categoria[]>([]);
  error = signal('');
  intento = signal(false);
  emoji = categoriaEmoji;
  valido = computed(() => !!this.store.categoria() && this.store.titulo().trim().length >= 5 && this.store.descripcion().trim().length >= 10);

  ngOnInit(): void {
    this.api.listarCategorias().subscribe({
      next: (c) => {
        this.categorias.set(c ?? []);
        if (!this.store.categoria() && c?.length) this.store.categoria.set(c[0]);
      },
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  siguiente(): void {
    this.intento.set(true);
    if (this.valido()) this.router.navigate(['/ciudadano/nuevo-reporte/2']);
  }
}
