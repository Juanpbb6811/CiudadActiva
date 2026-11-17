import { DOCUMENT } from '@angular/common';
import { Injectable, effect, inject, signal } from '@angular/core';

export type Tema = 'light' | 'dark';
const KEY = 'ca.tema';

/** Interruptor Modo Claro / Modo Oscuro (clase `dark` en <html> + color-scheme). */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly doc = inject(DOCUMENT);
  readonly tema = signal<Tema>(this.inicial());

  constructor() {
    effect(() => {
      const t = this.tema();
      const html = this.doc.documentElement;
      html.classList.toggle('dark', t === 'dark');
      html.style.colorScheme = t;
      try {
        localStorage.setItem(KEY, t);
      } catch {
        /* noop */
      }
    });
  }

  alternar(): void {
    this.tema.update((t) => (t === 'dark' ? 'light' : 'dark'));
  }

  private inicial(): Tema {
    try {
      const guardado = localStorage.getItem(KEY) as Tema | null;
      if (guardado === 'light' || guardado === 'dark') return guardado;
    } catch {
      /* noop */
    }
    return typeof matchMedia !== 'undefined' && matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
}
