import { DatePipe } from '@angular/common';
import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ApiConfigService } from '../core/api/api-config.service';
import { CiudadActivaApi } from '../core/api/ciudad-activa.api';
import { ESTADO_LABEL, ROL_LABEL, iconoCategoria } from '../core/api/labels';
import { Comentario, EstadoReporte, RolNombre } from '../core/api/models';
import { asyncState } from '../core/ui/async-state';
import { NotifyService } from '../core/ui/notify.service';
import { EstadoBadgeComponent, PrioridadBadgeComponent, ViewStateComponent } from './ui.components';

/**
 * Ficha de un reporte: foto, metadatos, historial de seguimiento (SOLO LECTURA, RN05)
 * y comentarios (POST /api/reportes/{id}/comentarios) cuando `permitirComentar` es true.
 */
@Component({
  selector: 'ca-reporte-detalle',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    ViewStateComponent,
    EstadoBadgeComponent,
    PrioridadBadgeComponent,
  ],
  template: `
    <div class="det">
      <div class="det__bar">
        <span class="eyebrow">Detalle del reporte</span>
        <span class="spacer"></span>
        @if (cerrable()) {
          <button mat-icon-button (click)="cerrar.emit()" aria-label="Cerrar ficha"><mat-icon>close</mat-icon></button>
        }
      </div>

      <ca-view-state [estado]="detalle.estado()" [error]="detalle.error()" (reintentar)="detalle.cargar()">
        @if (detalle.data(); as r) {
          <figure class="det__foto">
            @if (fotoUrl() && !fotoFallo()) {
              <img [src]="fotoUrl()" [alt]="'Fotografía del reporte ' + r.codigoSeguimiento" (error)="fotoFallo.set(true)" />
            } @else {
              <div class="det__sinfoto"><mat-icon>image_not_supported</mat-icon><span>Sin fotografía</span></div>
            }
            <figcaption class="mono">{{ r.codigoSeguimiento }}</figcaption>
          </figure>

          <div class="det__body">
            <h2>{{ r.titulo }}</h2>
            <div class="row row--wrap det__badges">
              <ca-estado-badge [estado]="r.estado" />
              <ca-prioridad-badge [prioridad]="r.prioridad" />
            </div>

            <dl class="det__meta">
              <dt>Categoría</dt>
              <dd><mat-icon inline>{{ icono(r.categoriaIcono) }}</mat-icon> {{ r.categoriaNombre }}</dd>
              <dt>Registrado</dt>
              <dd>{{ r.creadoEn | date: 'd MMM y, h:mm a' }}</dd>
              <dt>Ubicación</dt>
              <dd>{{ r.direccion || 'Sin dirección' }}@if (r.barrio) {, {{ r.barrio }}}</dd>
              <dt>Ciudadano</dt>
              <dd>{{ r.ciudadanoNombre || '—' }}</dd>
              <dt>Operador</dt>
              <dd>{{ r.asignacionActual?.operadorNombre || r.operadorAsignadoNombre || 'Sin asignar' }}</dd>
              @if (r.fechaCierre) {
                <dt>Cierre</dt>
                <dd>{{ r.fechaCierre | date: 'd MMM y, h:mm a' }}</dd>
              }
            </dl>

            <p class="det__desc">{{ r.descripcion }}</p>

            @if (r.justificacionRechazo) {
              <div class="det__note det__note--danger">
                <div class="eyebrow">Motivo del rechazo</div>
                {{ r.justificacionRechazo }}
              </div>
            }
            @if (r.analisisIaDescripcion) {
              <div class="det__note">
                <div class="eyebrow">Análisis automático (sugerencia)</div>
                {{ r.analisisIaDescripcion }}
                @if (r.analisisIaPrioridadSugerida) {
                  <div class="muted">Prioridad sugerida: {{ r.analisisIaPrioridadSugerida }} · Urgencia {{ r.analisisIaNivelUrgencia ?? '—' }}/5</div>
                }
              </div>
            }

            <section class="det__sec">
              <div class="det__sec-head">
                <h3>Historial de seguimiento</h3>
                <span class="eyebrow">Solo lectura</span>
              </div>
              @if (r.lineaDeTiempo.length === 0) {
                <p class="muted">Aún no hay eventos registrados.</p>
              } @else {
                <ol class="tl">
                  @for (s of r.lineaDeTiempo; track s.id) {
                    <li class="tl__item">
                      <div class="tl__dot"></div>
                      <div>
                        <div class="tl__head">
                          <strong>{{ accion(s.accion) }}</strong>
                          <span class="muted">{{ s.creadoEn | date: 'd MMM y, h:mm a' }}</span>
                        </div>
                        @if (s.estadoAnterior !== s.estadoNuevo) {
                          <div class="tl__estados mono">{{ estadoTxt(s.estadoAnterior) }} → {{ estadoTxt(s.estadoNuevo) }}</div>
                        }
                        <div class="tl__obs">{{ s.observaciones }}</div>
                        <div class="muted tl__who">{{ s.usuarioNombre }} · {{ rolTxt(s.usuarioRol) }}</div>
                      </div>
                    </li>
                  }
                </ol>
              }
            </section>

            <section class="det__sec">
              <div class="det__sec-head">
                <h3>Comentarios</h3>
                <span class="eyebrow">{{ comentarios().length }}</span>
              </div>
              @for (c of comentarios(); track c.id) {
                <div class="cm">
                  <div class="cm__head">
                    <strong>{{ c.usuarioNombre }}</strong>
                    <span class="muted">{{ rolTxt(c.usuarioRol) }} · {{ c.creadoEn | date: 'd MMM, h:mm a' }}</span>
                  </div>
                  <div>{{ c.contenido }}</div>
                </div>
              } @empty {
                <p class="muted">Sin comentarios todavía.</p>
              }

              @if (permitirComentar()) {
                <form class="cm__form" (submit)="$event.preventDefault(); comentar()">
                  <mat-form-field class="full">
                    <mat-label>Escribe un comentario</mat-label>
                    <textarea matInput [formControl]="texto" rows="3" maxlength="1000"></textarea>
                    <mat-hint align="end">{{ texto.value.length || 0 }}/1000</mat-hint>
                    @if (texto.hasError('minlength')) {
                      <mat-error>Mínimo 2 caracteres.</mat-error>
                    }
                  </mat-form-field>
                  <button class="btn-cta" mat-flat-button type="submit" [disabled]="texto.invalid || enviando()">
                    <mat-icon>send</mat-icon> {{ enviando() ? 'Enviando…' : 'Comentar' }}
                  </button>
                </form>
              }
            </section>
          </div>
        }
      </ca-view-state>
    </div>
  `,
  styles: `
    .det {
      display: flex;
      flex-direction: column;
      min-height: 100%;
      background: var(--ca-card);
    }
    .det__bar {
      display: flex;
      align-items: center;
      padding: 8px 8px 8px 20px;
      border-bottom: 1px solid var(--ca-border);
      position: sticky;
      top: 0;
      background: var(--ca-card);
      z-index: 2;
    }
    .det__foto {
      margin: 0;
      position: relative;
      background: var(--ca-input);
      border-bottom: 1px solid var(--ca-border);
    }
    .det__foto img {
      display: block;
      width: 100%;
      max-height: 280px;
      object-fit: cover;
    }
    .det__foto figcaption {
      position: absolute;
      left: 12px;
      bottom: 12px;
      padding: 3px 8px;
      background: var(--ca-card);
      border: 1px solid var(--ca-border);
    }
    .det__sinfoto {
      height: 150px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 6px;
      color: var(--ca-on-surface-variant);
    }
    .det__body {
      padding: 20px;
    }
    .det__badges {
      margin: 10px 0 16px;
    }
    .det__meta {
      display: grid;
      grid-template-columns: 110px 1fr;
      gap: 6px 12px;
      margin: 0 0 16px;
      font-size: 14px;
    }
    .det__meta dt {
      color: var(--ca-on-surface-variant);
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      padding-top: 2px;
    }
    .det__meta dd {
      margin: 0;
    }
    .det__desc {
      white-space: pre-line;
    }
    .det__note {
      border: 1px solid var(--ca-border);
      background: var(--ca-input);
      padding: 12px 14px;
      margin: 12px 0;
      font-size: 14px;
    }
    .det__note--danger {
      border-color: var(--ca-danger);
      background: var(--ca-danger-soft);
    }
    .det__sec {
      margin-top: 24px;
    }
    .det__sec-head {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      border-bottom: 1px solid var(--ca-border);
      padding-bottom: 8px;
      margin-bottom: 12px;
    }
    .tl {
      list-style: none;
      margin: 0;
      padding: 0 0 0 6px;
      border-left: 1px solid var(--ca-border);
    }
    .tl__item {
      display: grid;
      grid-template-columns: 14px 1fr;
      gap: 10px;
      padding: 0 0 16px;
      margin-left: -7px;
    }
    .tl__dot {
      width: 9px;
      height: 9px;
      margin-top: 6px;
      background: var(--ca-sage);
      outline: 2px solid var(--ca-card);
    }
    .tl__head {
      display: flex;
      flex-wrap: wrap;
      gap: 4px 10px;
      align-items: baseline;
      font-size: 14px;
    }
    .tl__estados {
      margin-top: 2px;
      color: var(--ca-primary);
    }
    .tl__obs {
      font-size: 14px;
      margin-top: 2px;
    }
    .tl__who {
      font-size: 12px;
    }
    .cm {
      border-left: 3px solid var(--ca-sand);
      padding: 6px 0 6px 12px;
      margin-bottom: 12px;
      font-size: 14px;
    }
    .cm__head {
      display: flex;
      flex-wrap: wrap;
      gap: 4px 10px;
      align-items: baseline;
    }
    .cm__form {
      margin-top: 12px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      align-items: flex-end;
    }
  `,
})
export class ReporteDetalleComponent {
  private readonly api = inject(CiudadActivaApi);
  private readonly cfg = inject(ApiConfigService);
  private readonly notify = inject(NotifyService);

