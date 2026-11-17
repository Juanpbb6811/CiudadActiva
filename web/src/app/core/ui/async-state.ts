import { DestroyRef, Signal, computed, inject, signal } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { ProblemDetail, aProblemDetail } from '../http/problem-detail';

export type Estado = 'loading' | 'error' | 'empty' | 'ready';

/**
 * Estado asíncrono reutilizable para todas las vistas: Loading / Empty / Error / Ready.
 * Uso: `readonly datos = asyncState(() => this.api.reportes(), (d) => d.length === 0);` y luego `datos.cargar()`.
 */
export interface AsyncState<T> {
  data: Signal<T | null>;
  error: Signal<ProblemDetail | null>;
  estado: Signal<Estado>;
  cargando: Signal<boolean>;
  cargar: () => void;
  set: (v: T) => void;
}

export function asyncState<T>(fuente: () => Observable<T>, esVacio: (d: T) => boolean = defaultVacio): AsyncState<T> {
  const destroyRef = inject(DestroyRef);
  const data = signal<T | null>(null);
  const error = signal<ProblemDetail | null>(null);
  const cargando = signal(false);
  let sub: Subscription | null = null;

  const cargar = () => {
    sub?.unsubscribe();
    cargando.set(true);
    error.set(null);
    sub = fuente().subscribe({
      next: (v) => {
        data.set(v);
        cargando.set(false);
      },
      error: (e) => {
        error.set(aProblemDetail(e));
        cargando.set(false);
      },
    });
  };
  destroyRef.onDestroy(() => sub?.unsubscribe());

  const estado = computed<Estado>(() => {
    if (cargando() && data() === null) return 'loading';
    if (error()) return 'error';
    const d = data();
    if (d === null) return 'loading';
    return esVacio(d) ? 'empty' : 'ready';
  });

  return { data: data.asReadonly(), error: error.asReadonly(), estado, cargando: cargando.asReadonly(), cargar, set: (v) => data.set(v) };
}

function defaultVacio(d: unknown): boolean {
  return Array.isArray(d) ? d.length === 0 : d == null;
}
