import { Injectable, signal } from '@angular/core';
import { environment } from '../../environments/environment';

const KEY = 'ca_base_url';

/** Equivale a SessionManager.getBaseUrl()/setBaseUrl() de la app Android. */
@Injectable({ providedIn: 'root' })
export class ApiConfigService {
  readonly baseUrl = signal<string>(this.clean(localStorage.getItem(KEY) || environment.apiUrl));

  set(url: string): void {
    const c = this.clean(url);
    this.baseUrl.set(c);
    localStorage.setItem(KEY, c);
  }

  reset(): void {
    localStorage.removeItem(KEY);
    this.baseUrl.set(this.clean(environment.apiUrl));
  }

  /** URL absoluta de la foto original del reporte (GET público). */
  fotoReporte(id: number): string { return `${this.baseUrl()}/api/reportes/${id}/foto`; }
  fotoSolucion(id: number): string { return `${this.baseUrl()}/api/reportes/${id}/foto-solucion`; }

  private clean(u: string): string { return u.trim().replace(/\/+$/, ''); }
}
