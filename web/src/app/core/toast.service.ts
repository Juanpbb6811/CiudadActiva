import { Injectable, signal } from '@angular/core';

export interface Toast { id: number; msg: string; tipo: 'ok' | 'error' | 'info'; }

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private n = 0;

  show(msg: string, tipo: Toast['tipo'] = 'info', ms = 4000): void {
    const id = ++this.n;
    this.toasts.update((t) => [...t, { id, msg, tipo }]);
    setTimeout(() => this.toasts.update((t) => t.filter((x) => x.id !== id)), ms);
  }
  ok(m: string) { this.show(m, 'ok'); }
  error(m: string) { this.show(m, 'error', 6000); }
}
