import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { homePorRol } from './constants';
import { Rol } from './models';
import { SessionService } from './session.service';

export const authGuard: CanActivateFn = () => {
  const s = inject(SessionService);
  return s.isLoggedIn() ? true : inject(Router).createUrlTree(['/login']);
};

/** Solo permite el acceso a los roles indicados; si no, redirige al inicio del rol actual. */
export const roleGuard = (...roles: Rol[]): CanActivateFn => () => {
  const s = inject(SessionService);
  const r = inject(Router);
  if (!s.isLoggedIn()) return r.createUrlTree(['/login']);
  return roles.includes(s.rol()!) ? true : r.createUrlTree([homePorRol(s.rol())]);
};

/** Para /login y /register: si ya hay sesión, va directo al inicio del rol. */
export const guestGuard: CanActivateFn = () => {
  const s = inject(SessionService);
  return s.isLoggedIn() ? inject(Router).createUrlTree([homePorRol(s.rol())]) : true;
};
