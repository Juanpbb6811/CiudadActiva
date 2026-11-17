import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { ApiConfigService } from '../../core/api/api-config.service';
import { AuthService } from '../../core/auth/auth.service';
import { ProblemDetail, aProblemDetail } from '../../core/http/problem-detail';
import { ThemeService } from '../../core/ui/theme.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatSelectModule, MatTooltipModule],
  template: `
    <div class="login">
      <aside class="plate" aria-hidden="true">
        <div class="plate__frame">
          <div class="plate__logo">
            <img src="logo-ciudadactiva.svg" alt="" width="220" height="220" />
          </div>
          <div class="plate__label">
            <div class="plate__title">CiudadActiva</div>
            <div class="plate__sub">Portal de gestión de incidencias urbanas</div>
          </div>
        </div>
      </aside>

      <main class="panel">
        <div class="panel__top">
          <span class="eyebrow">Portal web</span>
          <button mat-icon-button (click)="theme.alternar()" [matTooltip]="theme.tema() === 'dark' ? 'Modo claro' : 'Modo oscuro'" aria-label="Cambiar tema">
            <mat-icon>{{ theme.tema() === 'dark' ? 'light_mode' : 'dark_mode' }}</mat-icon>
          </button>
        </div>

        <form class="form" [formGroup]="form" (ngSubmit)="ingresar()">
          <h1>Ingreso al portal</h1>
          <p class="muted">Acceso exclusivo para perfiles <strong>Gestor</strong> y <strong>Administrador</strong>. Ciudadanos y operadores usan la app móvil.</p>

          @if (motivo() === 'expirada') {
            <div class="alert">Tu sesión expiró. Vuelve a ingresar.</div>
          }
          @if (error(); as e) {
            <div class="alert alert--error" role="alert">
              <strong>{{ e.title }}</strong>
              <div>{{ e.detail }}</div>
            </div>
          }

          <mat-form-field class="full">
            <mat-label>Correo institucional</mat-label>
            <input matInput type="email" formControlName="email" autocomplete="username" />
            <mat-icon matPrefix>mail</mat-icon>
            @if (form.controls.email.hasError('email')) {
              <mat-error>Formato de correo no válido.</mat-error>
            } @else if (form.controls.email.hasError('required')) {
              <mat-error>El correo es obligatorio.</mat-error>
            }
          </mat-form-field>

          <mat-form-field class="full">
            <mat-label>Contraseña</mat-label>
            <input matInput [type]="verPass() ? 'text' : 'password'" formControlName="password" autocomplete="current-password" />
            <mat-icon matPrefix>lock</mat-icon>
            <button mat-icon-button matSuffix type="button" (click)="verPass.set(!verPass())" [attr.aria-label]="verPass() ? 'Ocultar contraseña' : 'Mostrar contraseña'">
              <mat-icon>{{ verPass() ? 'visibility_off' : 'visibility' }}</mat-icon>
            </button>
            @if (form.controls.password.hasError('required')) {
              <mat-error>La contraseña es obligatoria.</mat-error>
            }
          </mat-form-field>

          <button class="btn-cta full" mat-flat-button type="submit" [disabled]="cargando()">
            <mat-icon>{{ cargando() ? 'hourglass_top' : 'login' }}</mat-icon> {{ cargando() ? 'Verificando credenciales…' : 'Ingresar' }}
          </button>

          <details class="srv">
            <summary class="eyebrow">Servidor · {{ api.baseUrl() }}</summary>
            <mat-form-field class="full">
              <mat-label>Backend</mat-label>
              <mat-select [value]="api.baseUrl()" (selectionChange)="api.setBaseUrl($event.value)">
                @for (p of api.presets; track p.url) {
                  <mat-option [value]="p.url">{{ p.label }}</mat-option>
                }
                @if (!esPreset()) {
                  <mat-option [value]="api.baseUrl()">Personalizado · {{ api.baseUrl() }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            <mat-form-field class="full">
              <mat-label>URL personalizada</mat-label>
              <input matInput #custom placeholder="https://mi-backend.com" />
              <button mat-icon-button matSuffix type="button" (click)="custom.value && api.setBaseUrl(custom.value)" aria-label="Usar URL"><mat-icon>check</mat-icon></button>
            </mat-form-field>
          </details>
        </form>
      </main>
    </div>
  `,
  styles: `
    .login {
      min-height: 100vh;
      display: grid;
      grid-template-columns: minmax(0, 1.05fr) minmax(360px, 520px);
      background: var(--ca-background);
    }
    .plate {
      padding: 32px;
      background: var(--ca-surface);
      border-right: 1px solid var(--ca-border);
      display: flex;
    }
    .plate__frame {
      flex: 1;
      border: 1px solid var(--ca-border);
      outline: 1px solid var(--ca-border);
      outline-offset: 6px;
      display: flex;
      flex-direction: column;
      padding: 28px;
      color: var(--ca-primary);
      background-image:
        linear-gradient(var(--ca-grid-line) 1px, transparent 1px),
        linear-gradient(90deg, var(--ca-grid-line) 1px, transparent 1px);
      background-size: 24px 24px;
    }
    .plate__logo {
      flex: 1;
      display: grid;
      place-items: center;
      margin: 24px 0;
    }
    .plate__logo img {
      width: min(220px, 40vh);
      height: auto;
    }
    .plate__label {
      border-top: 2px solid var(--ca-on-surface-strong);
      padding-top: 14px;
    }
    .plate__title {
      font-family: var(--ca-font-display);
      font-weight: 800;
      font-size: 44px;
      line-height: 1;
      color: var(--ca-on-surface-strong);
    }
    .plate__sub {
      margin-top: 8px;
      font-size: 17px;
      color: var(--ca-on-surface-variant);
    }
    .panel {
      display: flex;
      flex-direction: column;
      padding: 24px 40px;
      background: var(--ca-card);
    }
    .panel__top {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .form {
      margin: auto 0;
      display: flex;
      flex-direction: column;
      gap: 14px;
      padding: 24px 0;
    }
    .form h1 {
      font-size: 34px;
    }
    .form p {
      margin: 0 0 8px;
    }
    .alert {
      border: 1px solid var(--ca-border);
      background: var(--ca-input);
      padding: 10px 14px;
      font-size: 14px;
    }
    .alert--error {
      border-color: var(--ca-danger);
      background: var(--ca-danger-soft);
    }
    .srv {
      margin-top: 10px;
      border-top: 1px solid var(--ca-border);
      padding-top: 12px;
    }
    .srv summary {
      cursor: pointer;
      margin-bottom: 12px;
      word-break: break-all;
    }
    @media (max-width: 880px) {
      .login {
        grid-template-columns: 1fr;
      }
      .plate {
        display: none;
      }
      .panel {
        padding: 20px;
      }
    }
  `,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly api = inject(ApiConfigService);
  protected readonly theme = inject(ThemeService);

  /** Query params (withComponentInputBinding) */
  motivo = input<string | undefined>();
  redirect = input<string | undefined>();

  protected readonly cargando = signal(false);
  protected readonly verPass = signal(false);
  protected readonly error = signal<ProblemDetail | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected esPreset(): boolean {
    return this.api.presets.some((p) => p.url === this.api.baseUrl());
  }

  protected ingresar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set(null);
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: (r) => {
        this.cargando.set(false);
        if (r.tipo === 'ok') {
          const destino = this.redirect() && this.redirect()!.startsWith(r.ruta.split('/').slice(0, 2).join('/')) ? this.redirect()! : r.ruta;
          this.router.navigateByUrl(destino);
        } else if (r.tipo === 'rol-movil') {
          // CP02: CIUDADANO / OPERADOR no pueden operar el portal web
          this.router.navigate(['/acceso-denegado'], { queryParams: { motivo: 'movil', rol: r.rol } });
        } else {
          this.error.set({
            type: 'about:blank',
            title: 'Cuenta pendiente de verificación',
            status: 403,
            detail: 'Tu cuenta aún no está verificada. Completa la verificación desde la app móvil.',
          });
        }
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(aProblemDetail(e));
      },
    });
  }
}
