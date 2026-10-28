import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { mensajeError } from '../../core/constants';
import { NuevoReporteStore } from '../../core/nuevo-reporte.store';
import { IconComponent } from '../../shared/icon.component';
import { PhotoPickerComponent } from '../../shared/photo-picker.component';
import { TopBarComponent } from '../../shared/top-bar.component';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-nuevo-reporte-paso3',
  standalone: true,
  imports: [TopBarComponent, PhotoPickerComponent, IconComponent],
  templateUrl: './nuevo-reporte-paso3.component.html',
})
export class NuevoReportePaso3Component {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  store = inject(NuevoReporteStore);
  enviando = signal(false);
  error = signal('');

  constructor() {
    // Si se recarga la página se pierde el estado del asistente: volver al paso 1.
    if (!this.store.categoria() || !this.store.titulo()) this.router.navigate(['/ciudadano/nuevo-reporte/1']);
  }

  publicar(): void {
    const cat = this.store.categoria();
    if (!cat) return;
    const base = {
      titulo: this.store.titulo().trim(),
      descripcion: this.store.descripcion().trim(),
      categoriaId: cat.id,
      latitud: this.store.latitud(),
      longitud: this.store.longitud(),
      direccion: this.store.direccion().trim(),
    };
    const foto = this.store.foto();
    this.enviando.set(true);
    this.error.set('');
    const req$ = foto ? this.api.crearReporteConFoto(base, foto) : this.api.crearReporte(base);
    req$.subscribe({
      next: (rep) => {
        this.store.creado.set(rep);
        this.store.reset(true);
        this.enviando.set(false);
        this.router.navigate(['/ciudadano/nuevo-reporte/exito'], { replaceUrl: true });
      },
      error: (e) => { this.enviando.set(false); this.error.set('No se pudo registrar la incidencia: ' + mensajeError(e)); this.toast.error(this.error()); },
    });
  }
  atras(): void { this.router.navigate(['/ciudadano/nuevo-reporte/2']); }
}
