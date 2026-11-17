import { Injectable, computed, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly pendientes = signal(0);
  readonly activo = computed(() => this.pendientes() > 0);

  inicio(): void {
    this.pendientes.update((n) => n + 1);
  }

  fin(): void {
    this.pendientes.update((n) => Math.max(0, n - 1));
  }
}
