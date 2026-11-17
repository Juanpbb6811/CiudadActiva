import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { filter } from 'rxjs';
import { CiudadActivaApi } from '../../core/api/ciudad-activa.api';
import { ICONO_CATEGORIA, iconoCategoria } from '../../core/api/labels';
import { Categoria } from '../../core/api/models';
import { ProblemDetail, aProblemDetail } from '../../core/http/problem-detail';
import { asyncState } from '../../core/ui/async-state';
import { NotifyService } from '../../core/ui/notify.service';
import { PageHeaderComponent, ViewStateComponent } from '../../shared/ui.components';

/* ===================== Modal crear / editar categoría ===================== */
@Component({
  selector: 'app-categoria-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatIconModule, MatSlideToggleModule],
  template: `
    <h2 mat-dialog-title>{{ c ? 'Editar categoría' : 'Nueva categoría' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="f">
        <mat-form-field class="full">
          <mat-label>Nombre</mat-label>
          <input matInput formControlName="nombre" maxlength="80" />
          <mat-hint align="end">{{ form.controls.nombre.value.length }}/80</mat-hint>
          <mat-error>Entre 2 y 80 caracteres.</mat-error>
        </mat-form-field>
        <mat-form-field class="full">
          <mat-label>Descripción</mat-label>
          <textarea matInput formControlName="descripcion" rows="3" maxlength="255"></textarea>
        </mat-form-field>
        <div>
          <div class="eyebrow lbl">Ícono</div>
          <div class="icons" role="radiogroup" aria-label="Ícono de la categoría">
            @for (i of iconos; track i.clave) {
              <button type="button" class="ic" role="radio" [attr.aria-checked]="form.controls.icono.value === i.clave" [class.ic--on]="form.controls.icono.value === i.clave" (click)="form.controls.icono.setValue(i.clave)" [attr.title]="i.clave">
                <mat-icon>{{ i.simbolo }}</mat-icon>
                <span class="mono">{{ i.clave }}</span>
              </button>
            }
          </div>
          <p class="muted small">Claves compatibles con la app Android.</p>
        </div>
        <mat-slide-toggle formControlName="activa">Categoría activa (visible para ciudadanos)</mat-slide-toggle>
      </form>
      @if (error(); as e) {
        <div class="err" role="alert"><strong>{{ e.title }}</strong> {{ e.detail }}</div>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button class="btn-cta" mat-flat-button (click)="guardar()" [disabled]="form.invalid || enviando()">{{ enviando() ? 'Guardando…' : 'Guardar' }}</button>
    </mat-dialog-actions>
  `,
  styles: `
    .f { display: flex; flex-direction: column; gap: 14px; }
    .lbl { margin-bottom: 8px; }
    .icons { display: grid; grid-template-columns: repeat(auto-fill, minmax(84px, 1fr)); gap: 6px; }
    .ic { display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 10px 4px; border: 1px solid var(--ca-border); background: var(--ca-input); color: var(--ca-on-surface); cursor: pointer; font: inherit; font-size: 11px; }
    .ic--on { outline: 2px solid var(--ca-sage); background: var(--ca-card); color: var(--ca-primary); }
    .small { font-size: 12px; margin: 6px 0 0; }
    .err { margin-top: 8px; padding: 10px 12px; border: 1px solid var(--ca-danger); background: var(--ca-danger-soft); font-size: 14px; }
  `,
})
export class CategoriaDialogComponent {
  protected readonly c = inject<Categoria | null>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<CategoriaDialogComponent, Categoria>);
  private readonly api = inject(CiudadActivaApi);
  private readonly notify = inject(NotifyService);
  private readonly fb = inject(FormBuilder);

  protected readonly iconos = Object.entries(ICONO_CATEGORIA).map(([clave, simbolo]) => ({ clave, simbolo }));
  protected readonly enviando = signal(false);
  protected readonly error = signal<ProblemDetail | null>(null);
  protected readonly form = this.fb.nonNullable.group({
    nombre: [this.c?.nombre ?? '', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    descripcion: [this.c?.descripcion ?? ''],
    icono: [this.c?.icono ?? 'tag'],
    activa: [this.c?.activa ?? true],
  });

  protected guardar(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    const body = { nombre: v.nombre.trim(), descripcion: v.descripcion.trim() || null, icono: v.icono, activa: v.activa };
    this.enviando.set(true);
    this.error.set(null);
    (this.c ? this.api.actualizarCategoria(this.c.id, body) : this.api.crearCategoria(body)).subscribe({
      next: (res) => {
        this.notify.ok(this.c ? 'Categoría actualizada.' : 'Categoría creada.');
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
  selector: 'app-categorias',
  imports: [MatButtonModule, MatButtonToggleModule, MatIconModule, MatSlideToggleModule, MatTooltipModule, PageHeaderComponent, ViewStateComponent],
  template: `
    <div class="page">
      <ca-page-header codigo="A-03" seccion="Administrador" titulo="Configuración de categorías" descripcion="Tipos de incidencia que el ciudadano puede reportar. Desactivar oculta la categoría para nuevos reportes sin afectar los existentes (baja lógica · RNF08).">
        <mat-button-toggle-group [value]="filtro()" (change)="filtro.set($event.value)" hideSingleSelectionIndicator aria-label="Filtrar">
          <mat-button-toggle value="todas">Todas</mat-button-toggle>
          <mat-button-toggle value="activas">Activas</mat-button-toggle>
          <mat-button-toggle value="inactivas">Inactivas</mat-button-toggle>
        </mat-button-toggle-group>
        <button class="btn-cta" mat-flat-button (click)="abrir(null)"><mat-icon>add</mat-icon> Nueva categoría</button>
      </ca-page-header>

      <ca-view-state [estado]="cats.estado()" [error]="cats.error()" (reintentar)="cats.cargar()" tituloVacio="Sin categorías" textoVacio="Crea la primera categoría de incidencia.">
        <div class="grid">
          @for (c of visibles(); track c.id) {
            <article class="ca-card cat" [class.cat--off]="!c.activa">
              <div class="cat__top">
                <span class="eyebrow">Categoría</span>
                <mat-slide-toggle [checked]="c.activa" [disabled]="pendiente() === c.id" (change)="toggle(c, $event.checked)" [matTooltip]="c.activa ? 'Desactivar (baja lógica)' : 'Reactivar'" [attr.aria-label]="'Activar ' + c.nombre" />
              </div>
              <div class="cat__icon"><mat-icon>{{ icono(c.icono) }}</mat-icon></div>
              <h3>{{ c.nombre }}</h3>
              <p class="muted cat__desc">{{ c.descripcion || 'Sin descripción' }}</p>
              <div class="cat__foot">
                <span><b class="kpi-value cat__n">{{ c.totalReportes ?? 0 }}</b> <span class="muted">reportes</span></span>
                <span class="badge" [style.color]="c.activa ? 'var(--ca-primary)' : 'var(--ca-on-surface-variant)'">{{ c.activa ? 'Activa' : 'Inactiva' }}</span>
                <button mat-icon-button (click)="abrir(c)" matTooltip="Editar" aria-label="Editar categoría"><mat-icon>edit</mat-icon></button>
              </div>
            </article>
          } @empty {
            <p class="muted">No hay categorías {{ filtro() }}.</p>
          }
        </div>
      </ca-view-state>
    </div>
  `,
  styles: `
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
    .cat { display: flex; flex-direction: column; gap: 6px; }
    .cat--off { background: var(--ca-input); }
    .cat--off h3, .cat--off .cat__icon { opacity: 0.55; }
    .cat__top { display: flex; justify-content: space-between; align-items: center; }
    .cat__icon { width: 52px; height: 52px; display: grid; place-items: center; border: 1px solid var(--ca-border); background: var(--ca-accent-soft); color: var(--ca-primary); margin: 8px 0 6px; }
    .cat__icon mat-icon { font-size: 28px; width: 28px; height: 28px; }
    .cat__desc { font-size: 14px; flex: 1; margin: 0; }
    .cat__foot { display: flex; align-items: center; gap: 10px; border-top: 1px solid var(--ca-border); padding-top: 10px; margin-top: 8px; }
    .cat__foot > span:first-child { flex: 1; }
    .cat__n { font-size: 22px; margin-right: 6px; }
  `,
})
export class CategoriasComponent {
  private readonly api = inject(CiudadActivaApi);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(NotifyService);

  protected readonly cats = asyncState(() => this.api.categoriasTodas());
  protected readonly filtro = signal<'todas' | 'activas' | 'inactivas'>('todas');
  protected readonly pendiente = signal<number | null>(null);
  protected readonly visibles = computed(() => {
    const f = this.filtro();
    return (this.cats.data() ?? []).filter((c) => f === 'todas' || (f === 'activas' ? c.activa : !c.activa)).sort((a, b) => a.nombre.localeCompare(b.nombre));
  });

  constructor() {
    this.cats.cargar();
  }

  protected icono = iconoCategoria;

  protected abrir(c: Categoria | null): void {
    this.dialog.open(CategoriaDialogComponent, { data: c, width: '560px', maxWidth: '95vw' }).afterClosed().pipe(filter(Boolean)).subscribe(() => this.cats.cargar());
  }

  /** RNF08: PATCH /api/categorias/{id}/estado, nunca DELETE */
  protected toggle(c: Categoria, activa: boolean): void {
    this.pendiente.set(c.id);
    this.api.estadoCategoria(c.id, activa).subscribe({
      next: (res) => {
        this.pendiente.set(null);
        this.cats.set((this.cats.data() ?? []).map((x) => (x.id === c.id ? { ...x, ...res } : x)));
        this.notify.ok(activa ? `“${c.nombre}” reactivada.` : `“${c.nombre}” desactivada.`);
      },
      error: (e) => {
        this.pendiente.set(null);
        this.cats.set((this.cats.data() ?? []).map((x) => (x.id === c.id ? { ...x } : x)));
        this.notify.error(e);
      },
    });
  }
}
