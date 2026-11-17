import { DatePipe } from '@angular/common';
import { Component, effect, inject, signal, untracked, viewChild } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Observable, filter, of, switchMap } from 'rxjs';
import { CiudadActivaApi } from '../../core/api/ciudad-activa.api';
import { ROL_LABEL } from '../../core/api/labels';
import { ROLES, RolNombre, Usuario } from '../../core/api/models';
import { AuthService } from '../../core/auth/auth.service';
import { ProblemDetail, aProblemDetail } from '../../core/http/problem-detail';
import { asyncState } from '../../core/ui/async-state';
import { NotifyService } from '../../core/ui/notify.service';
import { paginatorEs } from '../../shared/paginator-es';
import { PageHeaderComponent, ViewStateComponent } from '../../shared/ui.components';

/* ===================== Modal crear / editar ===================== */
@Component({
  selector: 'app-usuario-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ u ? 'Editar usuario' : 'Crear usuario' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form-grid">
        <mat-form-field class="span-2">
          <mat-label>Nombre completo</mat-label>
          <input matInput formControlName="nombre" maxlength="120" />
          <mat-error>El nombre es obligatorio.</mat-error>
        </mat-form-field>
        <mat-form-field class="span-2">
          <mat-label>Correo electrónico</mat-label>
          <input matInput type="email" formControlName="email" />
          <mat-error>Ingresa un correo válido.</mat-error>
        </mat-form-field>
        @if (!u) {
          <mat-form-field class="span-2">
            <mat-label>Contraseña inicial</mat-label>
            <input matInput type="password" formControlName="password" autocomplete="new-password" />
            <mat-hint>Mínimo 6 caracteres. El usuario podrá cambiarla luego.</mat-hint>
            <mat-error>Mínimo 6 caracteres.</mat-error>
          </mat-form-field>
        }
        <mat-form-field>
          <mat-label>Teléfono</mat-label>
          <input matInput formControlName="telefono" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Rol (único por sesión · RN08)</mat-label>
          <mat-select formControlName="rol">
            @for (r of roles; track r) {
              <mat-option [value]="r" [disabled]="esYo && r !== 'ROLE_ADMIN'">{{ rolLabel[r] }}</mat-option>
            }
          </mat-select>
          @if (esYo) { <mat-hint>No puedes quitarte el rol de administrador.</mat-hint> }
        </mat-form-field>
        <mat-form-field class="span-2">
          <mat-label>Dirección</mat-label>
          <input matInput formControlName="direccion" />
        </mat-form-field>
      </form>
      @if (error(); as e) {
        <div class="err" role="alert"><strong>{{ e.title }}</strong> {{ e.detail }}</div>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button class="btn-cta" mat-flat-button (click)="guardar()" [disabled]="form.invalid || enviando()">{{ enviando() ? 'Guardando…' : u ? 'Guardar cambios' : 'Crear usuario' }}</button>
    </mat-dialog-actions>
  `,
  styles: `.err { margin-top: 8px; padding: 10px 12px; border: 1px solid var(--ca-danger); background: var(--ca-danger-soft); font-size: 14px; }`,
})
export class UsuarioDialogComponent {
  protected readonly u = inject<Usuario | null>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<UsuarioDialogComponent, Usuario>);
  private readonly api = inject(CiudadActivaApi);
  private readonly notify = inject(NotifyService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  protected readonly roles = ROLES;
  protected readonly rolLabel = ROL_LABEL;
  protected readonly esYo = !!this.u && this.u.id === this.auth.userId();
  protected readonly enviando = signal(false);
  protected readonly error = signal<ProblemDetail | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    nombre: [this.u?.nombre ?? '', [Validators.required, Validators.maxLength(120)]],
    email: [this.u?.email ?? '', [Validators.required, Validators.email]],
    password: ['', this.u ? [] : [Validators.required, Validators.minLength(6)]],
    telefono: [this.u?.telefono ?? ''],
    direccion: [this.u?.direccion ?? ''],
    rol: [(this.u?.rol ?? 'ROLE_OPERADOR') as RolNombre, Validators.required],
  });

  protected guardar(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    let op$: Observable<Usuario>;
    if (!this.u) {
      op$ = this.api.crearUsuario({ nombre: v.nombre.trim(), email: v.email.trim().toLowerCase(), password: v.password, telefono: v.telefono || null, direccion: v.direccion || null, rol: v.rol });
    } else {
      const u = this.u;
      op$ = this.api
        .actualizarUsuario(u.id, { nombre: v.nombre.trim(), email: v.email.trim().toLowerCase(), telefono: v.telefono, direccion: v.direccion })
        .pipe(switchMap((res) => (v.rol !== u.rol ? this.api.cambiarRol(u.id, v.rol) : of(res))));
    }
    op$.subscribe({
      next: (res) => {
        this.notify.ok(this.u ? 'Usuario actualizado.' : 'Usuario creado.');
        this.ref.close(res);
      },
      error: (e) => {
        this.enviando.set(false);
        this.error.set(aProblemDetail(e));
      },
    });
  }
}

/* ===================== Vista ===================== */
@Component({
  selector: 'app-usuarios',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTooltipModule,
    PageHeaderComponent,
    ViewStateComponent,
  ],
  providers: [{ provide: MatPaginatorIntl, useFactory: paginatorEs }],
  template: `
    <div class="page">
      <ca-page-header codigo="A-02" seccion="Administrador" titulo="Gestión de usuarios y roles" descripcion="Crea cuentas, ajusta datos y rol, y activa o desactiva accesos. La desactivación es lógica (activo = false): nunca se borra a nadie (RNF08).">
        <button class="btn-cta" mat-flat-button (click)="crear()"><mat-icon>person_add</mat-icon> Nuevo usuario</button>
      </ca-page-header>

      <div class="filters">
        <mat-form-field>
          <mat-label>Rol</mat-label>
          <mat-select [formControl]="fRol">
            <mat-option [value]="null">Todos</mat-option>
            @for (r of roles; track r) {
              <mat-option [value]="r">{{ rolLabel[r] }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field class="search">
          <mat-label>Buscar</mat-label>
          <input matInput [formControl]="fTexto" placeholder="Nombre, correo, teléfono…" />
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
      </div>

      <ca-view-state [estado]="usuarios.estado()" [error]="usuarios.error()" (reintentar)="usuarios.cargar()" tituloVacio="No hay usuarios con ese rol">
        <div class="ca-card ca-card--flush">
          <div class="table-scroll">
            <table mat-table [dataSource]="ds" matSort>
              <ng-container matColumnDef="nombre">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Usuario</th>
                <td mat-cell *matCellDef="let u">
                  <div class="strong">{{ u.nombre }} @if (u.id === yo()) { <span class="muted small">(tú)</span> }</div>
                  <div class="muted small">{{ u.email }}</div>
                </td>
              </ng-container>
              <ng-container matColumnDef="rol">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Rol</th>
                <td mat-cell *matCellDef="let u"><span class="badge" [style.color]="colorRol(u.rol)">{{ rolTxt(u) }}</span></td>
              </ng-container>
              <ng-container matColumnDef="telefono">
                <th mat-header-cell *matHeaderCellDef>Teléfono</th>
                <td mat-cell *matCellDef="let u">{{ u.telefono || '—' }}</td>
              </ng-container>
              <ng-container matColumnDef="labores">
                <th mat-header-cell *matHeaderCellDef>Labores activas</th>
                <td mat-cell *matCellDef="let u">{{ u.rol === 'ROLE_OPERADOR' ? (u.laboresActivas ?? 0) : '—' }}</td>
              </ng-container>
              <ng-container matColumnDef="creadoEn">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Alta</th>
                <td mat-cell *matCellDef="let u">{{ u.creadoEn | date: 'd MMM y' }}</td>
              </ng-container>
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Acceso</th>
                <td mat-cell *matCellDef="let u">
                  <mat-slide-toggle
                    [checked]="u.activo"
                    [disabled]="u.id === yo() || pendiente() === u.id"
                    (change)="toggle(u, $event.checked)"
                    [matTooltip]="u.id === yo() ? 'No puedes desactivar tu propia cuenta' : u.activo ? 'Desactivar (baja lógica)' : 'Reactivar'"
                  >{{ u.activo ? 'Activo' : 'Inactivo' }}</mat-slide-toggle>
                </td>
              </ng-container>
              <ng-container matColumnDef="acciones">
                <th mat-header-cell *matHeaderCellDef></th>
                <td mat-cell *matCellDef="let u" class="right"><button mat-icon-button (click)="editar(u)" matTooltip="Editar datos y rol" aria-label="Editar"><mat-icon>edit</mat-icon></button></td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="columnas; sticky: true"></tr>
              <tr mat-row *matRowDef="let row; columns: columnas" [class.row-off]="!row.activo"></tr>
              <tr class="mat-row" *matNoDataRow><td class="mat-cell nodata" [attr.colspan]="columnas.length">Sin coincidencias.</td></tr>
            </table>
          </div>
          <mat-paginator [pageSizeOptions]="[10, 25, 50]" [pageSize]="10" showFirstLastButtons />
        </div>
      </ca-view-state>
    </div>
  `,
  styles: `
    .search { width: 280px !important; }
    .filters { margin-bottom: 16px; }
    table { width: 100%; }
    .strong { font-weight: 600; }
    .small { font-size: 12px; }
    .right { text-align: right; }
    .row-off td { opacity: 0.55; }
    .row-off td:has(mat-slide-toggle) { opacity: 1; }
    .nodata { padding: 24px; text-align: center; color: var(--ca-on-surface-variant); }
  `,
})
export class UsuariosComponent {
  private readonly api = inject(CiudadActivaApi);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(NotifyService);
  private readonly auth = inject(AuthService);

  protected readonly roles = ROLES;
  protected readonly rolLabel = ROL_LABEL;
  protected readonly columnas = ['nombre', 'rol', 'telefono', 'labores', 'creadoEn', 'activo', 'acciones'];
  protected readonly yo = this.auth.userId;
  protected readonly pendiente = signal<number | null>(null);

  protected readonly fRol = new FormControl<RolNombre | null>(null);
  protected readonly fTexto = new FormControl('', { nonNullable: true });
  protected readonly usuarios = asyncState(() => this.api.usuarios(this.fRol.value));
  protected readonly ds = new MatTableDataSource<Usuario>([]);
  private readonly sort = viewChild(MatSort);
  private readonly paginator = viewChild(MatPaginator);

  constructor() {
    this.usuarios.cargar();
    this.fRol.valueChanges.subscribe(() => this.usuarios.cargar());
    this.fTexto.valueChanges.subscribe((t) => (this.ds.filter = t.trim().toLowerCase()));
    this.ds.filterPredicate = (u, f) => [u.nombre, u.email, u.telefono, ROL_LABEL[u.rol]].filter(Boolean).join(' ').toLowerCase().includes(f);
    this.ds.sortingDataAccessor = (u, c) => (c === 'activo' ? (u.activo ? 1 : 0) : ((u as unknown as Record<string, string>)[c] ?? ''));
    effect(() => {
      this.ds.data = this.usuarios.data() ?? [];
    });
    effect(() => {
      const s = this.sort();
      const p = this.paginator();
      untracked(() => {
        if (s) this.ds.sort = s;
        if (p) this.ds.paginator = p;
      });
    });
  }

  protected rolTxt(u: Usuario): string {
    return ROL_LABEL[u.rol] ?? u.rol;
  }

  protected colorRol(r: RolNombre): string {
    return r === 'ROLE_ADMIN' ? 'var(--ca-danger)' : r === 'ROLE_GESTOR' ? 'var(--ca-primary)' : r === 'ROLE_OPERADOR' ? '#3B6FD0' : 'var(--ca-on-surface-variant)';
  }

  protected crear(): void {
    this.dialog.open(UsuarioDialogComponent, { data: null, width: '600px', maxWidth: '95vw' }).afterClosed().pipe(filter(Boolean)).subscribe(() => this.usuarios.cargar());
  }

  protected editar(u: Usuario): void {
    this.dialog.open(UsuarioDialogComponent, { data: u, width: '600px', maxWidth: '95vw' }).afterClosed().pipe(filter(Boolean)).subscribe(() => this.usuarios.cargar());
  }

  /** RNF08: PATCH /api/admin/usuarios/{id}/estado, nunca DELETE */
  protected toggle(u: Usuario, activo: boolean): void {
    this.pendiente.set(u.id);
    this.api.estadoUsuario(u.id, activo).subscribe({
      next: (res) => {
        this.pendiente.set(null);
        this.usuarios.set((this.usuarios.data() ?? []).map((x) => (x.id === u.id ? { ...x, ...res } : x)));
        this.notify.ok(activo ? `${u.nombre} reactivado.` : `${u.nombre} desactivado (baja lógica).`);
      },
      error: (e) => {
        this.pendiente.set(null);
        // Revertir visualmente el interruptor
        this.usuarios.set((this.usuarios.data() ?? []).map((x) => (x.id === u.id ? { ...x } : x)));
        this.notify.error(e);
      },
    });
  }
}
