import { DatePipe } from '@angular/common';
import { Component, computed, effect, inject, signal, untracked, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { filter } from 'rxjs';
import { CiudadActivaApi } from '../../core/api/ciudad-activa.api';
import { Asignacion, Usuario } from '../../core/api/models';
import { asyncState } from '../../core/ui/async-state';
import { paginatorEs } from '../../shared/paginator-es';
import { EstadoBadgeComponent, PageHeaderComponent, PrioridadBadgeComponent, ViewStateComponent } from '../../shared/ui.components';
import { abrirAsignar, abrirDetalle } from './dialogs';

/** Historial completo de asignaciones de un reporte (activas e inactivas, RN05). */
@Component({
  selector: 'app-historial-asignaciones',
  imports: [DatePipe, MatDialogModule, MatButtonModule, ViewStateComponent],
  template: `
    <h2 mat-dialog-title>Historial de asignaciones</h2>
    <mat-dialog-content>
      <p class="muted">{{ data.reporteCodigo }} · {{ data.reporteTitulo }}</p>
      <ca-view-state [estado]="hist.estado()" [error]="hist.error()" (reintentar)="hist.cargar()">
        <ol class="h">
          @for (a of hist.data() ?? []; track a.id) {
            <li class="h__i" [class.h__i--on]="a.activa">
              <div class="h__top">
                <strong>{{ a.operadorNombre }}</strong>
                <span class="badge" [style.color]="a.activa ? 'var(--ca-primary)' : 'var(--ca-on-surface-variant)'">{{ a.activa ? 'Activa' : 'Anterior' }}</span>
              </div>
              <div class="muted">Asignó {{ a.gestorNombre }} · {{ a.creadoEn | date: 'd MMM y, h:mm a' }}</div>
              @if (a.notas) {
                <div class="h__n">“{{ a.notas }}”</div>
              }
            </li>
          }
        </ol>
      </ca-view-state>
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-button mat-dialog-close>Cerrar</button></mat-dialog-actions>
  `,
  styles: `
    .h { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 10px; }
    .h__i { border: 1px solid var(--ca-border); padding: 10px 12px; font-size: 14px; }
    .h__i--on { border-left: 4px solid var(--ca-sage); }
    .h__top { display: flex; justify-content: space-between; gap: 8px; align-items: center; }
    .h__n { margin-top: 4px; font-style: italic; }
  `,
})
export class HistorialAsignacionesComponent {
  protected readonly data = inject<Asignacion>(MAT_DIALOG_DATA);
  private readonly api = inject(CiudadActivaApi);
  protected readonly hist = asyncState(() => this.api.historialAsignaciones(this.data.reporteId));
  constructor() {
    this.hist.cargar();
  }
}

@Component({
  selector: 'app-asignaciones',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    PageHeaderComponent,
    ViewStateComponent,
    EstadoBadgeComponent,
    PrioridadBadgeComponent,
  ],
  providers: [{ provide: MatPaginatorIntl, useFactory: paginatorEs }],
  template: `
    <div class="page">
      <ca-page-header codigo="G-03" seccion="Gestor" titulo="Gestión de cuadrillas y asignaciones" descripcion="Carga de trabajo de cada operador y reportes asignados. Puedes reasignar un reporte a otro operador; el historial de asignaciones se conserva.">
        <button mat-stroked-button (click)="recargar()"><mat-icon>refresh</mat-icon> Actualizar</button>
      </ca-page-header>

      <section class="ca-card crew">
        <div class="ca-card__head">
          <h2>Cuadrillas en campo</h2>
          <span class="eyebrow">{{ (ops.data() ?? []).length }} operadores activos</span>
        </div>
        <ca-view-state [estado]="ops.estado()" [error]="ops.error()" (reintentar)="ops.cargar()" tituloVacio="Sin operadores activos">
          <div class="crew__grid">
            @for (o of opsOrdenados(); track o.id) {
              <button class="op" [class.op--sel]="fOperador.value === o.id" (click)="filtrarOperador(o)" [attr.aria-pressed]="fOperador.value === o.id">
                <div class="op__name">{{ o.nombre }}</div>
                <div class="op__bar"><div [style.width.%]="carga(o)"></div></div>
                <div class="op__meta"><span class="kpi-value op__n">{{ o.laboresActivas ?? 0 }}</span><span class="muted">labores activas</span></div>
              </button>
            }
          </div>
        </ca-view-state>
      </section>

      <div class="filters">
        <mat-form-field>
          <mat-label>Operador</mat-label>
          <mat-select [formControl]="fOperador">
            <mat-option [value]="null">Todos</mat-option>
            @for (o of ops.data() ?? []; track o.id) {
              <mat-option [value]="o.id">{{ o.nombre }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field class="search">
          <mat-label>Buscar</mat-label>
          <input matInput [formControl]="fTexto" placeholder="Radicado, título, categoría…" />
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
      </div>

      <ca-view-state [estado]="asig.estado()" [error]="asig.error()" (reintentar)="asig.cargar()" tituloVacio="No hay asignaciones activas" textoVacio="Asigna reportes admitidos desde la bandeja o el triage." iconoVacio="assignment_turned_in">
        <div class="ca-card ca-card--flush">
          <div class="table-scroll">
            <table mat-table [dataSource]="ds">
              <ng-container matColumnDef="reporte">
                <th mat-header-cell *matHeaderCellDef>Reporte</th>
                <td mat-cell *matCellDef="let a">
                  <div class="mono muted">{{ a.reporteCodigo || '#' + a.reporteId }}</div>
                  <div class="strong">{{ a.reporteTitulo }}</div>
                  <div class="muted small">{{ a.categoriaNombre }}</div>
                </td>
              </ng-container>
              <ng-container matColumnDef="estado">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let a">
                  @if (a.reporteEstado) { <ca-estado-badge [estado]="a.reporteEstado" /> }
                  @if (a.reportePrioridad) { <div class="mt"><ca-prioridad-badge [prioridad]="a.reportePrioridad" /></div> }
                </td>
              </ng-container>
              <ng-container matColumnDef="operador">
                <th mat-header-cell *matHeaderCellDef>Operador</th>
                <td mat-cell *matCellDef="let a">
                  <div class="strong">{{ a.operadorNombre }}</div>
                  <div class="muted small">{{ a.operadorEmail }}</div>
                </td>
              </ng-container>
              <ng-container matColumnDef="gestor">
                <th mat-header-cell *matHeaderCellDef>Asignó</th>
                <td mat-cell *matCellDef="let a">
                  <div>{{ a.gestorNombre }}</div>
                  <div class="muted small">{{ a.creadoEn | date: 'd MMM y, h:mm a' }}</div>
                </td>
              </ng-container>
              <ng-container matColumnDef="notas">
                <th mat-header-cell *matHeaderCellDef>Observaciones</th>
                <td mat-cell *matCellDef="let a" class="notas">{{ a.notas || '—' }}</td>
              </ng-container>
              <ng-container matColumnDef="acciones">
                <th mat-header-cell *matHeaderCellDef class="right">Acciones</th>
                <td mat-cell *matCellDef="let a" class="right nowrap">
                  <button mat-icon-button (click)="ver(a)" matTooltip="Ficha del reporte" aria-label="Ficha"><mat-icon>visibility</mat-icon></button>
                  <button mat-icon-button (click)="historial(a)" matTooltip="Historial de asignaciones" aria-label="Historial"><mat-icon>history</mat-icon></button>
                  <button mat-stroked-button (click)="reasignar(a)"><mat-icon>swap_horiz</mat-icon> Reasignar</button>
                </td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="columnas; sticky: true"></tr>
              <tr mat-row *matRowDef="let row; columns: columnas"></tr>
              <tr class="mat-row" *matNoDataRow>
                <td class="mat-cell nodata" [attr.colspan]="columnas.length">Sin coincidencias para los filtros.</td>
              </tr>
            </table>
          </div>
          <mat-paginator [pageSizeOptions]="[10, 25, 50]" [pageSize]="10" showFirstLastButtons />
        </div>
      </ca-view-state>
    </div>
  `,
  styles: `
    .crew { margin-bottom: 20px; }
    .crew__grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 10px; }
    .op { text-align: left; font: inherit; color: inherit; background: var(--ca-input); border: 1px solid var(--ca-border); padding: 12px 14px; cursor: pointer; }
    .op:hover { border-color: var(--ca-sage); }
    .op--sel { outline: 2px solid var(--ca-sage); background: var(--ca-card); }
    .op__name { font-weight: 700; }
    .op__bar { height: 6px; border: 1px solid var(--ca-border); background: var(--ca-card); margin: 8px 0; }
    .op__bar div { height: 100%; background: var(--ca-cta); }
    .op__meta { display: flex; align-items: baseline; gap: 6px; font-size: 13px; }
    .op__n { font-size: 24px; }
    .search { width: 280px !important; }
    .filters { margin-bottom: 16px; }
    table { width: 100%; }
    .strong { font-weight: 600; }
    .small { font-size: 12px; }
    .mt { margin-top: 4px; }
    .notas { max-width: 260px; font-size: 13px; }
    .right { text-align: right; }
    .nowrap { white-space: nowrap; }
    .nodata { padding: 24px; text-align: center; color: var(--ca-on-surface-variant); }
  `,
})
export class AsignacionesComponent {
  private readonly api = inject(CiudadActivaApi);
  private readonly dialog = inject(MatDialog);

