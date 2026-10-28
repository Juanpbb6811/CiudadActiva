import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { ROLES, ROL_LABEL, mensajeError } from '../../core/constants';
import { Rol, Usuario } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { IconComponent } from '../../shared/icon.component';
import { ModalComponent } from '../../shared/modal.component';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [FormsModule, TopBarComponent, ModalComponent, IconComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  template: `
    <app-top-bar title="Directorio de Usuarios" [subtitle]="lista().length + ' usuarios'">
      <button class="btn primary btn-sm" type="button" (click)="abrirNuevo()"><app-icon name="plus" [size]="14" /> Crear Nuevo Usuario</button>
    </app-top-bar>
    <div class="page">
      <div class="pills mb">
        <button type="button" class="pill" [class.active]="filtro() === 'TODOS'" (click)="setFiltro('TODOS')">TODOS</button>
        @for (r of roles; track r) { <button type="button" class="pill" [class.active]="filtro() === r" (click)="setFiltro(r)">{{ rolLabel[r] }}</button> }
      </div>
      @if (error()) { <app-error-view [message]="error()" (retried)="cargar()" /> }
      @if (cargando()) { <app-loading [n]="4" /> }
      @else if (lista().length === 0) { <app-empty emoji="👥" title="No se encontraron usuarios registrados en este rol." /> }
      @else {
        <div class="grid grid-auto">
          @for (u of lista(); track u.id) {
            <div class="card">
              <div class="row gap">
                <div class="avatar">{{ u.nombre.charAt(0).toUpperCase() }}</div>
                <div class="grow" style="min-width:0"><strong>{{ u.nombre }}</strong><p class="muted xs" style="overflow:hidden;text-overflow:ellipsis">{{ u.email }}</p></div>
                <span class="chip" [style.background]="u.activo ? '#e8f5e9' : '#eeeeee'" [style.color]="u.activo ? '#2e7d32' : '#616161'" [style.border-color]="u.activo ? '#a5d6a7' : '#bdbdbd'">{{ u.activo ? 'Activo' : 'Inactivo' }}</span>
              </div>
              <div class="row between mt-sm wrap gap-sm"><small class="kicker">{{ rolLabel[u.rol] }}</small>@if (u.telefono) { <small class="muted">Tel: {{ u.telefono }}</small> }</div>
              @if (u.activo) { <button class="btn danger-ghost btn-sm mt-sm" type="button" (click)="aDesactivar.set(u)">Desactivar</button> }
            </div>
          }
        </div>
      }
    </div>

    @if (nuevo()) {
      <app-modal title="Crear Usuario Administrativo" (closed)="nuevo.set(false)">
        <label class="field"><span>Nombre</span><input type="text" [(ngModel)]="f.nombre" /></label>
        <label class="field"><span>Correo</span><input type="email" [(ngModel)]="f.email" /></label>
        <label class="field"><span>Contraseña</span><input type="password" [(ngModel)]="f.password" autocomplete="new-password" /></label>
        <label class="field"><span>Teléfono (opcional)</span><input type="tel" [(ngModel)]="f.telefono" /></label>
        <label class="field"><span>Rol</span><select [(ngModel)]="f.rol">@for (r of roles; track r) { <option [value]="r">{{ rolLabel[r] }}</option> }</select></label>
        @if (errorForm()) { <small class="field-error">{{ errorForm() }}</small> }
        <ng-container footer>
          <button class="btn ghost" type="button" (click)="nuevo.set(false)">Cancelar</button>
          <button class="btn primary" type="button" [disabled]="guardando()" (click)="crear()">Crear Usuario</button>
        </ng-container>
      </app-modal>
    }
    @if (aDesactivar(); as u) {
      <app-modal title="¿Desactivar usuario?" (closed)="aDesactivar.set(null)">
        <p>El usuario <strong>{{ u.nombre }}</strong> ya no podrá iniciar sesión.</p>
        <ng-container footer>
          <button class="btn ghost" type="button" (click)="aDesactivar.set(null)">Cancelar</button>
          <button class="btn danger" type="button" [disabled]="guardando()" (click)="desactivar()">Desactivar</button>
        </ng-container>
      </app-modal>
    }`,
})
export class AdminUsuariosComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  roles = ROLES; rolLabel = ROL_LABEL;
  lista = signal<Usuario[]>([]);
  filtro = signal<string>('TODOS');
  cargando = signal(true);
  error = signal('');
  nuevo = signal(false);
  aDesactivar = signal<Usuario | null>(null);
  guardando = signal(false);
  errorForm = signal('');
  f: { nombre: string; email: string; password: string; telefono: string; rol: Rol } = { nombre: '', email: '', password: '', telefono: '', rol: 'ROLE_OPERADOR' };

  ngOnInit(): void { this.cargar(); }
  setFiltro(f: string): void { this.filtro.set(f); this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.api.listarUsuarios(this.filtro()).subscribe({
      next: (l) => { this.lista.set(l ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set('Error al cargar usuarios: ' + mensajeError(e)); this.cargando.set(false); },
    });
  }
  abrirNuevo(): void { this.f = { nombre: '', email: '', password: '', telefono: '', rol: 'ROLE_OPERADOR' }; this.errorForm.set(''); this.nuevo.set(true); }
  crear(): void {
    const { nombre, email, password, telefono, rol } = this.f;
    if (!nombre.trim() || !email.trim() || password.length < 6) { this.errorForm.set('Nombre, correo y contraseña (mín. 6 caracteres) son obligatorios.'); return; }
    this.guardando.set(true);
    this.api.crearUsuario({ nombre: nombre.trim(), email: email.trim().toLowerCase(), password, telefono: telefono.trim() || null, rol }).subscribe({
      next: () => { this.guardando.set(false); this.nuevo.set(false); this.toast.ok('Usuario creado.'); this.cargar(); },
      error: (e) => { this.guardando.set(false); this.errorForm.set(mensajeError(e)); },
    });
  }
  desactivar(): void {
    const u = this.aDesactivar(); if (!u) return;
    this.guardando.set(true);
    this.api.desactivarUsuario(u.id).subscribe({
      next: () => { this.guardando.set(false); this.aDesactivar.set(null); this.toast.ok('Usuario desactivado.'); this.cargar(); },
      error: (e) => { this.guardando.set(false); this.aDesactivar.set(null); this.toast.error(mensajeError(e)); },
    });
  }
}
