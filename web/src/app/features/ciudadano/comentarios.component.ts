import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { ROL_LABEL, mensajeError } from '../../core/constants';
import { Comentario, Rol } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { ToastService } from '../../core/toast.service';
import { IconComponent } from '../../shared/icon.component';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-comentarios',
  standalone: true,
  imports: [DatePipe, FormsModule, TopBarComponent, IconComponent, EmptyComponent, LoadingComponent, ErrorViewComponent],
  template: `
    <app-top-bar title="Comentarios" [subtitle]="'Reporte #' + id()" [back]="true" [fallback]="'/ciudadano/seguimiento/' + id()" />
    <div class="page page-narrow">
      @if (error()) { <app-error-view [message]="error()" (retried)="cargar()" /> }
      @if (cargando()) { <app-loading [n]="3" /> }
      @else if (lista().length === 0) { <app-empty emoji="💬" title="Aún no hay comentarios" text="Escribe el primero abajo." /> }
      @else {
        @for (c of lista(); track c.id) {
          <div class="card mb">
            <div class="row between gap-sm">
              <div class="row gap-sm"><div class="avatar" style="width:32px;height:32px;font-size:13px">{{ c.usuarioNombre.charAt(0).toUpperCase() }}</div>
                <div><strong class="small">{{ c.usuarioNombre }}</strong><small class="muted"> ({{ rolLabel(c.usuarioRol) }})</small><br /><small class="muted xs">{{ c.creadoEn | date: 'dd MMM yyyy, HH:mm' }}</small></div></div>
              @if (puedeEliminar(c)) { <button class="icon-btn" type="button" (click)="eliminar(c)" aria-label="Eliminar"><app-icon name="trash" [size]="16" /></button> }
            </div>
            <p class="mt-sm">{{ c.contenido }}</p>
          </div>
        }
      }
      <h2 class="section-title">Escribir retroalimentación</h2>
      <textarea rows="3" [(ngModel)]="texto" placeholder="Escribe una nota o agradecimiento..."></textarea>
      <button class="btn primary block mt" type="button" [disabled]="enviando() || !texto.trim()" (click)="enviar()"><app-icon name="send" [size]="16" /> Enviar</button>
    </div>`,
})
export class ComentariosComponent implements OnInit {
  id = input.required<string>();
  private api = inject(ApiService);
  private session = inject(SessionService);
  private toast = inject(ToastService);
  lista = signal<Comentario[]>([]);
  cargando = signal(true);
  error = signal('');
  enviando = signal(false);
  texto = '';

  ngOnInit(): void { this.cargar(); }
  rolLabel(r?: string | null): string { return r ? (ROL_LABEL[r as Rol] ?? r.replace('ROLE_', '')) : 'Usuario'; }
  puedeEliminar(c: Comentario): boolean { return c.usuarioId === this.session.user()?.id || this.session.rol() === 'ROLE_ADMIN'; }

  cargar(): void {
    this.cargando.set(true);
    this.api.listarComentarios(+this.id()).subscribe({
      next: (l) => { this.lista.set(l ?? []); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  enviar(): void {
    this.enviando.set(true);
    this.api.agregarComentario(+this.id(), this.texto.trim()).subscribe({
      next: () => { this.texto = ''; this.enviando.set(false); this.cargar(); },
      error: (e) => { this.enviando.set(false); this.toast.error(mensajeError(e)); },
    });
  }
  eliminar(c: Comentario): void {
    this.api.eliminarComentario(c.id).subscribe({
      next: () => this.cargar(),
      error: (e) => this.toast.error('No se pudo eliminar el comentario: ' + mensajeError(e)),
    });
  }
}
