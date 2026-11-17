import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { Observable, map } from 'rxjs';
import { CiudadActivaApi } from '../../core/api/ciudad-activa.api';
import { PRIORIDAD_LABEL } from '../../core/api/labels';
import { Asignacion, PRIORIDADES, PrioridadReporte, Reporte, Usuario } from '../../core/api/models';
import { asyncState } from '../../core/ui/async-state';
import { NotifyService } from '../../core/ui/notify.service';
import { ProblemDetail, aProblemDetail } from '../../core/http/problem-detail';
import { ReporteDetalleComponent } from '../../shared/reporte-detalle.component';
import { EstadoBadgeComponent, PrioridadBadgeComponent, ViewStateComponent } from '../../shared/ui.components';

/* =====================================================================
   MODAL VALIDAR (PATCH /api/reportes/{id}/validar) · RF04
   - Admitir: prioridad oficial OBLIGATORIA (RN02)
   - Rechazar: justificación OBLIGATORIA (RN01 / CP05)
   - Solo PENDIENTE / EN_REVISION (RN04, también validado en backend)
   ===================================================================== */
@Component({
  selector: 'app-validar-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatRadioModule, MatSelectModule, MatFormFieldModule, MatInputModule, EstadoBadgeComponent],
  template: `
    <h2 mat-dialog-title>Validar reporte</h2>
    <mat-dialog-content>
      <div class="ref">
        <span class="mono">{{ r.codigoSeguimiento }}</span>
        <ca-estado-badge [estado]="r.estado" />
      </div>
      <h3 class="ref__t">{{ r.titulo }}</h3>
      <p class="muted ref__d">{{ r.categoriaNombre }} · {{ r.direccion || 'Sin dirección' }}</p>

      <form [formGroup]="form" class="vform">
        <mat-radio-group formControlName="decision" class="dec" aria-label="Decisión">
          <mat-radio-button value="admitir"><strong>Admitir</strong> · pasa a ADMITIDO y queda listo para asignar</mat-radio-button>
          <mat-radio-button value="rechazar"><strong>Rechazar</strong> · pasa a RECHAZADO (visible para el ciudadano)</mat-radio-button>
        </mat-radio-group>

        @if (decision() === 'admitir') {
          <mat-form-field class="full">
            <mat-label>Prioridad oficial (RN02)</mat-label>
            <mat-select formControlName="prioridad" required>
              @for (p of prioridades; track p) {
                <mat-option [value]="p">{{ etiquetaPrioridad(p) }}</mat-option>
              }
            </mat-select>
            @if (r.analisisIaPrioridadSugerida) {
              <mat-hint>Sugerencia automática: {{ r.analisisIaPrioridadSugerida }} (no es oficial)</mat-hint>
            }
            <mat-error>Debes asignar una prioridad oficial para admitir.</mat-error>
          </mat-form-field>
          <mat-form-field class="full">
            <mat-label>Observación (opcional)</mat-label>
            <textarea matInput formControlName="nota" rows="2" maxlength="500"></textarea>
          </mat-form-field>
        } @else if (decision() === 'rechazar') {
          <mat-form-field class="full">
            <mat-label>Justificación del rechazo (RN01)</mat-label>
            <textarea matInput formControlName="justificacion" rows="4" maxlength="1000" required></textarea>
            <mat-hint>El ciudadano verá este texto. Mínimo 10 caracteres.</mat-hint>
            <mat-error>La justificación es obligatoria (mínimo 10 caracteres).</mat-error>
          </mat-form-field>
        }
      </form>

      @if (error(); as e) {
        <div class="err" role="alert"><strong>{{ e.title }}</strong> {{ e.detail }}</div>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button class="btn-cta" mat-flat-button (click)="enviar()" [disabled]="enviando() || !decision()">
        {{ enviando() ? 'Guardando…' : decision() === 'rechazar' ? 'Confirmar rechazo' : 'Admitir reporte' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .ref { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
    .ref__t { margin-top: 10px; }
    .ref__d { margin: 2px 0 16px; }
    .vform { display: flex; flex-direction: column; gap: 14px; }
    .dec { display: flex; flex-direction: column; gap: 6px; padding: 10px 12px; border: 1px solid var(--ca-border); background: var(--ca-input); }
    .err { margin-top: 12px; padding: 10px 12px; border: 1px solid var(--ca-danger); background: var(--ca-danger-soft); font-size: 14px; }
  `,
})
export class ValidarDialogComponent {
  protected readonly r = inject<Reporte>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<ValidarDialogComponent, Reporte>);
  private readonly api = inject(CiudadActivaApi);
  private readonly notify = inject(NotifyService);
  private readonly fb = inject(FormBuilder);

  protected readonly prioridades = PRIORIDADES;
  protected readonly enviando = signal(false);
  protected readonly error = signal<ProblemDetail | null>(null);

  protected readonly form = this.fb.group({
    decision: this.fb.control<'admitir' | 'rechazar' | null>(null, Validators.required),
    prioridad: this.fb.control<PrioridadReporte | null>(null),
    nota: this.fb.control(''),
    justificacion: this.fb.control(''),
  });
  protected readonly decision = toSignal(this.form.controls.decision.valueChanges, { initialValue: null });

  constructor() {
    this.form.controls.decision.valueChanges.subscribe((d) => {
      const { prioridad, justificacion } = this.form.controls;
      prioridad.setValidators(d === 'admitir' ? [Validators.required] : []);
      justificacion.setValidators(d === 'rechazar' ? [Validators.required, Validators.minLength(10), noBlanco] : []);
      prioridad.updateValueAndValidity();
      justificacion.updateValueAndValidity();
    });
  }

  protected etiquetaPrioridad(p: PrioridadReporte) {
    return PRIORIDAD_LABEL[p];
  }

  protected enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const admitido = v.decision === 'admitir';
    this.enviando.set(true);
    this.error.set(null);
    this.api
      .validar(this.r.id, {
        admitido,
        prioridad: admitido ? v.prioridad : null,
        justificacion: admitido ? v.nota?.trim() || null : v.justificacion?.trim(),
      })
      .subscribe({
        next: (rep) => {
          this.notify.ok(admitido ? 'Reporte admitido con prioridad oficial.' : 'Reporte rechazado con justificación.');
          this.ref.close(rep);
        },
        error: (e) => {
          this.enviando.set(false);
          this.error.set(aProblemDetail(e));
        },
      });
  }
}

