import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { RolNombre } from '../api/models';
import { AuthService } from './auth.service';

/** AuthGuard: exige sesión válida (token presente y no expirado). */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.autenticado()) return true;
  return router.createUrlTree(['/login'], { queryParams: { redirect: state.url } });
};

/** RoleGuard: RN08, un único rol por sesión; si no coincide muestra la pantalla 403. */
export const roleGuard =
  (...roles: RolNombre[]): CanActivateFn =>
  () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const rol = auth.rol();
    if (rol && roles.includes(rol)) return true;
    return router.createUrlTree(['/acceso-denegado'], { queryParams: { motivo: 'seccion' } });
  };

/** Evita ver el login si ya hay sesión: redirige al tablero del rol. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? router.parseUrl(auth.rutaInicio()) : true;
};

/** Redirección raíz según rol. */
export const inicioRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return inject(Router).parseUrl(auth.autenticado() ? auth.rutaInicio() : '/login');
};
