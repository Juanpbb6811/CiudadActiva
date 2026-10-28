import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ApiConfigService } from './api-config.service';
import { SessionService } from './session.service';

/** Equivale a AuthInterceptor.kt: agrega el JWT y, ante un 401 fuera de /api/auth, cierra la sesión. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const cfg = inject(ApiConfigService);
  const router = inject(Router);

  const isApi = req.url.startsWith(cfg.baseUrl());
  const token = session.token();
  const authReq = isApi && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (isApi && err.status === 401 && !req.url.includes('/api/auth/')) {
        session.clear();
        router.navigate(['/login'], { queryParams: { expirada: 1 } });
      }
      return throwError(() => err);
    }),
  );
};