function noBlanco(c: { value: string | null }) {
  return c.value && c.value.trim().length === 0 ? { blanco: true } : null;
}

/* =====================================================================
   MODAL ASIGNAR / REASIGNAR OPERADOR · RF05, RN05, RN09
   - Asignar: POST /api/reportes/{id}/asignaciones (solo ADMITIDO)
   - Reasignar: PATCH /api/asignaciones/{id}/reasignar (baja lógica de la previa)
   ===================================================================== */
export interface AsignarDialogData {
  reporte?: Reporte;
  asignacion?: Asignacion;
}

@Component({
  selector: 'app-asignar-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatRadioModule, MatFormFieldModule, MatInputModule, ViewStateComponent, PrioridadBadgeComponent],
  template: `
    <h2 mat-dialog-title>{{ esReasignacion ? 'Reasignar operador' : 'Asignar operador' }}</h2>
    <mat-dialog-content>
      <div class="ref">
        <span class="mono">{{ codigo }}</span>
        <ca-prioridad-badge [prioridad]="prioridad" />
      </div>
      <h3 class="ref__t">{{ titulo }}</h3>
      @if (esReasignacion) {
        <p class="note">
          Operador actual: <strong>{{ data.asignacion?.operadorNombre }}</strong>. La asignación actual se
          <strong>desactivará lógicamente</strong> y se creará una nueva; el historial se conserva (RN05 / RNF08).
        </p>
      } @else {
        <p class="note">Al confirmar, el reporte pasa a <strong>EN PROCESO (asignado)</strong> y el operador lo verá en su app.</p>
      }

      <div class="eyebrow lbl">Operadores activos · carga actual</div>
      <ca-view-state [estado]="ops.estado()" [error]="ops.error()" (reintentar)="ops.cargar()" tituloVacio="No hay operadores activos" textoVacio="Pide al administrador que active o cree operadores.">
        <mat-radio-group class="ops" [formControl]="form.controls.operadorId" aria-label="Operador">
          @for (o of opsOrdenados(); track o.id) {
            <label class="op" [class.op--off]="o.id === data.asignacion?.operadorId">
              <mat-radio-button [value]="o.id" [disabled]="o.id === data.asignacion?.operadorId" />
              <div class="op__info">
                <strong>{{ o.nombre }}</strong>
                <span class="muted">{{ o.email }}</span>
              </div>
              <div class="op__load">
                <div class="bar"><div class="bar__fill" [style.width.%]="carga(o)"></div></div>
                <span class="mono">{{ o.laboresActivas ?? 0 }} activas</span>
              </div>
            </label>
          }
        </mat-radio-group>
      </ca-view-state>

      <mat-form-field class="full obs">
        <mat-label>Observaciones para el operador</mat-label>
        <textarea matInput [formControl]="form.controls.notas" rows="3" maxlength="500"></textarea>
      </mat-form-field>

      @if (error(); as e) {
        <div class="err" role="alert"><strong>{{ e.title }}</strong> {{ e.detail }}</div>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button class="btn-cta" mat-flat-button (click)="enviar()" [disabled]="form.invalid || enviando()">
        {{ enviando() ? 'Guardando…' : esReasignacion ? 'Confirmar reasignación' : 'Asignar operador' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .ref { display: flex; justify-content: space-between; align-items: center; }
    .ref__t { margin: 8px 0 6px; }
    .note { font-size: 14px; padding: 10px 12px; background: var(--ca-input); border: 1px solid var(--ca-border); }
    .lbl { margin: 16px 0 8px; }
    .ops { display: flex; flex-direction: column; gap: 6px; max-height: 300px; overflow: auto; }
    .op { display: grid; grid-template-columns: auto 1fr 150px; align-items: center; gap: 8px; padding: 6px 10px; border: 1px solid var(--ca-border); cursor: pointer; }
    .op:hover { background: var(--ca-accent-soft); }
    .op--off { opacity: 0.5; cursor: not-allowed; }
    .op__info { display: flex; flex-direction: column; font-size: 14px; min-width: 0; }
    .op__info span { font-size: 12px; overflow: hidden; text-overflow: ellipsis; }
    .op__load { display: flex; flex-direction: column; gap: 4px; font-size: 12px; }
    .bar { height: 6px; background: var(--ca-input); border: 1px solid var(--ca-border); }
    .bar__fill { height: 100%; background: var(--ca-cta); }
    .obs { margin-top: 16px; }
    .err { margin-top: 12px; padding: 10px 12px; border: 1px solid var(--ca-danger); background: var(--ca-danger-soft); font-size: 14px; }
  `,
})
export class AsignarDialogComponent {
  protected readonly data = inject<AsignarDialogData>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<AsignarDialogComponent, Asignacion>);
  private readonly api = inject(CiudadActivaApi);
  private readonly notify = inject(NotifyService);
  private readonly fb = inject(FormBuilder);

  protected readonly esReasignacion = !!this.data.asignacion;
  protected readonly codigo = this.data.reporte?.codigoSeguimiento ?? this.data.asignacion?.reporteCodigo ?? `#${this.data.asignacion?.reporteId}`;
  protected readonly titulo = this.data.reporte?.titulo ?? this.data.asignacion?.reporteTitulo ?? '';
  protected readonly prioridad = this.data.reporte?.prioridad ?? this.data.asignacion?.reportePrioridad;

  protected readonly ops = asyncState(() => this.api.operadores());
  protected readonly opsOrdenados = computed(() =>
    [...(this.ops.data() ?? [])].filter((o) => o.activo).sort((a, b) => (a.laboresActivas ?? 0) - (b.laboresActivas ?? 0)),
  );
  private readonly maxCarga = computed(() => Math.max(5, ...(this.ops.data() ?? []).map((o) => o.laboresActivas ?? 0)));

  protected readonly enviando = signal(false);
  protected readonly error = signal<ProblemDetail | null>(null);
  protected readonly form = this.fb.group({
    operadorId: this.fb.control<number | null>(null, Validators.required),
    notas: this.fb.control(''),
  });

  constructor() {
    this.ops.cargar();
  }

  protected carga(o: Usuario): number {
    return Math.min(100, ((o.laboresActivas ?? 0) / this.maxCarga()) * 100);
  }

  protected enviar(): void {
    const { operadorId, notas } = this.form.getRawValue();
    if (!operadorId) return;
    this.enviando.set(true);
    this.error.set(null);
    const body = { operadorId, notas: notas?.trim() || null };
    const op$: Observable<Asignacion> = this.esReasignacion
      ? this.api.reasignar(this.data.asignacion!.id, body)
      : this.api.asignar(this.data.reporte!.id, body);
    op$.subscribe({
      next: (a) => {
        this.notify.ok(this.esReasignacion ? `Reasignado a ${a.operadorNombre}.` : `Asignado a ${a.operadorNombre}.`);
        this.ref.close(a);
      },
      error: (e) => {
        this.enviando.set(false);
        this.error.set(aProblemDetail(e));
      },
    });
  }
}