  reporteId = input.required<number>();
  permitirComentar = input(false);
  cerrable = input(true);
  cerrar = output<void>();

  protected readonly detalle = asyncState(() => this.api.detalle(this.reporteId()), () => false);
  protected readonly comentariosExtra = signal<Comentario[]>([]);
  protected readonly comentarios = computed(() => [...(this.detalle.data()?.comentarios ?? []), ...this.comentariosExtra()]);
  protected readonly fotoFallo = signal(false);
  protected readonly enviando = signal(false);
  protected readonly texto = new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(2)] });

  protected readonly fotoUrl = computed(() => {
    const r = this.detalle.data();
    if (!r) return null;
    if (r.tieneFoto) return this.cfg.fotoReporte(r.id);
    if (r.imagenUrl && /^(https?:|data:image)/.test(r.imagenUrl)) return r.imagenUrl;
    return null;
  });

  constructor() {
    effect(() => {
      this.reporteId();
      untracked(() => {
        this.comentariosExtra.set([]);
        this.fotoFallo.set(false);
        this.detalle.cargar();
      });
    });
  }

  protected comentar(): void {
    if (this.texto.invalid) return;
    this.enviando.set(true);
    this.api.comentar(this.reporteId(), this.texto.value.trim()).subscribe({
      next: (c) => {
        this.comentariosExtra.update((l) => [...l, c]);
        this.texto.reset('');
        this.enviando.set(false);
        this.notify.ok('Comentario publicado.');
      },
      error: (e) => {
        this.enviando.set(false);
        this.notify.error(e);
      },
    });
  }

  protected icono = iconoCategoria;
  protected estadoTxt(e?: EstadoReporte | null): string {
    return e ? ESTADO_LABEL[e] : 'Inicio';
  }
  protected rolTxt(r?: string | null): string {
    return (r && ROL_LABEL[r as RolNombre]) || r || '';
  }
  protected accion(a: string): string {
    return (a || '').replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
  }
}
