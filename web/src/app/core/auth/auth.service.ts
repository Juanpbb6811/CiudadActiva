import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { ApiConfigService } from '../api/api-config.service';
import { ApiResponse, AuthResponse, JwtClaims, RolNombre } from '../api/models';
import { decodificarJwt, jwtExpirado } from './jwt.util';

const TOKEN_KEY = 'ca.session';

/** Roles que pueden operar el portal web. CIUDADANO y OPERADOR solo usan la app móvil (CP02). */
export const ROLES_WEB: RolNombre[] = ['ROLE_GESTOR', 'ROLE_ADMIN'];

export const RUTA_INICIO: Partial<Record<RolNombre, string>> = {
  ROLE_GESTOR: '/gestor/triage',
  ROLE_ADMIN: '/admin/estadisticas',
};

export type ResultadoLogin =
  | { tipo: 'ok'; ruta: string }
  | { tipo: 'rol-movil'; rol: RolNombre; nombre: string }
  | { tipo: 'verificacion' };

/**
 * Sesión del portal.
 * - El JWT se guarda en sessionStorage (se borra al cerrar la pestaña, no se comparte entre pestañas
 *   ni persiste en disco como localStorage). El backend no emite cookies httpOnly, así que esta es
 *   la opción más acotada disponible desde el cliente.
 * - RN08: un único rol por sesión, tomado del claim `rol` del token.
 * - Cierre automático cuando el token expira (claim `exp`).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiConfigService);
  private readonly router = inject(Router);

  private readonly _token = signal<string | null>(null);
  private expiryTimer: ReturnType<typeof setTimeout> | null = null;

  readonly token = this._token.asReadonly();
  readonly claims = computed<JwtClaims | null>(() => {
    const t = this._token();
    return t ? decodificarJwt(t) : null;
  });
  readonly rol = computed<RolNombre | null>(() => this.claims()?.rol ?? null);
  readonly nombre = computed(() => this.claims()?.nombre ?? '');
  readonly email = computed(() => this.claims()?.sub ?? '');
  readonly userId = computed(() => this.claims()?.userId ?? null);
  readonly autenticado = computed(() => !!this._token() && !jwtExpirado(this.claims()));

  constructor() {
    this.restaurar();
  }

  login(email: string, password: string): Observable<ResultadoLogin> {
    return this.http
      .post<ApiResponse<AuthResponse>>(this.api.url('/api/auth/login'), { email: email.trim(), password })
      .pipe(
        map((res) => res.data),
        map((data): ResultadoLogin => {
          if (!data?.token) return { tipo: 'verificacion' };
          const claims = decodificarJwt(data.token);
          const rol = (claims?.rol ?? data.usuario?.rol ?? 'ROLE_CIUDADANO') as RolNombre;
          if (!ROLES_WEB.includes(rol)) {
            // CP02: el token NO se guarda; el perfil solo opera en la app móvil
            return { tipo: 'rol-movil', rol, nombre: claims?.nombre ?? data.usuario?.nombre ?? '' };
          }
          this.guardar(data.token);
          return { tipo: 'ok', ruta: RUTA_INICIO[rol] ?? '/login' };
        }),
      );
  }

  logout(motivo?: 'expirada'): void {
    this._token.set(null);
    if (this.expiryTimer) clearTimeout(this.expiryTimer);
    try {
      sessionStorage.removeItem(TOKEN_KEY);
    } catch {
      /* noop */
    }
    this.router.navigate(['/login'], motivo ? { queryParams: { motivo } } : {});
  }

  rutaInicio(): string {
    const r = this.rol();
    return (r && RUTA_INICIO[r]) || '/login';
  }

  private guardar(token: string): void {
    this._token.set(token);
    try {
      sessionStorage.setItem(TOKEN_KEY, token);
    } catch {
      /* noop */
    }
    this.programarExpiracion();
  }

  private restaurar(): void {
    let t: string | null = null;
    try {
      t = sessionStorage.getItem(TOKEN_KEY);
    } catch {
      t = null;
    }
    if (!t) return;
    const c = decodificarJwt(t);
    if (!c || jwtExpirado(c) || !ROLES_WEB.includes(c.rol)) {
      try {
        sessionStorage.removeItem(TOKEN_KEY);
      } catch {
        /* noop */
      }
      return;
    }
    this._token.set(t);
    this.programarExpiracion();
  }

  private programarExpiracion(): void {
    if (this.expiryTimer) clearTimeout(this.expiryTimer);
    const exp = this.claims()?.exp;
    if (!exp) return;
    const ms = Math.min(exp * 1000 - Date.now(), 2_147_000_000);
    this.expiryTimer = setTimeout(() => this.logout('expirada'), Math.max(ms, 0));
  }
}