  protected readonly columnas = ['reporte', 'estado', 'operador', 'gestor', 'notas', 'acciones'];
  protected readonly ops = asyncState(() => this.api.operadores());
  protected readonly asig = asyncState(() => this.api.asignacionesActivas());
  protected readonly ds = new MatTableDataSource<Asignacion>([]);
  private readonly paginator = viewChild(MatPaginator);

  protected readonly fOperador = new FormControl<number | null>(null);
  protected readonly fTexto = new FormControl('', { nonNullable: true });
  private readonly filtroSig = signal<{ op: number | null; t: string }>({ op: null, t: '' });

  protected readonly opsOrdenados = computed(() => [...(this.ops.data() ?? [])].sort((a, b) => (b.laboresActivas ?? 0) - (a.laboresActivas ?? 0)));
  private readonly maxCarga = computed(() => Math.max(4, ...(this.ops.data() ?? []).map((o) => o.laboresActivas ?? 0)));

  constructor() {
    this.recargar();
    this.ds.filterPredicate = (a, f) => {
      const { op, t } = JSON.parse(f) as { op: number | null; t: string };
      if (op && a.operadorId !== op) return false;
      if (!t) return true;
      return [a.reporteCodigo, a.reporteTitulo, a.categoriaNombre, a.operadorNombre, a.notas].filter(Boolean).join(' ').toLowerCase().includes(t);
    };
    this.fOperador.valueChanges.subscribe((op) => this.filtroSig.update((f) => ({ ...f, op })));
    this.fTexto.valueChanges.subscribe((t) => this.filtroSig.update((f) => ({ ...f, t: t.trim().toLowerCase() })));
    effect(() => {
      this.ds.data = this.asig.data() ?? [];
    });
    effect(() => {
      const f = this.filtroSig();
      untracked(() => {
        this.ds.filter = JSON.stringify(f);
        this.paginator()?.firstPage();
      });
    });
    effect(() => {
      const p = this.paginator();
      if (p) this.ds.paginator = p;
    });
  }

  protected recargar(): void {
    this.ops.cargar();
    this.asig.cargar();
  }

  protected carga(o: Usuario): number {
    return Math.min(100, ((o.laboresActivas ?? 0) / this.maxCarga()) * 100);
  }

  protected filtrarOperador(o: Usuario): void {
    this.fOperador.setValue(this.fOperador.value === o.id ? null : o.id);
  }

  protected ver(a: Asignacion): void {
    abrirDetalle(this.dialog, a.reporteId).subscribe();
  }

  protected historial(a: Asignacion): void {
    this.dialog.open(HistorialAsignacionesComponent, { data: a, width: '520px', maxWidth: '95vw' });
  }

  protected reasignar(a: Asignacion): void {
    abrirAsignar(this.dialog, { asignacion: a })
      .pipe(filter(Boolean))
      .subscribe(() => this.recargar());
  }
}
