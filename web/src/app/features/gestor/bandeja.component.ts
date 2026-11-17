import { DatePipe } from '@angular/common';
import { AfterViewInit, Component, computed, effect, inject, input, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
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
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, filter, of, switchMap } from 'rxjs';
import { CiudadActivaApi } from '../../core/api/ciudad-activa.api';
import { ESTADO_LABEL, PRIORIDAD_LABEL, PRIORIDAD_PESO, iconoCategoria, puedeAsignar, puedeValidar } from '../../core/api/labels';
import { ESTADOS, EstadoReporte, PRIORIDADES, PrioridadReporte, Reporte } from '../../core/api/models';
import { asyncState } from '../../core/ui/async-state';
import { paginatorEs } from '../../shared/paginator-es';
import { EstadoBadgeComponent, PageHeaderComponent, PrioridadBadgeComponent, ViewStateComponent } from '../../shared/ui.components';
import { abrirAsignar, abrirDetalle, abrirValidar } from './dialogs';

@Component({
  selector: 'app-bandeja',
  imports: [
    DatePipe,
    ReactiveFormsModule,
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
      <ca-page-header codigo="G-02" seccion="Gestor" titulo="Bandeja unificada de reportes" descripcion="Filtra, inspecciona, valida y asigna. Las acciones respetan la máquina de estados (RN04) y los bloqueos de asignación (RN09).">
        <button mat-stroked-button (click)="reportes.cargar()"><mat-icon>refresh</mat-icon> Actualizar</button>
      </ca-page-header>

      <div class="filters">
        <mat-form-field>
          <mat-label>Categoría</mat-label>
          <mat-select [formControl]="fCategoria">
            <mat-option [value]="null">Todas</mat-option>
            @for (c of categorias.data() ?? []; track c.id) {
              <mat-option [value]="c.id">{{ c.nombre }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Estado</mat-label>
          <mat-select [formControl]="fEstado">
            <mat-option [value]="null">Todos</mat-option>
            @for (e of estados; track e) {
              <mat-option [value]="e">{{ estadoLabel[e] }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Prioridad</mat-label>
          <mat-select [formControl]="fPrioridad">
            <mat-option [value]="null">Todas</mat-option>
            @for (p of prioridades; track p) {
              <mat-option [value]="p">{{ prioridadLabel[p] }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field class="search">
          <mat-label>Buscar</mat-label>
          <input matInput [formControl]="fTexto" placeholder="Radicado, título, dirección…" />
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
        @if (hayFiltros()) {
          <button mat-button class="clear" (click)="limpiar()"><mat-icon>filter_alt_off</mat-icon> Limpiar</button>
        }
      </div>

      <ca-view-state [estado]="estadoVista()" [error]="reportes.error()" (reintentar)="reportes.cargar()" tituloVacio="Sin reportes para estos filtros" textoVacio="Prueba con otra categoría, estado o prioridad.">
        <div class="ca-card ca-card--flush">
          <div class="table-scroll">
            <table mat-table [dataSource]="ds" matSort matSortActive="creadoEn" matSortDirection="desc">
              <ng-container matColumnDef="codigoSeguimiento">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Radicado</th>
                <td mat-cell *matCellDef="let r" class="mono nowrap">{{ r.codigoSeguimiento }}</td>
              </ng-container>
              <ng-container matColumnDef="titulo">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Reporte</th>
                <td mat-cell *matCellDef="let r">
                  <div class="t">
                    <mat-icon class="t__ic">{{ icono(r.categoriaIcono) }}</mat-icon>
                    <div>
                      <div class="t__title">{{ r.titulo }}</div>
                      <div class="muted t__sub">{{ r.categoriaNombre }} · {{ r.direccion || 'Sin dirección' }}</div>
                    </div>
                  </div>
                </td>
              </ng-container>
              <ng-container matColumnDef="estado">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Estado</th>
                <td mat-cell *matCellDef="let r"><ca-estado-badge [estado]="r.estado" /></td>
              </ng-container>
              <ng-container matColumnDef="prioridad">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Prioridad</th>
                <td mat-cell *matCellDef="let r"><ca-prioridad-badge [prioridad]="r.prioridad" /></td>
              </ng-container>
              <ng-container matColumnDef="operador">
                <th mat-header-cell *matHeaderCellDef>Operador</th>
                <td mat-cell *matCellDef="let r">{{ r.operadorAsignadoNombre || '—' }}</td>
              </ng-container>
              <ng-container matColumnDef="creadoEn">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Registrado</th>
                <td mat-cell *matCellDef="let r" class="nowrap">{{ r.creadoEn | date: 'd MMM y' }}</td>
              </ng-container>
              <ng-container matColumnDef="acciones">
                <th mat-header-cell *matHeaderCellDef class="right">Acciones</th>
                <td mat-cell *matCellDef="let r" class="right nowrap">
                  <button mat-icon-button (click)="ver(r)" matTooltip="Ver ficha e historial" aria-label="Ver ficha"><mat-icon>visibility</mat-icon></button>
                  <span [matTooltip]="validable(r) ? 'Validar (admitir / rechazar)' : 'Solo se validan reportes PENDIENTE o EN REVISIÓN (RN04)'">
                    <button mat-icon-button (click)="validar(r)" [disabled]="!validable(r)" aria-label="Validar"><mat-icon>rule</mat-icon></button>
                  </span>
                  <span [matTooltip]="tooltipAsignar(r)">
                    <button mat-icon-button (click)="asignar(r)" [disabled]="!asignable(r)" aria-label="Asignar operador"><mat-icon>person_add</mat-icon></button>
                  </span>
                </td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="columnas; sticky: true"></tr>
              <tr mat-row *matRowDef="let row; columns: columnas"></tr>
              <tr class="mat-row" *matNoDataRow>
                <td class="mat-cell nodata" [attr.colspan]="columnas.length">Ningún reporte coincide con “{{ fTexto.value }}”.</td>
              </tr>
            </table>
          </div>
          <mat-paginator [pageSizeOptions]="[10, 25, 50, 100]" [pageSize]="10" showFirstLastButtons />
        </div>
      </ca-view-state>
    </div>
  `,
  styles: `
    .search { width: 280px !important; }
    .clear { margin-top: 8px; }
    .filters { margin-bottom: 16px; }
    table { width: 100%; }
    .t { display: flex; gap: 10px; align-items: center; min-width: 260px; }
    .t__ic { color: var(--ca-primary); }
    .t__title { font-weight: 600; }
    .t__sub { font-size: 12px; }
    .right { text-align: right; }
    .nowrap { white-space: nowrap; }
    .nodata { padding: 24px; text-align: center; color: var(--ca-on-surface-variant); }
  `,
})
export class BandejaComponent implements AfterViewInit {
  private readonly api = inject(CiudadActivaApi);
  private readonly dialog = inject(MatDialog);

  /** Query param ?estado=PENDIENTE (accesos rápidos del triage) */
  estado = input<string | undefined>();

  protected readonly estados = ESTADOS;
  protected readonly prioridades = PRIORIDADES;
  protected readonly estadoLabel = ESTADO_LABEL;
  protected readonly prioridadLabel = PRIORIDAD_LABEL;
  protected readonly columnas = ['codigoSeguimiento', 'titulo', 'estado', 'prioridad', 'operador', 'creadoEn', 'acciones'];

  protected readonly fCategoria = new FormControl<number | null>(null);
  protected readonly fEstado = new FormControl<EstadoReporte | null>(null);
  protected readonly fPrioridad = new FormControl<PrioridadReporte | null>(null);
  protected readonly fTexto = new FormControl('', { nonNullable: true });

  protected readonly categorias = asyncState(() => this.api.categoriasActivas());
  protected readonly reportes = asyncState(() =>
    this.api.reportes({ categoriaId: this.fCategoria.value, estado: this.fEstado.value, prioridad: this.fPrioridad.value }),
  );

  protected readonly ds = new MatTableDataSource<Reporte>([]);
  private readonly sort = viewChild(MatSort);
  private readonly paginator = viewChild(MatPaginator);
  private readonly texto = toSignal(this.fTexto.valueChanges.pipe(debounceTime(200)), { initialValue: '' });
  protected readonly hayFiltros = signal(false);
  protected readonly estadoVista = computed(() => this.reportes.estado());

  constructor() {
    this.categorias.cargar();
    // Prefiltro desde query param
    effect(() => {
      const e = this.estado();
      if (e && (ESTADOS as string[]).includes(e)) this.fEstado.setValue(e as EstadoReporte, { emitEvent: false });
      this.reportes.cargar();
      this.actualizarHayFiltros();
    });
    const recargar = () => {
      this.reportes.cargar();
      this.actualizarHayFiltros();
    };
    this.fCategoria.valueChanges.subscribe(recargar);
    this.fEstado.valueChanges.subscribe(recargar);
    this.fPrioridad.valueChanges.subscribe(recargar);
    effect(() => {
      this.ds.data = this.reportes.data() ?? [];
    });
    effect(() => {
      this.ds.filter = this.texto().trim().toLowerCase();
      this.actualizarHayFiltros();
      this.paginator()?.firstPage();
    });
    // Mantener sort/paginator conectados aunque la tabla aparezca después del loading
    effect(() => {
      const s = this.sort();
      const p = this.paginator();
      if (s) this.ds.sort = s;
      if (p) this.ds.paginator = p;
    });
    this.ds.filterPredicate = (r, f) =>
      [r.codigoSeguimiento, r.titulo, r.direccion, r.barrio, r.categoriaNombre, r.ciudadanoNombre, r.operadorAsignadoNombre]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(f);
    this.ds.sortingDataAccessor = (r, col) => {
      if (col === 'prioridad') return PRIORIDAD_PESO[r.prioridad] ?? 0;
      if (col === 'estado') return ESTADOS.indexOf(r.estado);
      return (r as unknown as Record<string, string>)[col] ?? '';
    };
  }

  ngAfterViewInit(): void {
    const s = this.sort();
    if (s) this.ds.sort = s;
  }

  protected icono = iconoCategoria;
  protected validable = (r: Reporte) => puedeValidar(r.estado);
  protected asignable = (r: Reporte) => puedeAsignar(r.estado);
  protected tooltipAsignar(r: Reporte): string {
    if (r.estado === 'RECHAZADO' || r.estado === 'CERRADO') return `Bloqueado: el reporte está ${ESTADO_LABEL[r.estado]} (RN09)`;
    if (puedeAsignar(r.estado)) return 'Asignar operador';
    if (r.estado === 'EN_PROCESO') return 'Ya asignado. Reasigna desde Cuadrillas.';
    return 'Primero admite el reporte con prioridad oficial';
  }

  protected limpiar(): void {
    this.fCategoria.setValue(null, { emitEvent: false });
    this.fEstado.setValue(null, { emitEvent: false });
    this.fPrioridad.setValue(null, { emitEvent: false });
    this.fTexto.setValue('');
    this.reportes.cargar();
    this.actualizarHayFiltros();
  }

  private actualizarHayFiltros(): void {
    this.hayFiltros.set(!!(this.fCategoria.value || this.fEstado.value || this.fPrioridad.value || this.fTexto.value));
  }

  protected ver(r: Reporte): void {
    abrirDetalle(this.dialog, r.id).subscribe(() => this.reportes.cargar());
  }

  protected validar(r: Reporte): void {
    abrirValidar(this.dialog, r)
      .pipe(
        filter((x): x is Reporte => !!x),
        switchMap((rep) => (rep.estado === 'ADMITIDO' ? abrirAsignar(this.dialog, { reporte: rep }) : of(null))),
      )
      .subscribe({ complete: () => this.reportes.cargar() });
  }

  protected asignar(r: Reporte): void {
    abrirAsignar(this.dialog, { reporte: r })
      .pipe(filter(Boolean))
      .subscribe(() => this.reportes.cargar());
  }
}
