import { Component, computed, inject, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { ApiConfigService } from '../../core/api/api-config.service';
import { ROL_LABEL } from '../../core/api/labels';
import { RolNombre } from '../../core/api/models';
import { AuthService } from '../../core/auth/auth.service';

/** Pantalla 403 (CP02 y RoleGuard). */
@Component({
  selector: 'app-acceso-denegado',
  imports: [MatButtonModule, MatIconModule, RouterLink],
  template: `
    <div class="wrap">
      <div class="card">
        <div class="code">
          <span class="display">403</span>
          <span class="eyebrow">Acceso restringido</span>
        </div>
        @if (motivo() === 'movil') {
          <h1>Tu perfil opera en la app móvil</h1>
          <p>
            Ingresaste con un perfil de <strong>{{ rolTxt() }}</strong>. El portal web es exclusivo para los roles
            <strong>Gestor</strong> y <strong>Administrador</strong>. Desde la app CiudadActiva para Android puedes
            {{ rol() === 'ROLE_OPERADOR' ? 'consultar tus tareas asignadas, registrar avances y cerrar incidencias en campo' : 'reportar incidencias, seguir su estado y comentar' }}.
          </p>
          <p class="muted">Por seguridad no se guardó ninguna sesión en este navegador.</p>
          <div class="row row--wrap">
            <a class="btn-cta" mat-flat-button [href]="descarga()" target="_blank" rel="noopener">
              <mat-icon>android</mat-icon> Descargar la app móvil
            </a>
            <a mat-stroked-button routerLink="/login">Ingresar con otra cuenta</a>
          </div>
        } @else {
          <h1>Esta sección no corresponde a tu rol</h1>
          <p>
            Tu sesión está asociada al rol <strong>{{ rolSesion() }}</strong> y no tienes permisos para ver esta sección.
          </p>
          <div class="row row--wrap">
            @if (auth.autenticado()) {
              <a class="btn-cta" mat-flat-button [routerLink]="auth.rutaInicio()"><mat-icon>home</mat-icon> Ir a mi tablero</a>
              <button mat-stroked-button (click)="auth.logout()">Cerrar sesión</button>
            } @else {
              <a class="btn-cta" mat-flat-button routerLink="/login">Ir al inicio de sesión</a>
            }
          </div>
        }
      </div>
    </div>
  `,
  styles: `
    .wrap {
      min-height: 100vh;
      display: grid;
      place-items: center;
      padding: 24px;
      background: var(--ca-background);
    }
    .card {
      max-width: 620px;
      background: var(--ca-card);
      border: 1px solid var(--ca-border);
      outline: 1px solid var(--ca-border);
      outline-offset: 6px;
      padding: 36px;
    }
    .code {
      display: flex;
      align-items: baseline;
      gap: 14px;
      border-bottom: 2px solid var(--ca-on-surface-strong);
      padding-bottom: 12px;
      margin-bottom: 20px;
    }
    .code .display {
      font-size: 64px;
      font-weight: 800;
      line-height: 1;
      background: var(--ca-cta);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
    }
    h1 {
      margin-bottom: 12px;
    }
    .row {
      margin-top: 20px;
    }
  `,
})
export class AccesoDenegadoComponent {
  protected readonly auth = inject(AuthService);
  private readonly api = inject(ApiConfigService);
  motivo = input<string | undefined>();
  rol = input<string | undefined>();

  protected readonly rolTxt = computed(() => ROL_LABEL[this.rol() as RolNombre] ?? 'Ciudadano');
  protected readonly rolSesion = computed(() => {
    const r = this.auth.rol();
    return r ? ROL_LABEL[r] : 'sin sesión';
  });
  protected readonly descarga = computed(() => this.api.url(environment.appMovilPath));
}
