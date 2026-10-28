import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { categoriaEmoji, mensajeError } from '../../core/constants';
import { Categoria } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { SemaforoBadgeComponent } from '../../shared/chips.component';
import { IconComponent } from '../../shared/icon.component';
import { ModalComponent } from '../../shared/modal.component';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-admin-categorias',
  standalone: true,
  imports: [FormsModule, TopBarComponent, ModalComponent, SemaforoBadgeComponent, IconComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  template: `
    <app-top-bar title="Categorías Oficiales" [subtitle]="lista().length + ' categorías'">
      <button class="btn primary btn-sm" type="button" (click)="abrir(null)"><app-icon name="plus" [size]="14" /> Crear Nueva Categoría</button>
    </app-top-bar>
    <div class="page">
      @if (error()) { <app-error-view [message]="error()" (retried)="cargar()" /> }
      @if (cargando()) { <app-loading [n]="3" /> }
      @else if (lista().length === 0) { <app-empty emoji="🗂️" title="No hay categorías registradas." /> }
      @else {
        <div class="grid grid-auto">
          @for (c of lista(); track c.id) {
            <div class="card">
              <div class="row gap"><span class="cat-emoji">{{ emoji(c.icono) }}</span>
                <div class="grow"><strong>{{ c.nombre }}</strong><p class="muted xs">{{ c.descripcion }}</p></div><app-semaforo-badge [semaforo]="c.semaforo" [showLabel]="false" /></div>
              <div class="row between mt-sm"><small class="muted">{{ c.totalReportes ?? 0 }} reportes</small>
                <div class="row gap-sm"><button class="icon-btn" type="button" (click)="abrir(c)" aria-label="Editar"><app-icon name="edit" [size]="16" /></button>
                  <button class="icon-btn" type="button" (click)="aEliminar.set(c)" aria-label="Eliminar"><app-icon name="trash" [size]="16" /></button></div></div>
            </div>
          }
        </div>
      }
    </div>

    @if (modal()) {
      <app-modal [title]="editando() ? 'Editar Categoría' : 'Nueva Categoría'" (closed)="modal.set(false)">
        <label class="field"><span>Nombre</span><input type="text" [(ngModel)]="f.nombre" /></label>
        <label class="field"><span>Descripción</span><textarea rows="3" [(ngModel)]="f.descripcion"></textarea></label>
        <label class="field"><span>Ícono</span><select [(ngModel)]="f.icono">@for (i of iconos; track i) { <option [value]="i">{{ emoji(i) }} {{ i }}</option> }</select></label>
        @if (errorForm()) { <small class="field-error">{{ errorForm() }}</small> }
        <ng-container footer>
          <button class="btn ghost" type="button" (click)="modal.set(false)">Cancelar</button>
          <button class="btn primary" type="button" [disabled]="guardando()" (click)="guardar()"><app-icon name="save" [size]="16" /> Guardar</button>
        </ng-container>
      </app-modal>
    }
    @if (aEliminar(); as c) {
      <app-modal title="¿Eliminar categoría?" (closed)="aEliminar.set(null)">
        <p>Se eliminará la categoría <strong>{{ c.nombre }}</strong>. Si tiene reportes asociados el servidor puede rechazar la operación.</p>
        <ng-container footer>
          <button class="btn ghost" type="button" (click)="aEliminar.set(null)">Cancelar</button>
          <button class="btn danger" type="button" (click)="eliminar()">Eliminar</button>
        </ng-container>
      </app-modal>
    }`,
})
export class AdminCategoriasComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  emoji = categoriaEmoji;
  iconos = ['road', 'lightbulb', 'trash', 'park', 'water', 'traffic'];
  lista = signal<Categoria[]>([]);
  cargando = signal(true);
  error = signal('');
  modal = signal(false);
  editando = signal<Categoria | null>(null);
  aEliminar = signal<Categoria | null>(null);
  guardando = signal(false);
  errorForm = signal('');
  f = { nombre: '', descripcion: '', icono: 'road' };

  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.api.listarCategorias().subscribe({
      next: (l) => { this.lista.set(l ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set('Error al cargar categorías: ' + mensajeError(e)); this.cargando.set(false); },
    });
  }
  abrir(c: Categoria | null): void {
    this.editando.set(c);
    this.f = { nombre: c?.nombre ?? '', descripcion: c?.descripcion ?? '', icono: c?.icono ?? 'road' };
    this.errorForm.set(''); this.modal.set(true);
  }
  guardar(): void {
    if (!this.f.nombre.trim()) { this.errorForm.set('El nombre es obligatorio.'); return; }
    const body = { nombre: this.f.nombre.trim(), descripcion: this.f.descripcion.trim() || null, icono: this.f.icono };
    const e = this.editando();
    this.guardando.set(true);
    (e ? this.api.actualizarCategoria(e.id, body) : this.api.crearCategoria(body)).subscribe({
      next: () => { this.guardando.set(false); this.modal.set(false); this.toast.ok('Categoría guardada.'); this.cargar(); },
      error: (er) => { this.guardando.set(false); this.errorForm.set(mensajeError(er)); },
    });
  }
  eliminar(): void {
    const c = this.aEliminar(); if (!c) return;
    this.api.eliminarCategoria(c.id).subscribe({
      next: () => { this.aEliminar.set(null); this.toast.ok('Categoría eliminada.'); this.cargar(); },
      error: (e) => { this.aEliminar.set(null); this.toast.error(mensajeError(e)); },
    });
  }
}
