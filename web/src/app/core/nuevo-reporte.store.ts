import { Injectable, signal } from '@angular/core';
import { Categoria, Reporte } from './models';

/** Estado compartido del asistente de 3 pasos (equivale a las MutableStateFlow nuevoReporte* de CiudadanoViewModel). */
@Injectable({ providedIn: 'root' })
export class NuevoReporteStore {
  readonly categoria = signal<Categoria | null>(null);
  readonly titulo = signal('');
  readonly descripcion = signal('');
  readonly direccion = signal('');
  readonly latitud = signal(4.609712);
  readonly longitud = signal(-74.08175);
  readonly foto = signal<File | null>(null);
  readonly fotoPreview = signal<string | null>(null);
  readonly creado = signal<Reporte | null>(null);

  setFoto(file: File | null): void {
    const prev = this.fotoPreview();
    if (prev) URL.revokeObjectURL(prev);
    this.foto.set(file);
    this.fotoPreview.set(file ? URL.createObjectURL(file) : null);
  }

  reset(keepCreated = false): void {
    this.titulo.set('');
    this.descripcion.set('');
    this.direccion.set('');
    this.setFoto(null);
    if (!keepCreated) this.creado.set(null);
  }
}
