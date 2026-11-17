import { DatePipe } from '@angular/common';
import { Component, effect, inject, signal, untracked, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CiudadActivaApi } from '../../core/api/ciudad-activa.api';
import { ESTADO_LABEL, PRIORIDAD_LABEL, PRIORIDAD_PESO, ROL_LABEL } from '../../core/api/labels';
import { ESTADOS, EstadoReporte, PRIORIDADES, PrioridadReporte, Reporte, RolNombre, Seguimiento } from '../../core/api/models';
import { asyncState } from '../../core/ui/async-state';
import { paginatorEs } from '../../shared/paginator-es';
import { EstadoBadgeComponent, PageHeaderComponent, PrioridadBadgeComponent, ViewStateComponent } from '../../shared/ui.components';
import { abrirDetalle } from '../gestor/dialogs';

@Component({
  selector: 'app-auditoria',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatTabsModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    PageHeaderComponent,
    ViewStateComponent,
    EstadoBadgeComponent,
    PrioridadBadgeComponent,
  ],
  providers: [{ provide: MatPaginatorIntl, useFactory: paginatorEs }],
  template: `
    <div class="page">
      <ca-page-header codigo="A-04" seccion="Administrador" titulo="Auditoría general de reportes" descripcion="Vista global en modo solo lectura de todos los reportes, sus historiales y la bitácora inmutable de eventos del sistema.">
        <span class="badge ro"><mat-icon inline>lock</mat-icon> Solo lectura</span>
      </ca-page-header>

      <mat-tab-group animationDuration="0ms" mat-stretch-tabs="false" (selectedIndexChange)="tab.set($event)">
        <mat-tab label="Reportes">
          <div class="tabbody">
            <div class="filters">
              <mat-form-field>
                <mat-label>Estado</mat-label>
                <mat-select [formControl]="fEstado">
                  <mat-option [value]="null">Todos</mat-option>
                  @for (e of estados; track e) { <mat-option [value]="e">{{ estadoLabel[e] }}</mat-option> }
                </mat-select>
              </mat-form-field>
              <mat-form-field>
                <mat-label>Prioridad</mat-label>
                <mat-select [formControl]="fPrioridad">
                  <mat-option [value]="null">Todas</mat-option>
                  @for (p of prioridades; track p) { <mat-option [value]="p">{{ prioridadLabel[p] }}</mat-option> }
                </mat-select>
              </mat-form-field>
              <mat-form-field class="search">
                <mat-label>Buscar</mat-label>
                <input matInput [formControl]="fTexto" placeholder="Radicado, título, ciudadano…" />
                <mat-icon matSuffix>search</mat-icon>
              </mat-form-field>
            </div>
            <ca-view-state [estado]="reportes.estado()" [error]="reportes.error()" (reintentar)="reportes.cargar()">
              <div class="ca-card ca-card--flush">
                <div class="table-scroll">
                  <table mat-table [dataSource]="dsR" matSort #sortR="matSort" matSortActive="creadoEn" matSortDirection="desc">
                    <ng-container matColumnDef="codigoSeguimiento">
                      <th mat-header-cell *matHeaderCellDef mat-sort-header>Radicado</th>
                      <td mat-cell *matCellDef="let r" class="mono nowrap">{{ r.codigoSeguimiento }}</td>
                    </ng-container>
                    <ng-container matColumnDef="titulo">
                      <th mat-header-cell *matHeaderCellDef mat-sort-header>Reporte</th>
                      <td mat-cell *matCellDef="let r"><div class="strong">{{ r.titulo }}</div><div class="muted small">{{ r.categoriaNombre }}</div></td>
                    </ng-container>
                    <ng-container matColumnDef="ciudadano">
                      <th mat-header-cell *matHeaderCellDef>Ciudadano</th>
                      <td mat-cell *matCellDef="let r">{{ r.ciudadanoNombre }}</td>
                    </ng-container>
                    <ng-container matColumnDef="estado">
                      <th mat-header-cell *matHeaderCellDef mat-sort-header>Estado</th>
                      <td mat-cell *matCellDef="let r"><ca-estado-badge [estado]="r.estado" /></td>
                    </ng-container>
                    <ng-container matColumnDef="prioridad">
                      <th mat-header-cell *matHeaderCellDef mat-sort-header>Prioridad</th>
                      <td mat-cell *matCellDef="let r"><ca-prioridad-badge [prioridad]="r.prioridad" /></td>
                    </ng-container>
                    <ng-container matColumnDef="creadoEn">
                      <th mat-header-cell *matHeaderCellDef mat-sort-header>Registrado</th>
                      <td mat-cell *matCellDef="let r" class="nowrap">{{ r.creadoEn | date: 'd MMM y' }}</td>
                    </ng-container>
                    <ng-container matColumnDef="fechaCierre">
                      <th mat-header-cell *matHeaderCellDef mat-sort-header>Cierre</th>
                      <td mat-cell *matCellDef="let r" class="nowrap">{{ r.fechaCierre ? (r.fechaCierre | date: 'd MMM y') : '—' }}</td>
                    </ng-container>
                    <ng-container matColumnDef="ver">
                      <th mat-header-cell *matHeaderCellDef></th>
                      <td mat-cell *matCellDef="let r" class="right"><button mat-stroked-button (click)="ver(r)"><mat-icon>history</mat-icon> Historial</button></td>
                    </ng-container>
                    <tr mat-header-row *matHeaderRowDef="colsR; sticky: true"></tr>
                    <tr mat-row *matRowDef="let row; columns: colsR"></tr>
                    <tr class="mat-row" *matNoDataRow><td class="mat-cell nodata" [attr.colspan]="colsR.length">Sin coincidencias.</td></tr>
                  </table>
                </div>
                <mat-paginator #pagR [pageSizeOptions]="[10, 25, 50, 100]" [pageSize]="10" showFirstLastButtons />
              </div>
            </ca-view-state>
          </div>
        </mat-tab>

        <mat-tab label="Bitácora de eventos">
          <div class="tabbody">
            <p class="muted">Cada transición queda registrada con su autor y un hash de verificación. Los eventos no se pueden editar ni borrar.</p>
            <ca-view-state [estado]="eventos.estado()" [error]="eventos.error()" (reintentar)="eventos.cargar()" tituloVacio="Sin eventos registrados">
              <div class="ca-card ca-card--flush">
                <div class="table-scroll">
                  <table mat-table [dataSource]="dsE">
                    <ng-container matColumnDef="fecha">
                      <th mat-header-cell *matHeaderCellDef>Fecha</th>
                      <td mat-cell *matCellDef="let e" class="nowrap">{{ e.creadoEn | date: 'd MMM y, h:mm a' }}</td>
                    </ng-container>
                    <ng-container matColumnDef="reporte">
                      <th mat-header-cell *matHeaderCellDef>Reporte</th>
                      <td mat-cell *matCellDef="let e"><span class="mono muted">#{{ e.reporteId }}</span> {{ e.reporteTitulo }}</td>
                    </ng-container>
                    <ng-container matColumnDef="accion">
                      <th mat-header-cell *matHeaderCellDef>Acción</th>
                      <td mat-cell *matCellDef="let e"><div class="strong">{{ accion(e.accion) }}</div><div class="small mono">{{ txtEstado(e.estadoAnterior) }} → {{ txtEstado(e.estadoNuevo) }}</div></td>
                    </ng-container>
                    <ng-container matColumnDef="usuario">
                      <th mat-header-cell *matHeaderCellDef>Autor</th>
                      <td mat-cell *matCellDef="let e">{{ e.usuarioNombre }}<div class="muted small">{{ txtRol(e.usuarioRol) }}</div></td>
                    </ng-container>
                    <ng-container matColumnDef="obs">
                      <th mat-header-cell *matHeaderCellDef>Observaciones</th>
                      <td mat-cell *matCellDef="let e" class="obs">{{ e.observaciones }}</td>
                    </ng-container>
                    <ng-container matColumnDef="hash">
                      <th mat-header-cell *matHeaderCellDef>Hash</th>
                      <td mat-cell *matCellDef="let e" class="mono small" [matTooltip]="e.hashTrazabilidad || ''">{{ (e.hashTrazabilidad || '—').slice(0, 10) }}</td>
                    </ng-container>
                    <tr mat-header-row *matHeaderRowDef="colsE; sticky: true"></tr>
                    <tr mat-row *matRowDef="let row; columns: colsE"></tr>
                  </table>
                </div>
                <mat-paginator #pagE [pageSizeOptions]="[25, 50, 100]" [pageSize]="25" showFirstLastButtons />
              </div>
            </ca-view-state>
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: `
    .ro { color: var(--ca-on-surface-variant); }
    .ro::before { display: none; }
    .tabbody { padding-top: 20px; }
    .filters { margin-bottom: 16px; }
    .search { width: 280px !important; }
    table { width: 100%; }
    .strong { font-weight: 600; }
    .small { font-size: 12px; }
    .right { text-align: right; }
    .nowrap { white-space: nowrap; }
    .obs { max-width: 360px; font-size: 13px; }
    .nodata { padding: 24px; text-align: center; color: var(--ca-on-surface-variant); }
  `,
})
export class AuditoriaComponent {
  private readonly api = inject(CiudadActivaApi);
  private readonly dialog = inject(MatDialog);

  protected readonly estados = ESTADOS;
  protected readonly prioridades = PRIORIDADES;
  protected readonly estadoLabel = ESTADO_LABEL;
  protected readonly prioridadLabel = PRIORIDAD_LABEL;
  protected readonly colsR = ['codigoSeguimiento', 'titulo', 'ciudadano', 'estado', 'prioridad', 'creadoEn', 'fechaCierre', 'ver'];
  protected readonly colsE = ['fecha', 'reporte', 'accion', 'usuario', 'obs', 'hash'];
  protected readonly tab = signal(0);

  protected readonly fEstado = new FormControl<EstadoReporte | null>(null);
  protected readonly fPrioridad = new FormControl<PrioridadReporte | null>(null);
  protected readonly fTexto = new FormControl('', { nonNullable: true });

  protected readonly reportes = asyncState(() => this.api.reportes({ estado: this.fEstado.value, prioridad: this.fPrioridad.value }));
  protected readonly eventos = asyncState(() => this.api.auditoria());
  protected readonly dsR = new MatTableDataSource<Reporte>([]);
  protected readonly dsE = new MatTableDataSource<Seguimiento>([]);
  private readonly sortR = viewChild<MatSort>('sortR');
  private readonly pagR = viewChild<MatPaginator>('pagR');
  private readonly pagE = viewChild<MatPaginator>('pagE');
  private eventosCargados = false;

  constructor() {
    this.reportes.cargar();
    this.fEstado.valueChanges.subscribe(() => this.reportes.cargar());
    this.fPrioridad.valueChanges.subscribe(() => this.reportes.cargar());
    this.fTexto.valueChanges.subscribe((t) => (this.dsR.filter = t.trim().toLowerCase()));
    this.dsR.filterPredicate = (r, f) => [r.codigoSeguimiento, r.titulo, r.ciudadanoNombre, r.categoriaNombre, r.direccion].filter(Boolean).join(' ').toLowerCase().includes(f);
    this.dsR.sortingDataAccessor = (r, c) => (c === 'prioridad' ? PRIORIDAD_PESO[r.prioridad] : c === 'estado' ? ESTADOS.indexOf(r.estado) : ((r as unknown as Record<string, string>)[c] ?? ''));

    effect(() => (this.dsR.data = this.reportes.data() ?? []));
    effect(() => (this.dsE.data = [...(this.eventos.data() ?? [])].sort((a, b) => b.creadoEn.localeCompare(a.creadoEn))));
    effect(() => {
      const s = this.sortR();
      const p = this.pagR();
      const pe = this.pagE();
      untracked(() => {
        if (s) this.dsR.sort = s;
        if (p) this.dsR.paginator = p;
        if (pe) this.dsE.paginator = pe;
      });
    });
    // Carga diferida de la bitácora al abrir la pestaña
    effect(() => {
      if (this.tab() === 1 && !this.eventosCargados) {
        this.eventosCargados = true;
        untracked(() => this.eventos.cargar());
      }
    });
  }

  /** Ficha en modo solo lectura (sin formulario de comentarios). */
  protected ver(r: Reporte): void {
    abrirDetalle(this.dialog, r.id, false).subscribe();
  }
  protected txtEstado(e?: EstadoReporte | null): string {
    return e ? ESTADO_LABEL[e] : 'Inicio';
  }
  protected txtRol(r?: string): string {
    return (r && ROL_LABEL[r as RolNombre]) || r || '';
  }
  protected accion(a: string): string {
    return (a || '').replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
  }
}