/* =====================================================================
   HOJA LATERAL DE DETALLE (reutiliza ReporteDetalleComponent)
   ===================================================================== */
@Component({
  selector: 'app-detalle-sheet',
  imports: [ReporteDetalleComponent],
  template: `<ca-reporte-detalle [reporteId]="data.id" [permitirComentar]="data.comentar" (cerrar)="ref.close()" />`,
})
export class DetalleSheetComponent {
  protected readonly data = inject<{ id: number; comentar: boolean }>(MAT_DIALOG_DATA);
  protected readonly ref = inject(MatDialogRef<DetalleSheetComponent>);
}

/** Helpers para abrir los modales con la misma configuración en todas las vistas. */
export function abrirValidar(dialog: MatDialog, r: Reporte): Observable<Reporte | undefined> {
  return dialog.open(ValidarDialogComponent, { data: r, width: '560px', maxWidth: '95vw', autoFocus: false }).afterClosed();
}
export function abrirAsignar(dialog: MatDialog, data: AsignarDialogData): Observable<Asignacion | undefined> {
  return dialog.open(AsignarDialogComponent, { data, width: '620px', maxWidth: '95vw', autoFocus: false }).afterClosed();
}
export function abrirDetalle(dialog: MatDialog, id: number, comentar = true): Observable<void> {
  return dialog
    .open(DetalleSheetComponent, {
      data: { id, comentar },
      width: '520px',
      maxWidth: '100vw',
      height: '100vh',
      position: { right: '0', top: '0' },
      panelClass: 'sheet-right',
      autoFocus: false,
    })
    .afterClosed()
    .pipe(map(() => undefined));
}
