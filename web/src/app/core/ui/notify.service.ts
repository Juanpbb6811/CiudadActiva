import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProblemDetail, aProblemDetail, mensajeProblema } from '../http/problem-detail';

@Injectable({ providedIn: 'root' })
export class NotifyService {
  private readonly snack = inject(MatSnackBar);

  ok(mensaje: string): void {
    this.snack.open(mensaje, 'Cerrar', { duration: 3500, panelClass: 'snack-ok' });
  }

  error(err: unknown): void {
    const p: ProblemDetail = aProblemDetail(err);
    this.snack.open(`${p.title}: ${mensajeProblema(p)}`, 'Cerrar', { duration: 7000, panelClass: 'snack-error' });
  }
}
