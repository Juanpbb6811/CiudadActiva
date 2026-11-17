import { Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';

const KEY = 'ca.apiUrl';

/** URL base del backend, configurable en tiempo de ejecución (persistida en localStorage). */
@Injectable({ providedIn: 'root' })
export class ApiConfigService {
  readonly presets = environment.apiPresets;
  private readonly _baseUrl = signal<string>(this.leer());
  readonly baseUrl = this._baseUrl.asReadonly();

  setBaseUrl(url: string): void {
    const limpia = url.trim().replace(/\/+$/, '');
    this._baseUrl.set(limpia);
    try {
      localStorage.setItem(KEY, limpia);
    } catch {
      /* almacenamiento no disponible */
    }
  }

  url(path: string): string {
    return `${this._baseUrl()}${path.startsWith('/') ? path : '/' + path}`;
  }

  /** URL pública de la foto del reporte (endpoint permitAll en SecurityConfig). */
  fotoReporte(id: number): string {
    return this.url(`/api/reportes/${id}/foto`);
  }

  fotoSolucion(id: number): string {
    return this.url(`/api/reportes/${id}/foto-solucion`);
  }

  private leer(): string {
    try {
      return (localStorage.getItem(KEY) || environment.apiUrl).replace(/\/+$/, '');
    } catch {
      return environment.apiUrl;
    }
  }
}
