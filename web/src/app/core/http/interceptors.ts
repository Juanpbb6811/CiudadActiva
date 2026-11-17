import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, finalize, throwError } from 'rxjs';
import { ApiConfigService } from '../api/api-config.service';
import { AuthService } from '../auth/auth.service';
import { LoadingService } from '../ui/loading.service';
import { aProblemDetail } from './problem-detail';

/** Marca una petición para que NO active la barra de carga global. */
export const SIN_LOADER = new HttpContextToken<boolean>(() => false);

/** Adjunta `Authorization: Bearer <token>` solo a peticiones dirigidas al backend configurado. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const api = inject(ApiConfigService);
  const token = auth.token();
  const esBackend = req.url.startsWith(api.baseUrl());
  const esLogin = req.url.endsWith('/api/auth/login');
  if (token && esBackend && !esLogin) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};

/**
 * Convierte cualquier error HTTP a ProblemDetail (RFC 7807) para que las vistas
 * muestren título + detalle de forma uniforme. En 401 con sesión activa cierra la sesión.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  return next(req).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401 && auth.token() && !req.url.endsWith('/api/auth/login')) {
        auth.logout('expirada');
      }
      return throwError(() => aProblemDetail(err));
    }),
  );
};

/** Contador global de peticiones en curso → barra de progreso en el encabezado. */
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.context.get(SIN_LOADER)) return next(req);
  const loading = inject(LoadingService);
  loading.inicio();
  return next(req).pipe(finalize(() => loading.fin()));
};
