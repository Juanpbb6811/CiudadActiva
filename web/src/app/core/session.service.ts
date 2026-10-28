import { Injectable, computed, signal } from '@angular/core';
import { Rol, Usuario } from './models';

const KEY = 'ca_session';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly _token = signal<string | null>(null);
  private readonly _user = signal<Usuario | null>(null);

  readonly token = this._token.asReadonly();
  readonly user = this._user.asReadonly();
  readonly rol = computed<Rol | null>(() => this._user()?.rol ?? null);
  readonly isLoggedIn = computed(() => !!this._token());

  constructor() {
    const raw = localStorage.getItem(KEY) ?? sessionStorage.getItem(KEY);
    if (raw) {
      try {
        const s = JSON.parse(raw);
        this._token.set(s.token);
        this._user.set(s.user);
      } catch { this.clear(); }
    }
  }

  save(token: string, user: Usuario, remember = true): void {
    this._token.set(token);
    this._user.set(user);
    const store = remember ? localStorage : sessionStorage;
    (remember ? sessionStorage : localStorage).removeItem(KEY);
    store.setItem(KEY, JSON.stringify({ token, user }));
  }

  updateUser(user: Usuario): void {
    this._user.set(user);
    const store = localStorage.getItem(KEY) ? localStorage : sessionStorage;
    store.setItem(KEY, JSON.stringify({ token: this._token(), user }));
  }

  clear(): void {
    this._token.set(null);
    this._user.set(null);
    localStorage.removeItem(KEY);
    sessionStorage.removeItem(KEY);
  }
}
